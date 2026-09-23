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

package fr.loudo.narrativecraft.narrative.cutscene;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.MapLike;
import com.mojang.serialization.RecordBuilder;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import fr.loudo.narrativecraft.NarrativeCraftMod;
import fr.loudo.narrativecraft.api.editors.cutscene.layers.CutsceneLayer;
import fr.loudo.narrativecraft.api.editors.cutscene.layers.ICutsceneLayerType;
import fr.loudo.narrativecraft.narrative.NarrativeEntryDetail;
import fr.loudo.narrativecraft.utils.codec.NarrativeCodecs;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

public record CutsceneTimeline(List<CutsceneLayer> layers, int manualMaxTick) implements NarrativeEntryDetail {

    private static final Codec<CutsceneLayer> LAYER_CODEC = new LayerCodec().codec();

    public static final MapCodec<CutsceneTimeline> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                    NarrativeCodecs.field(NarrativeCodecs.lenientList(LAYER_CODEC), "layers", List.of())
                            .forGetter(CutsceneTimeline::layers),
                    NarrativeCodecs.field(Codec.INT, "manualMaxTick", 0).forGetter(CutsceneTimeline::manualMaxTick))
            .apply(instance, CutsceneTimeline::new));

    public static final Codec<CutsceneTimeline> CODEC = MAP_CODEC.codec();

    public CutsceneTimeline {
        layers = layers.stream()
                .sorted(Comparator.comparingInt(CutsceneLayer::getSortIndex))
                .toList();
    }

    private static final class LayerCodec extends MapCodec<CutsceneLayer> {

        private static final String TYPE = "type";
        private static final String SORT_INDEX = "sortIndex";
        private static final String KEYFRAMES = "keyframes";

        @Override
        public <T> Stream<T> keys(DynamicOps<T> ops) {
            return Stream.of(TYPE, SORT_INDEX, KEYFRAMES).map(ops::createString);
        }

        @Override
        public <T> DataResult<CutsceneLayer> decode(DynamicOps<T> ops, MapLike<T> input) {
            T typeInput = input.get(TYPE);
            if (typeInput == null) return DataResult.error(() -> "Cutscene layer without type");
            return Codec.STRING.parse(ops, typeInput).flatMap(typeId -> {
                ICutsceneLayerType type = NarrativeCraftMod.getInstance()
                        .getCutsceneLayerRegistry()
                        .getType(typeId);
                if (type == null) return DataResult.error(() -> "Unknown cutscene layer type " + typeId);

                CutsceneLayer layer = type.createLayer();
                T sortIndexInput = input.get(SORT_INDEX);
                if (sortIndexInput != null) {
                    Codec.INT.parse(ops, sortIndexInput).result().ifPresent(layer::setSortIndex);
                }
                T keyframesInput = input.get(KEYFRAMES);
                if (keyframesInput != null) {
                    NarrativeCodecs.lenientList(type.keyframeCodec(layer))
                            .parse(ops, keyframesInput)
                            .result()
                            .ifPresent(keyframes -> keyframes.forEach(layer::addKeyframe));
                }
                return DataResult.success(layer);
            });
        }

        @Override
        public <T> RecordBuilder<T> encode(CutsceneLayer layer, DynamicOps<T> ops, RecordBuilder<T> prefix) {
            return prefix.add(TYPE, ops.createString(layer.getTypeId()))
                    .add(SORT_INDEX, ops.createInt(layer.getSortIndex()))
                    .add(
                            KEYFRAMES,
                            layer.getType().keyframeCodec(layer).listOf().encodeStart(ops, layer.getKeyframes()));
        }
    }
}
