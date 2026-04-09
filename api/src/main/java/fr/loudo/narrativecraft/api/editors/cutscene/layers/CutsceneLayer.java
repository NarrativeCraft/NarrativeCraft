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

package fr.loudo.narrativecraft.api.editors.cutscene.layers;

import fr.loudo.narrativecraft.api.editors.cutscene.keyframes.Keyframe;
import java.util.ArrayList;
import java.util.List;

public abstract class CutsceneLayer implements ICutsceneLayer {

    protected final ICutsceneLayerType layerType;
    private final List<Keyframe> keyframes = new ArrayList<>();

    public CutsceneLayer(ICutsceneLayerType layerType) {
        this.layerType = layerType;
    }

    public abstract Keyframe createDefaultKeyframe(int tick);

    public void addKeyframe(Keyframe keyframe) {
        keyframes.add(keyframe);
    }

    public void removeKeyframe(Keyframe keyframe) {
        keyframes.remove(keyframe);
    }

    public List<Keyframe> getKeyframes() {
        return keyframes;
    }

    @Override
    public ICutsceneLayerType getType() {
        return layerType;
    }

    /**
     * Verify to play a keyframe based on a range
     * @param tick current tick of the cutscene
     * @return if the current tick is between 2 keyframes on the timeline of his layer
     */
    protected boolean isTickCoveredBy(float tick) {
        if (keyframes.isEmpty()) return false;
        int first = keyframes.stream().mapToInt(Keyframe::getTick).min().getAsInt();
        int last = keyframes.stream().mapToInt(Keyframe::getTick).max().getAsInt();
        return tick >= first && tick <= last;
    }

    /**
     * Verify to play a keyframe a single time
     * @param tick current tick of the cutscene
     * @return if the tick match exactly a keyframe tick on the timeline of his layer
     */
    protected boolean isExactTick(float tick) {
        if (keyframes.isEmpty()) return false;
        return keyframes.stream().mapToInt(Keyframe::getTick).max().getAsInt() == tick;
    }
}
