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

package fr.loudo.narrativecraft.narrative;

import io.netty.buffer.ByteBuf;
import java.util.UUID;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public class NarrativeEntry {
    protected final UUID uuid;
    protected String name;
    protected String description;

    public NarrativeEntry(UUID uuid, String name, String description) {
        this.uuid = uuid;
        this.name = name;
        this.description = description;
    }

    public NarrativeEntry(String name, String description) {
        this.uuid = UUID.randomUUID();
        this.name = name;
        this.description = description;
    }

    public static final StreamCodec<ByteBuf, NarrativeEntry> BASE_STREAM_CODEC = StreamCodec.composite(
            UUIDUtil.STREAM_CODEC,
            NarrativeEntry::getId,
            ByteBufCodecs.STRING_UTF8,
            NarrativeEntry::getName,
            ByteBufCodecs.STRING_UTF8,
            NarrativeEntry::getDescription,
            NarrativeEntry::new);

    public static final StreamCodec<ByteBuf, NarrativeEntry> STREAM_CODEC = new StreamCodec<>() {
        @Override
        public NarrativeEntry decode(ByteBuf buffer) {
            int ordinal = buffer.readInt();
            NarrativeEntryType type = NarrativeEntryType.values()[ordinal];
            return type.getCodec().decode(buffer);
        }

        @Override
        public void encode(ByteBuf buffer, NarrativeEntry value) {
            NarrativeEntryType type = NarrativeEntryType.fromClass(value.getClass());
            buffer.writeInt(type.ordinal());
            type.getCodec().encode(buffer, value);
        }
    };

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public UUID getId() {
        return uuid;
    }
}
