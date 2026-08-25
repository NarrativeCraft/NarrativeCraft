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
import fr.loudo.narrativecraft.api.utils.UserPosition;
import fr.loudo.narrativecraft.client.editors.widgets.DialogFieldSet;
import fr.loudo.narrativecraft.dialog.DialogData;
import fr.loudo.narrativecraft.dialog.DialogDataIO;
import java.lang.reflect.Type;
import java.util.Map;
import java.util.UUID;

public class StoryHandlerSerializer implements JsonSerializer<StoryHandler> {

    private final boolean includeLastPosition;

    public StoryHandlerSerializer(boolean includeLastPosition) {
        this.includeLastPosition = includeLastPosition;
    }

    @Override
    public JsonElement serialize(StoryHandler src, Type typeOfSrc, JsonSerializationContext context) {
        JsonObject json = new JsonObject();

        try {
            json.addProperty("storyState", src.getStory().getState().toJson());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        JsonObject characterDialogData = new JsonObject();
        for (Map.Entry<String, DialogData> entry : src.getCharacterDialogData().entrySet()) {
            characterDialogData.add(entry.getKey(), DialogDataIO.serialize(entry.getValue(), DialogFieldSet.CHARACTER));
        }
        json.add("characterDialogData", characterDialogData);

        JsonArray interactionIds = new JsonArray();
        for (UUID id : src.getInteractionIds()) {
            interactionIds.add(id.toString());
        }
        json.add("interactionIds", interactionIds);

        json.addProperty("lastCharacterSpoke", src.getLastCharacterSpoke());
        json.addProperty("dialogVisible", src.isDialogVisible());

        json.addProperty("ended", src.isEnded());
        json.addProperty("finishedStory", src.hasFinishedStory());
        json.addProperty(
                "chapterId", src.getPlayerSession().getChapter().getId().toString());
        json.addProperty("sceneId", src.getPlayerSession().getScene().getId().toString());
        if (includeLastPosition) {
            UserPosition lastPosition = UserPosition.of(src.getPlayerSession().getPlayer());
            json.add("lastPosition", lastPosition.serialize());
        }

        return json;
    }
}
