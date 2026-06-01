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

package fr.loudo.narrativecraft.client.editors.cutscene.layers.fov;

import com.google.gson.JsonObject;
import fr.loudo.narrativecraft.api.editors.cutscene.keyframes.EasingType;
import fr.loudo.narrativecraft.api.editors.cutscene.keyframes.Keyframe;
import fr.loudo.narrativecraft.api.editors.cutscene.layers.CutsceneLayer;
import fr.loudo.narrativecraft.editors.cutscene.keyframes.FovKeyframe;
import fr.loudo.narrativecraft.editors.cutscene.layers.CutsceneLayerType;

public class FovLayerType extends CutsceneLayerType {

    public static final String ID = "fov";

    @Override
    public String getId() {
        return ID;
    }

    @Override
    public String getName() {
        return "FOV";
    }

    @Override
    public FovLayer createLayer() {
        return new FovLayer(this);
    }

    @Override
    public JsonObject serializeKeyframe(Keyframe keyframe) {
        if (!(keyframe instanceof FovKeyframe fovKeyframe)) return null;

        JsonObject json = new JsonObject();
        json.addProperty("tick", fovKeyframe.getTick());
        json.addProperty("fov", fovKeyframe.getFov());
        json.addProperty("easing", fovKeyframe.getEasing().name());
        return json;
    }

    @Override
    public Keyframe deserializeKeyframe(CutsceneLayer layer, JsonObject json) {
        if (!json.has("tick") || !json.has("fov")) return null;

        int tick = json.get("tick").getAsInt();
        float fov = json.get("fov").getAsFloat();
        String easing = json.get("easing").getAsString();
        EasingType easingType;
        try {
            easingType = EasingType.valueOf(easing.toUpperCase());
        } catch (IllegalArgumentException e) {
            easingType = EasingType.LINEAR;
        }
        FovKeyframe fovKeyframe = new FovKeyframe(layer, tick, fov);
        fovKeyframe.setEasing(easingType);

        return fovKeyframe;
    }
}
