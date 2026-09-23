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

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import fr.loudo.narrativecraft.NarrativeCraftMod;
import fr.loudo.narrativecraft.api.editors.cutscene.keyframes.Keyframe;
import fr.loudo.narrativecraft.api.editors.cutscene.layers.CutsceneLayer;
import fr.loudo.narrativecraft.api.editors.cutscene.layers.ICutsceneLayerType;
import fr.loudo.narrativecraft.narrative.NarrativeDeserializer;
import fr.loudo.narrativecraft.narrative.animation.Animation;
import fr.loudo.narrativecraft.narrative.scene.Scene;
import fr.loudo.narrativecraft.narrative.subscene.Subscene;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

public class CutsceneDeserializer extends NarrativeDeserializer<Cutscene> {

    @Override
    public Cutscene deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
            throws JsonParseException {
        JsonObject obj = json.getAsJsonObject();

        if (!hasSceneReference(obj)) {
            return null;
        }

        UUID id = parseId(obj);
        String name = parseName(obj);

        Scene scene = resolveScene(obj);
        if (scene == null) return null;

        List<Animation> animations = new ArrayList<>();
        if (obj.has("animationIds")) {
            for (JsonElement element : obj.getAsJsonArray("animationIds")) {
                Animation animation = scene.getAnimationManager().getById(UUID.fromString(element.getAsString()));
                if (animation != null) animations.add(animation);
            }
        }

        List<Subscene> subscenes = new ArrayList<>();
        if (obj.has("subsceneIds")) {
            for (JsonElement element : obj.getAsJsonArray("subsceneIds")) {
                Subscene subscene = scene.getSubsceneManager().getById(UUID.fromString(element.getAsString()));
                if (subscene != null) subscenes.add(subscene);
            }
        }

        Cutscene cutscene = new Cutscene(id, name, scene, animations, subscenes);

        if (obj.has("manualMaxTick")) {
            cutscene.setManualMaxTick(obj.get("manualMaxTick").getAsInt());
        }

        if (obj.has("layers")) {
            cutscene.setLayers(parseLayers(obj.getAsJsonArray("layers")));
        }

        return cutscene;
    }

    public static List<CutsceneLayer> parseLayers(String layersJson) {
        return parseLayers(JsonParser.parseString(layersJson).getAsJsonArray());
    }

    private static List<CutsceneLayer> parseLayers(JsonArray layersArray) {
        List<CutsceneLayer> cutsceneLayers = new ArrayList<>();

        for (JsonElement layerElement : layersArray) {
            JsonObject layerObject = layerElement.getAsJsonObject();
            if (!layerObject.has("type")) continue;

            String typeId = layerObject.get("type").getAsString();
            int sortIndex =
                    layerObject.has("sortIndex") ? layerObject.get("sortIndex").getAsInt() : 0;

            ICutsceneLayerType layerType =
                    NarrativeCraftMod.getInstance().getCutsceneLayerRegistry().getType(typeId);
            if (layerType == null) continue;

            CutsceneLayer layer = layerType.createLayer();
            layer.setSortIndex(sortIndex);

            if (layerObject.has("keyframes")) {
                for (JsonElement keyframeElement : layerObject.getAsJsonArray("keyframes")) {
                    Keyframe keyframe = layerType.deserializeKeyframe(layer, keyframeElement.getAsJsonObject());
                    if (keyframe != null) layer.addKeyframe(keyframe);
                }
            }

            cutsceneLayers.add(layer);
        }

        cutsceneLayers.sort(Comparator.comparingInt(CutsceneLayer::getSortIndex));
        return cutsceneLayers;
    }
}
