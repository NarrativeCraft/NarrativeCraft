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

package fr.loudo.narrativecraft.narrative.cutscene.layers.text;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import fr.loudo.narrativecraft.api.editors.cutscene.keyframes.Keyframe;
import fr.loudo.narrativecraft.api.editors.cutscene.layers.CutsceneLayer;
import fr.loudo.narrativecraft.api.editors.cutscene.layers.ICutsceneLayerType;
import fr.loudo.narrativecraft.utils.codec.NarrativeCodecs;
import java.util.List;

public class TextLayerType implements ICutsceneLayerType {

    public static final String ID = "text";

    @Override
    public String getId() {
        return ID;
    }

    @Override
    public String getName() {
        return "Text";
    }

    @Override
    public TextLayer createLayer() {
        return new TextLayer(this);
    }

    @Override
    public Codec<Keyframe> keyframeCodec(CutsceneLayer layer) {
        return Keyframe.typedCodec(TextKeyframe.class, RecordCodecBuilder.create(instance -> instance.group(
                        Keyframe.TICK.forGetter(TextKeyframe::getTick),
                        NarrativeCodecs.field(Codec.STRING.listOf(), "tags", List.<String>of())
                                .forGetter(TextKeyframe::getTags))
                .apply(instance, (tick, tags) -> {
                    TextKeyframe keyframe = new TextKeyframe(layer, tick);
                    keyframe.setTags(tags);
                    return keyframe;
                })));
    }
}
