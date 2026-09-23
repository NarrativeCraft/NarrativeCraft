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

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import fr.loudo.narrativecraft.api.editors.cutscene.keyframes.EasingType;
import fr.loudo.narrativecraft.api.editors.cutscene.keyframes.Keyframe;
import fr.loudo.narrativecraft.api.editors.cutscene.layers.CutsceneLayer;
import fr.loudo.narrativecraft.api.editors.cutscene.layers.ICutsceneLayerType;
import fr.loudo.narrativecraft.utils.codec.NarrativeCodecs;

public class CameraLayerType implements ICutsceneLayerType {

    public static final String ID = "camera";

    private static final Codec<EasingType> EASING = NarrativeCodecs.enumByName(EasingType.class);

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
    public Codec<Keyframe> keyframeCodec(CutsceneLayer layer) {
        return Keyframe.typedCodec(CameraKeyframe.class, RecordCodecBuilder.create(instance -> instance.group(
                        Keyframe.TICK.forGetter(CameraKeyframe::getTick),
                        NarrativeCodecs.field(EASING, "easing", EasingType.SMOOTH)
                                .forGetter(CameraKeyframe::getEasing),
                        NarrativeCodecs.POSITION.forGetter(
                                keyframe -> keyframe.getPosition().getPosition()),
                        NarrativeCodecs.ROTATION.forGetter(
                                keyframe -> keyframe.getPosition().getRotation()))
                .apply(instance, (tick, easing, position, rotation) -> {
                    CameraKeyframe keyframe = new CameraKeyframe(layer, tick, new KeyframePosition(position, rotation));
                    keyframe.setEasing(easing);
                    return keyframe;
                })));
    }
}
