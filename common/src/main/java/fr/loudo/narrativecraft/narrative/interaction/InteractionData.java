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

package fr.loudo.narrativecraft.narrative.interaction;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import fr.loudo.narrativecraft.narrative.NarrativeEntryDetail;
import fr.loudo.narrativecraft.utils.codec.NarrativeCodecs;
import java.util.List;

public record InteractionData(List<InteractionZone> zones, List<InteractionPoint> points)
        implements NarrativeEntryDetail {

    public static final MapCodec<InteractionData> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                    NarrativeCodecs.field(NarrativeCodecs.lenientList(InteractionZone.CODEC), "zones", List.of())
                            .forGetter(InteractionData::zones),
                    NarrativeCodecs.field(NarrativeCodecs.lenientList(InteractionPoint.CODEC), "points", List.of())
                            .forGetter(InteractionData::points))
            .apply(instance, InteractionData::new));

    public static final Codec<InteractionData> CODEC = MAP_CODEC.codec();

    public InteractionData {
        zones = List.copyOf(zones);
        points = List.copyOf(points);
    }
}
