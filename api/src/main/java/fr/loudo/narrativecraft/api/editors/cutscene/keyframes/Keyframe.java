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

package fr.loudo.narrativecraft.api.editors.cutscene.keyframes;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import fr.loudo.narrativecraft.api.editors.cutscene.layers.CutsceneLayer;

public abstract class Keyframe {

    public static final MapCodec<Integer> TICK = Codec.INT.fieldOf("tick");

    protected int tick;
    protected boolean isSelected;
    protected final CutsceneLayer layer;

    public static <K extends Keyframe> Codec<Keyframe> typedCodec(Class<K> keyframeClass, Codec<K> codec) {
        return codec.flatComapMap(
                keyframe -> keyframe,
                keyframe -> keyframeClass.isInstance(keyframe)
                        ? DataResult.success(keyframeClass.cast(keyframe))
                        : DataResult.error(() ->
                                keyframe.getClass().getSimpleName() + " is not a " + keyframeClass.getSimpleName()));
    }

    public Keyframe(CutsceneLayer layer, int tick) {
        this.layer = layer;
        this.tick = tick;
    }

    public int getTick() {
        return tick;
    }

    public void setTick(int tick) {
        this.tick = tick;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }

    public CutsceneLayer getLayer() {
        return layer;
    }
}
