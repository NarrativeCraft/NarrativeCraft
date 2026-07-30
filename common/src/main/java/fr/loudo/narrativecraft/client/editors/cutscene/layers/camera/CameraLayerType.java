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

package fr.loudo.narrativecraft.client.editors.cutscene.layers.camera;

import com.google.gson.JsonObject;
import fr.loudo.narrativecraft.api.editors.cutscene.keyframes.EasingType;
import fr.loudo.narrativecraft.api.editors.cutscene.keyframes.Keyframe;
import fr.loudo.narrativecraft.api.editors.cutscene.layers.CutsceneLayer;
import fr.loudo.narrativecraft.api.editors.cutscene.layers.ICutsceneLayerType;
import fr.loudo.narrativecraft.editors.cutscene.keyframes.CameraKeyframe;
import fr.loudo.narrativecraft.editors.cutscene.keyframes.KeyframePosition;
import net.minecraft.world.phys.Vec3;

public class CameraLayerType implements ICutsceneLayerType {

    public static final String ID = "camera";

    @Override
    public String getId() {
        return ID;
    }

    @Override
    public String getName() {
        return "Camera";
    }

    @Override
    public CameraLayer createLayer() {
        return new CameraLayer(this);
    }

    @Override
    public JsonObject serializeKeyframe(Keyframe keyframe) {
        if (!(keyframe instanceof CameraKeyframe cameraKeyframe)) return null;

        JsonObject json = new JsonObject();
        json.addProperty("tick", cameraKeyframe.getTick());
        json.addProperty("easing", cameraKeyframe.getEasing().name());
        KeyframePosition position = cameraKeyframe.getPosition();
        json.addProperty("x", position.getPosition().x);
        json.addProperty("y", position.getPosition().y);
        json.addProperty("z", position.getPosition().z);
        json.addProperty("xRot", position.getRotation().x);
        json.addProperty("yRot", position.getRotation().y);
        json.addProperty("roll", position.getRotation().z);
        return json;
    }

    @Override
    public Keyframe deserializeKeyframe(CutsceneLayer layer, JsonObject json) {
        if (!json.has("tick") || !json.has("x")) return null;

        int tick = json.get("tick").getAsInt();
        EasingType easing =
                json.has("easing") ? EasingType.valueOf(json.get("easing").getAsString()) : EasingType.SMOOTH;
        double x = json.get("x").getAsDouble();
        double y = json.get("y").getAsDouble();
        double z = json.get("z").getAsDouble();
        double xRot = json.get("xRot").getAsDouble();
        double yRot = json.get("yRot").getAsDouble();
        double roll = json.has("roll") ? json.get("roll").getAsDouble() : 0.0;

        KeyframePosition position = new KeyframePosition(new Vec3(x, y, z), new Vec3(xRot, yRot, roll));
        CameraKeyframe cameraKeyframe = new CameraKeyframe(layer, tick, position);
        cameraKeyframe.setEasing(easing);
        return cameraKeyframe;
    }
}
