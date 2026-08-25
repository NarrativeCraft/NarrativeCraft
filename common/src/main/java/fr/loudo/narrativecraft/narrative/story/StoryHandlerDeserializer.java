/*
 * NarrativeCraft - Create narrative games inside Minecraft. No coding, no game engine, only text and logic.
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

import com.google.gson.*;
import fr.loudo.narrativecraft.NarrativeCraftMod;
import fr.loudo.narrativecraft.api.utils.UserPosition;
import fr.loudo.narrativecraft.client.editors.widgets.DialogFieldSet;
import fr.loudo.narrativecraft.dialog.DialogData;
import fr.loudo.narrativecraft.dialog.DialogDataIO;
import fr.loudo.narrativecraft.managers.ChapterManager;
import fr.loudo.narrativecraft.narrative.chapter.Chapter;
import fr.loudo.narrativecraft.narrative.scene.Scene;
import fr.loudo.narrativecraft.session.PlayerSession;
import java.lang.reflect.Type;
import java.util.*;

public class StoryHandlerDeserializer implements JsonDeserializer<StoryHandler> {

    private final PlayerSession playerSession;

    public StoryHandlerDeserializer(PlayerSession playerSession) {
        this.playerSession = playerSession;
    }

    @Override
    public StoryHandler deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext context)
            throws JsonParseException {
        JsonObject obj = jsonElement.getAsJsonObject();

        String storyState = obj.get("storyState").getAsString();

        Map<String, DialogData> characterDialogData = new HashMap<>();
        if (obj.has("characterDialogData")) {
            JsonObject dialogDataObj = obj.getAsJsonObject("characterDialogData");
            for (String key : dialogDataObj.keySet()) {
                characterDialogData.put(
                        key, DialogDataIO.deserialize(dialogDataObj.getAsJsonObject(key), DialogFieldSet.CHARACTER));
            }
        }

        Set<UUID> interactionIds = new HashSet<>();
        if (obj.has("interactionIds")) {
            for (JsonElement element : obj.getAsJsonArray("interactionIds")) {
                interactionIds.add(UUID.fromString(element.getAsString()));
            }
        }

        String lastCharacterSpoke =
                obj.has("lastCharacterSpoke") ? obj.get("lastCharacterSpoke").getAsString() : "";

        boolean dialogVisible =
                obj.has("dialogVisible") && obj.get("dialogVisible").getAsBoolean();

        boolean ended = obj.has("ended") && obj.get("ended").getAsBoolean();
        boolean finishedStory =
                obj.has("finishedStory") && obj.get("finishedStory").getAsBoolean();

        UserPosition lastPosition = null;
        if (obj.has("lastPosition")) {
            lastPosition = UserPosition.deserialize(obj.getAsJsonObject("lastPosition"));
        }

        ChapterManager chapterManager = NarrativeCraftMod.getInstance().getChapterManager();
        if (chapterManager.getList().isEmpty()) {
            throw new JsonParseException("No compiled chapter available to load the save");
        }
        Chapter firstChapter = chapterManager.getList().get(0);
        if (firstChapter.getSceneManager().getList().isEmpty()) {
            throw new JsonParseException("No compiled scene available to load the save");
        }
        UUID chapterId =
                obj.has("chapterId") ? UUID.fromString(obj.get("chapterId").getAsString()) : firstChapter.getId();
        UUID sceneId = obj.has("sceneId")
                ? UUID.fromString(obj.get("sceneId").getAsString())
                : firstChapter.getSceneManager().get(0).getId();

        Chapter chapter = chapterManager.getById(chapterId);
        if (chapter == null) chapter = firstChapter;
        Scene scene = chapter.getSceneManager().getById(sceneId);
        if (scene == null && !chapter.getSceneManager().getList().isEmpty()) {
            scene = chapter.getSceneManager().get(0);
        }
        playerSession.apply(chapter, scene);

        try {
            StoryHandler storyHandler = new StoryHandler(
                    playerSession,
                    storyState,
                    characterDialogData,
                    interactionIds,
                    lastCharacterSpoke,
                    dialogVisible,
                    ended,
                    finishedStory);
            storyHandler.setLastPosition(lastPosition);
            return storyHandler;
        } catch (Exception e) {
            throw new JsonParseException(e);
        }
    }
}
