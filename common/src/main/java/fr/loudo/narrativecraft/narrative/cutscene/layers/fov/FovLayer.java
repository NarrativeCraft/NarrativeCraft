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

package fr.loudo.narrativecraft.narrative.cutscene.layers.fov;

import fr.loudo.narrativecraft.api.editors.cutscene.keyframes.EasingType;
import fr.loudo.narrativecraft.api.editors.cutscene.keyframes.Interpolation;
import fr.loudo.narrativecraft.api.editors.cutscene.keyframes.KeyframeSegment;
import fr.loudo.narrativecraft.api.editors.cutscene.layers.CutsceneLayer;
import fr.loudo.narrativecraft.api.editors.cutscene.layers.ICutsceneLayerType;
import java.util.List;

public class FovLayer extends CutsceneLayer {

    public FovLayer(ICutsceneLayerType layerType) {
        super(layerType);
    }

    public boolean isActiveAt(float tick) {
        return !keyframes.isEmpty() && tick >= getFirstKeyframeTick();
    }

    public float getInterpolatedFov(float tick) {
        List<FovKeyframe> sorted = getSortedKeyframes(FovKeyframe.class);

        if (sorted.isEmpty()) throw new IllegalStateException("Cannot interpolate a FOV layer without keyframes");
        if (sorted.size() == 1) return sorted.get(0).getFov();
        if (tick <= sorted.get(0).getTick()) return sorted.get(0).getFov();
        if (tick >= sorted.get(sorted.size() - 1).getTick())
            return sorted.get(sorted.size() - 1).getFov();

        KeyframeSegment<FovKeyframe> seg = findSegment(sorted, tick);
        if (seg.to().getEasing() == EasingType.SMOOTH) {
            return (float) Interpolation.catmullRom(
                    seg.p0().getFov(),
                    seg.from().getFov(),
                    seg.to().getFov(),
                    seg.p3().getFov(),
                    seg.rawT());
        } else {
            return (float) Interpolation.lerp(
                    seg.from().getFov(),
                    seg.to().getFov(),
                    Interpolation.applyEasing(seg.to().getEasing(), seg.rawT()));
        }
    }
}
