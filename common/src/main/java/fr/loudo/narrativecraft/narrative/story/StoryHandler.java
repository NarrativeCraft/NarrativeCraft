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
import fr.loudo.narrativecraft.narrative.cameraangle.CameraAngleSerializer;
import fr.loudo.narrativecraft.narrative.chapter.Chapter;
import fr.loudo.narrativecraft.narrative.character.ICharacterStory;
import fr.loudo.narrativecraft.narrative.inkTag.InkTagHandler;
import fr.loudo.narrativecraft.narrative.inkTag.InkTagHandlerException;
import fr.loudo.narrativecraft.narrative.scene.Scene;
import fr.loudo.narrativecraft.network.S2CPlayerSession;
import fr.loudo.narrativecraft.network.story.S2CDialogStop;
import fr.loudo.narrativecraft.network.story.S2CShowChoices;
import fr.loudo.narrativecraft.network.story.S2CShowDialogue;
import fr.loudo.narrativecraft.network.story.S2CStopStory;
import fr.loudo.narrativecraft.platform.Services;
import fr.loudo.narrativecraft.session.PlayerSession;
import net.minecraft.world.entity.Entity;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class StoryHandler implements InkTagHandler.Lifecycle {

    private static final Pattern SPEAKER_PATTERN = Pattern.compile("^(\\w+)\\s*:\\s*(.+)$", Pattern.DOTALL);

    private final PlayerSession playerSession;
    private final Story story;
    private final InkTagHandler inkTagHandler;
    private final Map<String, Entity> characterEntities = new HashMap<>();
    private final Map<String, DialogData> characterDialogData = new HashMap<>();
    private String lastCharacterSpoke = "";
    private Snapshot snapshot;

    @Nullable
    private String pendingDialogueText;

    private boolean ended = false;

    public StoryHandler(PlayerSession playerSession, String compiledStoryJson) throws Exception {
        this.playerSession = playerSession;
        this.story = new Story(compiledStoryJson);
        this.inkTagHandler = new InkTagHandler(playerSession, this);
    }

    public void start() throws Exception {
        Chapter firstChapter =
                NarrativeCraftMod.getInstance().getChapterManager().get(0);
        Scene firstScene = firstChapter.getSceneManager().get(0);
        playerSession.setChapter(firstChapter);
        playerSession.setScene(firstScene);
        Services.PACKET.sendToPlayer(
                playerSession.getPlayer(), new S2CPlayerSession(firstChapter.getId(), firstScene.getId()));
        advance();
    }

    public void start(String knotPath) throws Exception {
        story.choosePathString(knotPath);
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
        stop();
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

    private void sendDialogue(String text) {
        String[] parts = parseSpeaker(text);
        String speaker = parts[0];
        String dialogueText = parts[1];
        int entityId = S2CShowDialogue.NO_ENTITY;
        if (!speaker.isEmpty()) {
            Entity entity = characterEntities.get(speaker.toLowerCase());
            if (entity != null) {
                entityId = entity.getId();
            }
        }
        DialogData dialogData = characterDialogData.get(speaker.toLowerCase());
        String dialogDataJson = dialogData != null
                ? CameraAngleSerializer.serializeDialogData(dialogData).toString()
                : "";
        Services.PACKET.sendToPlayer(
                playerSession.getPlayer(),
                new S2CShowDialogue(speaker.toLowerCase(), dialogueText, entityId, dialogDataJson));
        lastCharacterSpoke = speaker;
    }

    public void registerEntity(ICharacterStory characterStory, Entity entity) {
        characterEntities.put(characterStory.getName().toLowerCase(), entity);
    }

    public void unregisterEntity(ICharacterStory characterStory, Entity entity) {
        characterEntities.remove(characterStory.getName().toLowerCase(), entity);
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

    private void finish() {
        ended = true;
        Services.PACKET.sendToPlayer(playerSession.getPlayer(), new S2CDialogStop());
        NarrativeCraftMod.LOGGER.info(
                "Story finished for player {}.",
                playerSession.getPlayer().getName().getString());
    }

    private static String[] parseSpeaker(String text) {
        Matcher matcher = SPEAKER_PATTERN.matcher(text.trim());
        if (matcher.matches()) {
            return new String[] {matcher.group(1), matcher.group(2).trim()};
        }
        return new String[] {"", text};
    }

    public Map<String, Entity> getCharacterEntities() {
        return characterEntities;
    }

    record Snapshot(String text, List<String> tags) {}
}
