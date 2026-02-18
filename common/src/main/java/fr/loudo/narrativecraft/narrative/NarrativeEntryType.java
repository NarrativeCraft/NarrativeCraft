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

import fr.loudo.narrativecraft.narrative.chapter.Chapter;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;

public enum NarrativeEntryType {
    CHAPTER(Chapter.class, Chapter.STREAM_CODEC);

    private final Class<? extends NarrativeEntry> clazz;
    private final StreamCodec<? super ByteBuf, ? extends NarrativeEntry> codec;

    <T extends NarrativeEntry> NarrativeEntryType(Class<T> clazz, StreamCodec<? super ByteBuf, T> codec) {
        this.clazz = clazz;
        this.codec = codec;
    }

    public static NarrativeEntryType fromClass(Class<? extends NarrativeEntry> clazz) {
        for (NarrativeEntryType type : values()) {
            if (type.clazz.equals(clazz)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown NarrativeEntry type: " + clazz.getName());
    }

    @SuppressWarnings("unchecked")
    public <T extends NarrativeEntry> StreamCodec<? super ByteBuf, T> getCodec() {
        return (StreamCodec<? super ByteBuf, T>) codec;
    }
}
