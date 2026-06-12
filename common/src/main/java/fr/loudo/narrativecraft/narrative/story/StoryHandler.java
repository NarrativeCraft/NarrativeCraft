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
import fr.loudo.narrativecraft.api.events.character.CharacterDespawnEvent;
import fr.loudo.narrativecraft.api.events.character.CharacterSpawnEvent;
import fr.loudo.narrativecraft.api.events.dialog.DialogChoiceEvent;
import fr.loudo.narrativecraft.api.events.dialog.DialogEndEvent;
import fr.loudo.narrativecraft.api.events.dialog.DialogStartEvent;
import fr.loudo.narrativecraft.api.events.story.ChapterSceneStartEvent;
import fr.loudo.narrativecraft.api.events.story.SceneEndEvent;
import fr.loudo.narrativecraft.api.events.story.StoryEndEvent;
import fr.loudo.narrativecraft.api.events.story.StoryStartEvent;
import fr.loudo.narrativecraft.api.inkAction.InkActionUtil;
import fr.loudo.narrativecraft.api.narrative.IStoryHandler;
import fr.loudo.narrativecraft.api.narrative.character.ICharacter;
import fr.loudo.narrativecraft.client.editors.widgets.DialogFieldSet;
import fr.loudo.narrativecraft.dialog.DialogData;
import fr.loudo.narrativecraft.dialog.DialogDataIO;
import fr.loudo.narrativecraft.managers.CharacterManager;
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
import net.minecraft.ChatFormatting;
import net.minecraft.world.entity.Entity;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class StoryHandler implements InkTagHandler.Lifecycle, IStoryHandler {

    private static final Pattern SPEAKER_PATTERN = Pattern.compile("^([^:]+?)\\s*:\\s*(.+)$", Pattern.DOTALL);
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
        this.inkTagHandler = new InkTagHandler(playerSession, this, story);
    }

    public StoryHandler(PlayerSession playerSession, String saveJson) throws Exception {
        this.playerSession = playerSession;
        this.story = new Story(saveJson);
        this.inkTagHandler = new InkTagHandler(playerSession, this, story);
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
        this.inkTagHandler = new InkTagHandler(playerSession, this, story);
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
        NarrativeCraftMod.EVENT_BUS.post(new StoryStartEvent(playerSession));
        if (!loadedFromSave) {
            if (NarrativeCraftMod.getInstance().getChapterManager().getList().isEmpty()) {
                stop();
                throw new Exception("No chapter found to start the story!");
            }
            Chapter firstChapter =
                    NarrativeCraftMod.getInstance().getChapterManager().get(0);
            if (firstChapter.getSceneManager().getList().isEmpty()) {
                stop();
                throw new Exception("No scene found in the first chapter!");
            }
            Scene firstScene = firstChapter.getSceneManager().get(0);
            playerSession.setChapter(firstChapter);
            playerSession.setScene(firstScene);
            Services.PACKET.sendToPlayer(
                    playerSession.getPlayer(), new S2CPlayerSession(firstChapter.getId(), firstScene.getId()));
            NarrativeCraftMod.EVENT_BUS.post(new ChapterSceneStartEvent(playerSession, firstChapter, firstScene));
        }
        advance();
    }

    public void start(String knotPath) throws Exception {
        Services.PACKET.sendToPlayer(playerSession.getPlayer(), new S2CNotifyClientPlayStory());
        NarrativeCraftMod.EVENT_BUS.post(new StoryStartEvent(playerSession));
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
        NarrativeCraftMod.EVENT_BUS.post(new ChapterSceneStartEvent(playerSession, chapter, scene));
        advance();
    }

    public void tick() {
        inkTagHandler.tick();
    }

    public void stop() {
        if (playerSession.getScene() != null) {
            NarrativeCraftMod.EVENT_BUS.post(new SceneEndEvent(playerSession, playerSession.getScene()));
        }
        NarrativeCraftMod.EVENT_BUS.post(new StoryEndEvent(playerSession));
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
            List<Choice> choices = story.getCurrentChoices();
            List<String> choiceTexts = choices.stream().map(Choice::getText).toList();
            NarrativeCraftMod.EVENT_BUS.post(new DialogChoiceEvent(playerSession, choiceTexts, index));
            story.chooseChoiceIndex(index);
            advance();
        } catch (Exception exception) {
            onError(new InkTagHandlerException(exception.getMessage()));
        }
    }

    public void onDialogueAck() {
        NarrativeCraftMod.EVENT_BUS.post(new DialogEndEvent(playerSession));
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
        playerSession.setGameplayMode(false);
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

                String speaker = parseSpeaker(text)[0];
                if (lastCharacterSpoke.equalsIgnoreCase(TAG_2D) && text.isEmpty()) {
                    pendingDialogueText = text; // buffer B's text avant de return
                    Services.PACKET.sendToPlayer(playerSession.getPlayer(), new S2CDialogStop());
                    snapshot = new Snapshot(text, tags);
                    return;
                }
                // If the speaker is not the same as last speaker, stop dialog client-side FIRST before advance the
                // story.
                if (snapshot == null
                        && (!lastCharacterSpoke.isEmpty() && !lastCharacterSpoke.equalsIgnoreCase(speaker)
                                || willTagsBlock(tags))) {
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

    public void save(boolean showIcon) {
        if (showIcon) {
            Services.PACKET.sendToPlayer(playerSession.getPlayer(), new S2CRenderSaveIcon(0.9, 3, 0.9));
        }
        SaveFileManager saveFileManager = NarrativeCraftMod.getInstance().getSaveFileManager();
        saveFileManager.writeSave(playerSession);
    }

    private void sendDialogue(String text) {
        String[] parts = parseSpeaker(text);
        String speaker = parts[0].isEmpty() ? TAG_2D : parts[0];
        String dialogueText = parts[1];
        dialogueText = InkActionUtil.parseVariables(story, dialogueText);
        if (!text.isEmpty()) {
            NarrativeCraftMod.EVENT_BUS.post(new DialogStartEvent(playerSession, speaker, dialogueText));
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
                    DialogDataIO.serialize(dialogData, DialogFieldSet.ALL).toString();
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
        if (playerSession.getScene() != null) {
            NarrativeCraftMod.EVENT_BUS.post(new CharacterSpawnEvent(characterStory, playerSession.getScene()));
        }
    }

    public void unregisterEntity(ICharacterStory characterStory, Entity entity) {
        characterEntities.remove(characterStory.getName().toLowerCase(), entity);
        if (playerSession.getScene() != null) {
            NarrativeCraftMod.EVENT_BUS.post(new CharacterDespawnEvent(characterStory, playerSession.getScene()));
        }
    }

    public void unregisterEntity(ICharacterStory characterStory) {
        characterEntities.remove(characterStory.getName().toLowerCase());
        if (playerSession.getScene() != null) {
            NarrativeCraftMod.EVENT_BUS.post(new CharacterDespawnEvent(characterStory, playerSession.getScene()));
        }
    }

    public Entity getEntityFromCharacter(ICharacter characterStory) {
        return characterEntities.get(characterStory.getName().toLowerCase());
    }

    public boolean characterInStory(ICharacter characterStory) {
        return characterEntities.get(characterStory.getName().toLowerCase()) != null;
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

    public void finish() {
        Services.PACKET.sendToPlayer(playerSession.getPlayer(), new S2CDialogStop());
        NarrativeCraftMod.LOGGER.info(
                "Story finished for player {}.",
                playerSession.getPlayer().getName().getString());
        finishedStory = true;
        save(false);
        stop();
    }

    private static String[] parseSpeaker(String text) {
        Matcher matcher = SPEAKER_PATTERN.matcher(text.trim());
        if (matcher.matches()) {
            return new String[] {matcher.group(1), matcher.group(2).trim()};
        }
        return new String[] {TAG_2D, text};
    }

    public boolean willTagsBlock(List<String> tags) {
        for (String tag : tags) {
            if (tag.contains("--block")) return true;
        }
        return false;
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
