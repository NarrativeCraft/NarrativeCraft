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

package fr.loudo.narrativecraft.network;

import com.mojang.datafixers.util.Function3;
import fr.loudo.narrativecraft.narrative.NarrativeEntryDetail;
import fr.loudo.narrativecraft.narrative.NarrativeEntryPayload;
import fr.loudo.narrativecraft.narrative.NarrativeEntryType;
import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.DecoderException;
import io.netty.handler.codec.EncoderException;
import java.util.UUID;
import java.util.function.Function;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.codec.StreamCodec;

public final class NarrativeEntryDetailCodec {

    private NarrativeEntryDetailCodec() {}

    public static <P> StreamCodec<ByteBuf, P> create(
            Function3<UUID, NarrativeEntryPayload, NarrativeEntryDetail, P> factory,
            Function<P, UUID> entryId,
            Function<P, NarrativeEntryPayload> entry,
            Function<P, NarrativeEntryDetail> detail) {
        return new StreamCodec<>() {
            @Override
            public P decode(ByteBuf buffer) {
                UUID decodedEntryId = UUIDUtil.STREAM_CODEC.decode(buffer);
                NarrativeEntryPayload payload = NarrativeEntryPayload.STREAM_CODEC.decode(buffer);
                NarrativeEntryType type = NarrativeEntryType.fromPayload(payload);
                if (!type.hasDetail()) {
                    throw new DecoderException(type + " entries have no detail");
                }
                return factory.apply(
                        decodedEntryId, payload, type.getDetailCodec().decode(buffer));
            }

            @Override
            public void encode(ByteBuf buffer, P packet) {
                NarrativeEntryPayload payload = entry.apply(packet);
                NarrativeEntryDetail entryDetail = detail.apply(packet);
                NarrativeEntryType type = NarrativeEntryType.fromPayload(payload);
                if (!type.acceptsDetail(entryDetail)) {
                    throw new EncoderException(entryDetail.getClass().getSimpleName() + " is not a detail of " + type);
                }
                UUIDUtil.STREAM_CODEC.encode(buffer, entryId.apply(packet));
                NarrativeEntryPayload.STREAM_CODEC.encode(buffer, payload);
                type.getDetailCodec().encode(buffer, entryDetail);
            }
        };
    }
}
