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

package fr.loudo.narrativecraft.narrative.cutscene;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import fr.loudo.narrativecraft.api.editors.cutscene.keyframes.Keyframe;
import fr.loudo.narrativecraft.api.editors.cutscene.layers.CutsceneLayer;
import fr.loudo.narrativecraft.api.editors.cutscene.layers.ICutsceneLayerType;
import fr.loudo.narrativecraft.client.editors.cutscene.CutsceneMakerEditorLayer;
import fr.loudo.narrativecraft.narrative.animation.Animation;
import fr.loudo.narrativecraft.narrative.subscene.Subscene;
import java.lang.reflect.Type;
import java.util.List;

public class CutsceneSerializer implements JsonSerializer<Cutscene> {

    @Override
    public JsonElement serialize(Cutscene src, Type typeOfSrc, JsonSerializationContext context) {
        JsonObject json = new JsonObject();

        json.addProperty("id", src.getId().toString());
        json.addProperty("name", src.getName());
        json.addProperty("description", src.getDescription());
        json.addProperty("sceneId", src.getScene().getId().toString());
        json.addProperty("chapterId", src.getScene().getChapter().getId().toString());

        JsonArray animationIds = new JsonArray();
        for (Animation animation : src.getAnimations()) {
            animationIds.add(animation.getId().toString());
        }
        json.add("animationIds", animationIds);

        JsonArray subsceneIds = new JsonArray();
        for (Subscene subscene : src.getSubscenes()) {
            subsceneIds.add(subscene.getId().toString());
        }
        json.add("subsceneIds", subsceneIds);

        json.addProperty("manualMaxTick", src.getManualMaxTick());

        JsonArray layers = new JsonArray();
        if (src.getEditorLayers() != null) {
            for (CutsceneMakerEditorLayer editorLayer : src.getEditorLayers()) {
                layers.add(serializeLayer(editorLayer.getLayer()));
            }
        }
        json.add("layers", layers);

        return json;
    }

    public static String serializeLayers(List<CutsceneMakerEditorLayer> editorLayers) {
        JsonArray layers = new JsonArray();
        for (CutsceneMakerEditorLayer editorLayer : editorLayers) {
            layers.add(serializeLayer(editorLayer.getLayer()));
        }
        return new Gson().toJson(layers);
    }

    private static JsonObject serializeLayer(CutsceneLayer layer) {
        JsonObject json = new JsonObject();
        json.addProperty("type", layer.getTypeId());
        json.addProperty("sortIndex", layer.getSortIndex());

        JsonArray keyframes = new JsonArray();
        ICutsceneLayerType layerType = layer.getType();
        for (Keyframe keyframe : layer.getKeyframes()) {
            JsonObject keyframeJson = layerType.serializeKeyframe(keyframe);
            if (keyframeJson != null) keyframes.add(keyframeJson);
        }
        json.add("keyframes", keyframes);
        return json;
    }
}
