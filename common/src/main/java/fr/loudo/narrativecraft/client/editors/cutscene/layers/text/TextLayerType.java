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

package fr.loudo.narrativecraft.client.editors.cutscene.layers.text;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import fr.loudo.narrativecraft.api.editors.cutscene.keyframes.Keyframe;
import fr.loudo.narrativecraft.api.editors.cutscene.layers.CutsceneLayer;
import fr.loudo.narrativecraft.api.editors.cutscene.layers.ICutsceneLayerType;
import fr.loudo.narrativecraft.editors.cutscene.keyframes.TextKeyframe;
import java.util.ArrayList;
import java.util.List;

public class TextLayerType implements ICutsceneLayerType {

    public static final String ID = "text";

    @Override
    public String getId() {
        return ID;
    }

    @Override
    public String getName() {
        return "Text";
    }

    @Override
    public TextLayer createLayer() {
        return new TextLayer(this);
    }

    @Override
    public JsonObject serializeKeyframe(Keyframe keyframe) {
        if (!(keyframe instanceof TextKeyframe textKeyframe)) return null;

        JsonArray tagsArray = new JsonArray();
        for (String tag : textKeyframe.getTags()) {
            tagsArray.add(tag);
        }

        JsonObject json = new JsonObject();
        json.addProperty("tick", textKeyframe.getTick());
        json.add("tags", tagsArray);
        return json;
    }

    @Override
    public Keyframe deserializeKeyframe(CutsceneLayer layer, JsonObject json) {
        if (!json.has("tick")) return null;

        TextKeyframe textKeyframe = new TextKeyframe(layer, json.get("tick").getAsInt());

        if (json.has("tags")) {
            List<String> tags = new ArrayList<>();
            for (JsonElement tagElement : json.getAsJsonArray("tags")) {
                tags.add(tagElement.getAsString());
            }
            textKeyframe.setTags(tags);
        }

        return textKeyframe;
    }
}
