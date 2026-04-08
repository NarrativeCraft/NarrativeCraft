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

/**
 * Describes a layer type that can be registered in the {@link fr.loudo.narrativecraft.api.editors.ICutsceneLayerRegistry}.
 * <p>
 * A layer type acts as a descriptor and factory: it defines the identity of a layer kind
 * (id, display name) and is responsible for producing new layer instances when a user
 * adds this layer to the cutscene timeline.
 * <p>
 */
public interface ICutsceneLayerType {

    String getId();

    String getName();

    /**
     * Creates and returns a new instance of the layer associated with this type.
     * <p>
     * This method is called each time a user adds this layer type to the cutscene timeline,
     * so each call must return a fresh, independent instance with its own state.
     *
     * @return a new {@link ICutsceneLayer} instance of this type
     */
    ICutsceneLayer createLayer();
}
