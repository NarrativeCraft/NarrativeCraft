/*
 * NarrativeCraft - Create your own stories, easily, and freely in Minecraft.
 * Copyright (c) 2025 LOUDO and contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package fr.loudo.narrativecraft.narrative.story;

import com.bladecoder.ink.runtime.Choice;
import com.bladecoder.ink.runtime.Story;
import fr.loudo.narrativecraft.NarrativeCraftMod;
import fr.loudo.narrativecraft.dialog.DialogData;
import fr.loudo.narrativecraft.managers.CharacterManager;
import fr.loudo.narrativecraft.narrative.cameraangle.CameraAngleSerializer;
import fr.loudo.narrativecraft.narrative.chapter.Chapter;
import fr.loudo.narrativecraft.narrative.character.CharacterStory;
import fr.loudo.narrativecraft.narrative.character.ICharacterStory;
import fr.loudo.narrativecraft.narrative.inkTag.InkTagHandler;
import fr.loudo.narrativecraft.narrative.inkTag.InkTagHandlerException;
import fr.loudo.narrativecraft.narrative.save.SaveFileManager;
import fr.loudo.narrativecraft.narrative.scene.Scene;
import fr.loudo.narrativecraft.network.S2CPlayerSession;
import fr.loudo.narrativecraft.network.S2CRenderSaveIcon;
import fr.loudo.narrativecraft.network.story.*;
import fr.loudo.narrativecraft.platform.Services;
import fr.loudo.narrativecraft.session.PlayerSession;
import fr.loudo.narrativecraft.utils.Translation;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.ChatFormatting;
import net.minecraft.world.entity.Entity;

public final class StoryHandler implements InkTagHandler.Lifecycle {

    private static final Pattern SPEAKER_PATTERN = Pattern.compile("^(\\w+)\\s*:\\s*(.+)$", Pattern.DOTALL);
    private static final Pattern KNOT_CHAPTER_PATTERN = Pattern.compile("^chapter_(\\d+)");
    private static final String TAG_2D = "[2D]";

    private final PlayerSession playerSession;
    private final Story story;
    private final InkTagHandler inkTagHandler;
    private final Map<String, Entity> characterEntities = new HashMap<>();
    private final Map<String, DialogData> characterDialogData = new HashMap<>();
    private final Set<UUID> interactionIds = new HashSet<>();
    private String lastCharacterSpoke = "";
    private Snapshot snapshot;
    private String pendingDialogueText;
    private boolean ended = false;
    private boolean finishedStory;
    private boolean loadedFromSave = false;

    public StoryHandler(PlayerSession playerSession) throws Exception {
        this.playerSession = playerSession;
        this.story = new Story(NarrativeCraftMod.getInstance().getCompiledStoryJson());
        this.inkTagHandler = new InkTagHandler(playerSession, this);
    }

    public StoryHandler(PlayerSession playerSession, String saveJson) throws Exception {
        this.playerSession = playerSession;
        this.story = new Story(saveJson);
        this.inkTagHandler = new InkTagHandler(playerSession, this);
    }

    StoryHandler(
            PlayerSession playerSession,
            String storyState,
            Map<String, DialogData> characterDialogData,
            Set<UUID> interactionIds,
            String lastCharacterSpoke,
            Snapshot snapshot,
            String pendingDialogueText,
            boolean ended,
            boolean finishedStory)
            throws Exception {
        this.playerSession = playerSession;
        this.story = new Story(NarrativeCraftMod.getInstance().getCompiledStoryJson());
        this.story.getState().loadJson(storyState);
        this.inkTagHandler = new InkTagHandler(playerSession, this);
        this.characterDialogData.putAll(characterDialogData);
        this.interactionIds.addAll(interactionIds);
        this.lastCharacterSpoke = lastCharacterSpoke;
        this.snapshot = snapshot;
        this.pendingDialogueText = pendingDialogueText;
        this.ended = ended;
        this.finishedStory = finishedStory;
        this.loadedFromSave = true;
    }

    public void start() throws Exception {
        Services.PACKET.sendToPlayer(playerSession.getPlayer(), new S2CNotifyClientPlayStory());
        if (!loadedFromSave) {
            Chapter firstChapter =
                    NarrativeCraftMod.getInstance().getChapterManager().get(0);
            Scene firstScene = firstChapter.getSceneManager().get(0);
            playerSession.setChapter(firstChapter);
            playerSession.setScene(firstScene);
            Services.PACKET.sendToPlayer(
                    playerSession.getPlayer(), new S2CPlayerSession(firstChapter.getId(), firstScene.getId()));
        }
        advance();
    }

    public void start(String knotPath) throws Exception {
        Services.PACKET.sendToPlayer(playerSession.getPlayer(), new S2CNotifyClientPlayStory());
        story.choosePathString(knotPath);
        Chapter chapter = getChapterFromKnotName(knotPath);
        if (chapter == null) {
            stop();
            throw new Exception("Chapter of the knot does not exists!");
        }
        Scene scene = getSceneFromKnotName(chapter, knotPath);
        if (scene == null) {
            stop();
            throw new Exception("Scene of the knot does not exists!");
        }
        playerSession.setChapter(chapter);
        playerSession.setScene(scene);
        advance();
    }

    public void tick() {
        inkTagHandler.tick();
    }

    public void stop() {
        ended = true;
        pendingDialogueText = null;
        inkTagHandler.stopAll();
        characterEntities.forEach((s, entity) -> entity.remove(Entity.RemovalReason.DISCARDED));
        Services.PACKET.sendToPlayer(
                playerSession.getPlayer(),
                new S2CCharacterStoryAction(UUID.randomUUID(), S2CCharacterStoryAction.Action.CLEAR));
        Services.PACKET.sendToPlayer(playerSession.getPlayer(), new S2CStopStory());
        playerSession.clear();
    }

    public void onChoiceSelected(int index) {
        if (ended) return;
        try {
            story.chooseChoiceIndex(index);
            advance();
        } catch (Exception exception) {
            onError(new InkTagHandlerException(exception.getMessage()));
        }
    }

    public void onDialogueAck() {
        if (ended) {
            stop();
        } else {
            advance();
        }
    }

    @Override
    public void onTagsDrained() {
        if (pendingDialogueText != null) {
            String text = pendingDialogueText;
            pendingDialogueText = null;
            sendDialogue(text);
            return;
        }
        advance();
    }

    @Override
    public void onError(InkTagHandlerException exception) {
        NarrativeCraftMod.LOGGER.error(
                "Story error for player {}: {}",
                playerSession.getPlayer().getName().getString(),
                exception);
        playerSession
                .getPlayer()
                .sendSystemMessage(Translation.message("error.story").withStyle(ChatFormatting.RED));
        stop();
    }

    public void playStitch(String stitchName) {
        String stitch = String.format("%s.%s", playerSession.getScene().knotName(), stitchName.toLowerCase());
        try {
            story.choosePathString(stitch);
            advance();
        } catch (Exception e) {
            NarrativeCraftMod.LOGGER.error(
                    "Story error for player {}: {}",
                    playerSession.getPlayer().getName().getString(),
                    e);
            stop();
        }
    }

    public Story getStory() {
        return story;
    }

    public InkTagHandler getInkTagHandler() {
        return inkTagHandler;
    }

    public PlayerSession getPlayerSession() {
        return playerSession;
    }

    public boolean isEnded() {
        return ended;
    }

    private void advance() {
        if (ended) return;
        try {
            while (story.canContinue()) {
                String text;
                List<String> tags;

                if (snapshot != null) {
                    text = snapshot.text;
                    tags = snapshot.tags;
                } else if (loadedFromSave) {
                    loadedFromSave = false;
                    text = story.getCurrentText().stripTrailing();
                    tags = story.getCurrentTags();
                } else {
                    text = story.Continue().stripTrailing();
                    tags = story.getCurrentTags();
                }

                // If the speaker is not the same as last speaker, stop dialog client-side FIRST before advance the
                // story.
                String speaker = parseSpeaker(text)[0];
                if (!lastCharacterSpoke.isEmpty()
                        && !lastCharacterSpoke.equalsIgnoreCase(speaker)
                        && snapshot == null) {
                    pendingDialogueText = text; // buffer B's text avant de return
                    Services.PACKET.sendToPlayer(playerSession.getPlayer(), new S2CDialogStop());
                    snapshot = new Snapshot(text, tags);
                    return;
                }
                snapshot = null;

                if (!tags.isEmpty()) {
                    if (!text.isEmpty()) {
                        pendingDialogueText = text;
                    }
                    inkTagHandler.enqueue(tags);
                    return;
                }

                if (!text.isEmpty()) {
                    sendDialogue(text);
                    return;
                }
            }

            // Execute last tags before ending story
            if (snapshot != null) {
                pendingDialogueText = snapshot.text;
                List<String> tags = snapshot.tags;
                inkTagHandler.enqueue(tags);
                snapshot = null;
                return;
            }

            List<Choice> choices = story.getCurrentChoices();
            if (!choices.isEmpty()) {
                sendChoices(choices);
                return;
            }

            finish();
        } catch (Exception exception) {
            onError(new InkTagHandlerException(exception.getMessage()));
        }
    }

    public void save() {
        Services.PACKET.sendToPlayer(playerSession.getPlayer(), new S2CRenderSaveIcon(0.9, 3, 0.9));
        SaveFileManager saveFileManager = NarrativeCraftMod.getInstance().getSaveFileManager();
        saveFileManager.writeSave(playerSession);
    }

    private void sendDialogue(String text) {
        String[] parts = parseSpeaker(text);
        String speaker = parts[0].isEmpty() ? TAG_2D : parts[0];
        String dialogueText = parts[1];
        if (!text.isEmpty()) {
            int entityId = S2CShowDialogue.NO_ENTITY;
            Entity entity = characterEntities.get(speaker.toLowerCase());
            if (entity != null) {
                entityId = entity.getId();
            }
            DialogData dialogData = characterDialogData.get(speaker.toLowerCase());
            if (dialogData == null) {
                dialogData = NarrativeCraftMod.getInstance().getGlobalDialogData();
            }
            String dialogDataJson =
                    CameraAngleSerializer.serializeDialogData(dialogData).toString();
            Services.PACKET.sendToPlayer(
                    playerSession.getPlayer(),
                    new S2CShowDialogue(speaker.toLowerCase(), dialogueText, entityId, dialogDataJson));
        }
        lastCharacterSpoke = speaker;
    }

    public Entity getMainCharacterEntity() {
        CharacterManager characterManager = NarrativeCraftMod.getInstance().getCharacterManager();
        CharacterStory mainCharacter = characterManager.getMainCharacter();
        if (mainCharacter == null) return null;
        for (String name : characterEntities.keySet()) {
            if (name.equals(mainCharacter.getName().toLowerCase())) {
                return characterEntities.get(name);
            }
        }
        return null;
    }

    public void registerEntity(ICharacterStory characterStory, Entity entity) {
        characterEntities.put(characterStory.getName().toLowerCase(), entity);
    }

    public void unregisterEntity(ICharacterStory characterStory, Entity entity) {
        characterEntities.remove(characterStory.getName().toLowerCase(), entity);
    }

    public void unregisterEntity(ICharacterStory characterStory) {
        characterEntities.remove(characterStory.getName().toLowerCase());
    }

    public Entity getEntityFromCharacter(ICharacterStory characterStory) {
        return characterEntities.get(characterStory.getName());
    }

    public boolean characterInStory(ICharacterStory characterStory) {
        return characterEntities.get(characterStory.getName()) != null;
    }

    public void registerDialogDataForCharacter(ICharacterStory characterStory, DialogData data) {
        characterDialogData.put(characterStory.getName().toLowerCase(), data);
    }

    public void unregisterDialogDataForCharacter(ICharacterStory characterStory) {
        characterDialogData.remove(characterStory.getName().toLowerCase());
    }

    private void sendChoices(List<Choice> choices) {
        List<String> texts = choices.stream().map(Choice::getText).toList();
        Services.PACKET.sendToPlayer(playerSession.getPlayer(), new S2CShowChoices(texts));
    }

    public void triggerChangeScene() {
        for (Entity entity : characterEntities.values()) {
            entity.remove(Entity.RemovalReason.KILLED);
        }
        characterEntities.clear();
        inkTagHandler.stopAll();
    }

    private void finish() {
        if (playerSession.isGameplayMode()) return;
        ended = true;
        Services.PACKET.sendToPlayer(playerSession.getPlayer(), new S2CDialogStop());
        NarrativeCraftMod.LOGGER.info(
                "Story finished for player {}.",
                playerSession.getPlayer().getName().getString());
        playerSession.clear();
    }

    private static String[] parseSpeaker(String text) {
        Matcher matcher = SPEAKER_PATTERN.matcher(text.trim());
        if (matcher.matches()) {
            return new String[] {matcher.group(1), matcher.group(2).trim()};
        }
        return new String[] {TAG_2D, text};
    }

    public Map<String, Entity> getCharacterEntities() {
        return characterEntities;
    }

    public String getLastCharacterSpoke() {
        return lastCharacterSpoke;
    }

    public void setLastCharacterSpoke(String lastCharacterSpoke) {
        this.lastCharacterSpoke = lastCharacterSpoke;
    }

    public Snapshot getSnapshot() {
        return snapshot;
    }

    public String getPendingDialogueText() {
        return pendingDialogueText;
    }

    public Map<String, DialogData> getCharacterDialogData() {
        return characterDialogData;
    }

    public Set<UUID> getInteractionIds() {
        return interactionIds;
    }

    public boolean hasAlreadyInteracted(UUID interactionId) {
        return interactionIds.contains(interactionId);
    }

    public void addInteractionId(UUID interactionId) {
        interactionIds.add(interactionId);
    }

    public boolean hasFinishedStory() {
        return finishedStory;
    }

    public void setFinishedStory(boolean finishedStory) {
        this.finishedStory = finishedStory;
    }

    public static Chapter getChapterFromKnotName(String knotName) {
        Matcher matcher = KNOT_CHAPTER_PATTERN.matcher(knotName);
        if (!matcher.find()) return null;
        int chapterIndex = Integer.parseInt(matcher.group(1));
        return NarrativeCraftMod.getInstance().getChapterManager().getChapterByIndex(chapterIndex);
    }

    public static Scene getSceneFromKnotName(Chapter chapter, String knotName) {
        for (Scene scene : chapter.getSceneManager().getList()) {
            if (scene.knotName().equals(knotName)) return scene;
        }
        return null;
    }

    public record Snapshot(String text, List<String> tags) {}
}
