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

package fr.loudo.narrativecraft.narrative;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import fr.loudo.narrativecraft.utils.codec.NarrativeCodecs;
import java.util.Locale;
import java.util.UUID;
import java.util.function.BiFunction;

public abstract class NarrativeEntry<T extends NarrativeEntryPayload> {
    protected final UUID id;
    protected String name;

    protected static <P extends NarrativeEntryPayload, E extends NarrativeEntry<P>> Codec<E> entryCodec(
            MapCodec<P> payloadCodec, BiFunction<UUID, P, E> factory) {
        return NarrativeCodecs.versioned(RecordCodecBuilder.mapCodec(instance -> instance.group(
                        NarrativeCodecs.ID.forGetter(NarrativeEntry::getId),
                        payloadCodec.forGetter(NarrativeEntry::toPayload))
                .apply(instance, factory::apply)));
    }

    protected static <
                    P extends NarrativeEntryPayload,
                    D extends NarrativeEntryDetail,
                    E extends NarrativeEntry<P> & DetailedNarrativeEntry<D>>
            Codec<E> entryCodec(MapCodec<P> payloadCodec, MapCodec<D> detailCodec, BiFunction<UUID, P, E> factory) {
        return NarrativeCodecs.versioned(RecordCodecBuilder.mapCodec(instance -> instance.group(
                        NarrativeCodecs.ID.forGetter(NarrativeEntry::getId),
                        payloadCodec.forGetter(NarrativeEntry::toPayload),
                        detailCodec.forGetter(DetailedNarrativeEntry::getDetail))
                .apply(instance, (entryId, payload, detail) -> {
                    E entry = factory.apply(entryId, payload);
                    entry.setDetail(detail);
                    return entry;
                })));
    }

    public NarrativeEntry(UUID id, String name) {
        this.id = id;
        this.name = name;
    }

    public NarrativeEntry(String name) {
        this(UUID.randomUUID(), name);
    }

    public static String normalizeName(String name) {
        return name.toLowerCase(Locale.ROOT).replace(' ', '_');
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getNormalizedName() {
        return normalizeName(name);
    }

    public UUID getId() {
        return id;
    }

    public abstract T toPayload();

    /**
     * If your entry needs extra info in the name, otherwise juste return the current name
     * @return formatted name
     */
    public abstract String formattedName();

    public abstract String toFileName();
}
