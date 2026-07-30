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

import com.google.gson.JsonObject;
import fr.loudo.narrativecraft.api.editors.cutscene.keyframes.Keyframe;

/**
 * Describes a layer type that can be added to a cutscene timeline.
 * Acts as a factory for creating layer instances and as the serialization contract
 * for the keyframes they hold.
 */
public interface ICutsceneLayerType {

    /**
     * @return the unique identifier for this layer type (e.g. {@code "camera"})
     */
    String getId();

    /**
     * @return the human-readable display name shown in the layer selector
     */
    String getName();

    /**
     * Creates a new instance of the layer. Called each time the user adds this layer to the timeline,
     * and each time a saved layer of this type is loaded.
     *
     * @return a fresh layer instance
     */
    CutsceneLayer createLayer();

    /**
     * Serializes a keyframe of this layer type to JSON. The {@code tick} must be stored under that
     * exact property name, since it is rewritten when keyframes are pasted at a new playhead position.
     *
     * @return the JSON object, or {@code null} if the keyframe type is not supported
     */
    JsonObject serializeKeyframe(Keyframe keyframe);

    /**
     * Deserializes a keyframe of this layer type from JSON. The keyframe must be created against the
     * given layer without being added to it: the caller adds the returned keyframe itself.
     *
     * @return the created keyframe, or {@code null} if the JSON is invalid
     */
    Keyframe deserializeKeyframe(CutsceneLayer layer, JsonObject json);
}
