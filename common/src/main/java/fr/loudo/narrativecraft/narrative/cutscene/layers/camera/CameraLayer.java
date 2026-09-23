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

package fr.loudo.narrativecraft.narrative.cutscene.layers.camera;

import fr.loudo.narrativecraft.api.editors.cutscene.keyframes.EasingType;
import fr.loudo.narrativecraft.api.editors.cutscene.keyframes.Interpolation;
import fr.loudo.narrativecraft.api.editors.cutscene.keyframes.KeyframeSegment;
import fr.loudo.narrativecraft.api.editors.cutscene.layers.CutsceneLayer;
import fr.loudo.narrativecraft.api.editors.cutscene.layers.ICutsceneLayerType;
import java.util.List;
import net.minecraft.world.phys.Vec3;

public class CameraLayer extends CutsceneLayer {

    public CameraLayer(ICutsceneLayerType layerType) {
        super(layerType);
    }

    public List<CameraKeyframe> getSortedCameraKeyframes() {
        return getSortedKeyframes(CameraKeyframe.class);
    }

    public KeyframePosition getInterpolatedPosition(float tick) {
        List<CameraKeyframe> sorted = getSortedKeyframes(CameraKeyframe.class);

        if (sorted.isEmpty()) return null;
        if (sorted.size() == 1) return sorted.get(0).getPosition();
        if (tick <= sorted.get(0).getTick()) return sorted.get(0).getPosition();
        if (tick >= sorted.get(sorted.size() - 1).getTick())
            return sorted.get(sorted.size() - 1).getPosition();

        KeyframeSegment<CameraKeyframe> seg = findSegment(sorted, tick);
        if (seg.to().getEasing() == EasingType.SMOOTH) {
            return interpolateCatmullRom(
                    seg.p0().getPosition(),
                    seg.from().getPosition(),
                    seg.to().getPosition(),
                    seg.p3().getPosition(),
                    seg.rawT());
        } else {
            return interpolateLinear(
                    seg.from().getPosition(),
                    seg.to().getPosition(),
                    Interpolation.applyEasing(seg.to().getEasing(), seg.rawT()));
        }
    }

    private static KeyframePosition interpolateLinear(KeyframePosition a, KeyframePosition b, double t) {
        return new KeyframePosition(
                new Vec3(
                        Interpolation.lerp(a.getPosition().x, b.getPosition().x, t),
                        Interpolation.lerp(a.getPosition().y, b.getPosition().y, t),
                        Interpolation.lerp(a.getPosition().z, b.getPosition().z, t)),
                new Vec3(
                        Interpolation.lerp(a.getRotation().x, b.getRotation().x, t),
                        Interpolation.lerpAngle(a.getRotation().y, b.getRotation().y, t),
                        Interpolation.lerpAngle(a.getRotation().z, b.getRotation().z, t)));
    }

    private static KeyframePosition interpolateCatmullRom(
            KeyframePosition p0, KeyframePosition p1, KeyframePosition p2, KeyframePosition p3, double t) {
        return new KeyframePosition(
                new Vec3(
                        Interpolation.catmullRom(
                                p0.getPosition().x, p1.getPosition().x, p2.getPosition().x, p3.getPosition().x, t),
                        Interpolation.catmullRom(
                                p0.getPosition().y, p1.getPosition().y, p2.getPosition().y, p3.getPosition().y, t),
                        Interpolation.catmullRom(
                                p0.getPosition().z, p1.getPosition().z, p2.getPosition().z, p3.getPosition().z, t)),
                new Vec3(
                        Interpolation.catmullRom(
                                p0.getRotation().x, p1.getRotation().x, p2.getRotation().x, p3.getRotation().x, t),
                        Interpolation.catmullRomAngle(
                                p0.getRotation().y, p1.getRotation().y, p2.getRotation().y, p3.getRotation().y, t),
                        Interpolation.catmullRomAngle(
                                p0.getRotation().z, p1.getRotation().z, p2.getRotation().z, p3.getRotation().z, t)));
    }
}
