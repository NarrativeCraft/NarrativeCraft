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
import net.minecraft.network.codec.StreamCodec;

public class NarrativeEntryPayload {
    private final UUID uuid;
    private final String name;
    private final String description;

    public static final StreamCodec<ByteBuf, NarrativeEntryPayload> STREAM_CODEC = new StreamCodec<>() {
        @Override
        public NarrativeEntryPayload decode(ByteBuf buffer) {
            int ordinal = buffer.readInt();
            NarrativeEntryType type = NarrativeEntryType.values()[ordinal];
            return type.getCodec().decode(buffer);
        }

        @Override
        public void encode(ByteBuf buffer, NarrativeEntryPayload value) {
            NarrativeEntryType type = NarrativeEntryType.fromClass(value.getClass());
            buffer.writeInt(type.ordinal());
            type.getCodec().encode(buffer, value);
        }
    };

    public NarrativeEntryPayload(UUID uuid, String name, String description) {
        this.uuid = uuid;
        this.name = name;
        this.description = description;
    }

    public UUID getId() {
        return uuid;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }
}
