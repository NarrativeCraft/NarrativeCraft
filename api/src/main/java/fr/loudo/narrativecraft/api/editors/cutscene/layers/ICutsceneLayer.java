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

package fr.loudo.narrativecraft.api.editors.cutscene.layers;

/**
 * Represents a layer instance on the cutscene timeline.
 *
 * <p>Each layer holds a sequence of keyframes and belongs to a specific
 * {@link ICutsceneLayerType}. Extend {@link CutsceneLayer} rather than
 * implementing this interface directly.</p>
 */
public interface ICutsceneLayer {

    /**
     * @return the id of the {@link ICutsceneLayerType} that created this layer
     */
    String getTypeId();

    /**
     * @return the type descriptor that created this layer
     */
    ICutsceneLayerType getType();

    /**
     * Executes this layer at the given tick: applies whatever effect the layer produces
     * (e.g. camera movement, entity animation) if the tick falls within its keyframe range or keyframe tick.
     *
     * @param tick the current playback position (fractional ticks)
     * @return {@code true} if this layer handled the tick, {@code false} if the tick is outside its range
     */
    default boolean execute(float tick) {
        return false;
    }
}
