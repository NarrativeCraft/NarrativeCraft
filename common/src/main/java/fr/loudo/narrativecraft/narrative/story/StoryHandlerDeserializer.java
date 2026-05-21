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

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import fr.loudo.narrativecraft.dialog.DialogData;
import fr.loudo.narrativecraft.dialog.DialogDataIO;
import fr.loudo.narrativecraft.session.PlayerSession;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

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

        List<String> pendingTags = new ArrayList<>();
        if (obj.has("pendingTags")) {
            for (JsonElement element : obj.getAsJsonArray("pendingTags")) {
                pendingTags.add(element.getAsString());
            }
        }

        Map<String, DialogData> characterDialogData = new HashMap<>();
        if (obj.has("characterDialogData")) {
            JsonObject dialogDataObj = obj.getAsJsonObject("characterDialogData");
            for (String key : dialogDataObj.keySet()) {
                characterDialogData.put(key, DialogDataIO.deserialize(dialogDataObj.getAsJsonObject(key)));
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

        StoryHandler.Snapshot snapshot = null;
        if (obj.has("snapshot")) {
            JsonObject snapshotObj = obj.getAsJsonObject("snapshot");
            String text = snapshotObj.get("text").getAsString();
            List<String> tags = new ArrayList<>();
            if (snapshotObj.has("tags")) {
                for (JsonElement element : snapshotObj.getAsJsonArray("tags")) {
                    tags.add(element.getAsString());
                }
            }
            snapshot = new StoryHandler.Snapshot(text, tags);
        }

        String pendingDialogueText =
                obj.has("pendingDialogueText") ? obj.get("pendingDialogueText").getAsString() : null;

        boolean ended = obj.has("ended") && obj.get("ended").getAsBoolean();

        try {
            return new StoryHandler(
                    playerSession,
                    storyState,
                    pendingTags,
                    characterDialogData,
                    interactionIds,
                    lastCharacterSpoke,
                    snapshot,
                    pendingDialogueText,
                    ended);
        } catch (Exception e) {
            throw new JsonParseException(e);
        }
    }
}
