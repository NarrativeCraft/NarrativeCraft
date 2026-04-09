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

import fr.loudo.narrativecraft.api.editors.cutscene.keyframes.EasingType;
import fr.loudo.narrativecraft.api.editors.cutscene.keyframes.Interpolation;
import fr.loudo.narrativecraft.api.editors.cutscene.keyframes.Keyframe;
import fr.loudo.narrativecraft.api.editors.cutscene.layers.CutsceneLayer;
import fr.loudo.narrativecraft.client.ClientNarrativeCraftMod;
import fr.loudo.narrativecraft.editors.cutscene.keyframes.FovKeyframe;
import fr.loudo.narrativecraft.editors.cutscene.layers.CutsceneLayerType;
import java.util.Comparator;
import java.util.List;
import net.minecraft.client.Minecraft;

public class FovLayer extends CutsceneLayer {

    public FovLayer(CutsceneLayerType layerType) {
        super(layerType);
    }

    @Override
    public String getTypeId() {
        return FovLayerType.ID;
    }

    @Override
    public FovKeyframe createDefaultKeyframe(int tick) {
        float fov = (float) (int) Minecraft.getInstance().options.fov().get();
        return new FovKeyframe(this, tick, fov);
    }

    @Override
    public boolean execute(float tick) {
        if (!isTickCoveredBy(tick)) return false;
        ClientNarrativeCraftMod.getInstance()
                .getPlayerSession()
                .getCutsceneDataSession()
                .setFov(getInterpolatedFov(tick));
        return true;
    }

    public float getInterpolatedFov(float tick) {
        List<FovKeyframe> sorted = getKeyframes().stream()
                .filter(k -> k instanceof FovKeyframe)
                .map(k -> (FovKeyframe) k)
                .sorted(Comparator.comparingInt(Keyframe::getTick))
                .toList();

        if (sorted.isEmpty())
            return (float) (int) Minecraft.getInstance().options.fov().get();
        if (sorted.size() == 1) return sorted.get(0).getFov();

        if (tick <= sorted.get(0).getTick()) return sorted.get(0).getFov();
        if (tick >= sorted.get(sorted.size() - 1).getTick())
            return sorted.get(sorted.size() - 1).getFov();

        int fromIdx = 0;
        for (int i = 0; i < sorted.size() - 1; i++) {
            if (tick < sorted.get(i + 1).getTick()) {
                fromIdx = i;
                break;
            }
        }

        FovKeyframe from = sorted.get(fromIdx);
        FovKeyframe to = sorted.get(fromIdx + 1);
        double rawT = (tick - from.getTick()) / (double) (to.getTick() - from.getTick());

        if (from.getEasing() == EasingType.SMOOTH) {
            FovKeyframe p0 = fromIdx > 0 ? sorted.get(fromIdx - 1) : from;
            FovKeyframe p3 = fromIdx + 2 < sorted.size() ? sorted.get(fromIdx + 2) : to;
            return (float) Interpolation.catmullRom(p0.getFov(), from.getFov(), to.getFov(), p3.getFov(), rawT);
        } else {
            return (float)
                    Interpolation.lerp(from.getFov(), to.getFov(), Interpolation.applyEasing(from.getEasing(), rawT));
        }
    }
}
