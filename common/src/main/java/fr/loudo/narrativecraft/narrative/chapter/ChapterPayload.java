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

package fr.loudo.narrativecraft.narrative.chapter;

import fr.loudo.narrativecraft.narrative.NarrativeEntryPayload;
import net.minecraft.network.FriendlyByteBuf;

public class ChapterPayload extends NarrativeEntryPayload {

    private final int chapterIndex;

    public ChapterPayload(String name, String description, int chapterIndex) {
        super(name, description);
        this.chapterIndex = chapterIndex;
    }

    public static ChapterPayload read(FriendlyByteBuf buf) {
        return new ChapterPayload(buf.readUtf(), buf.readUtf(), buf.readVarInt());
    }

    @Override
    protected void writeData(FriendlyByteBuf buf) {
        buf.writeUtf(getName());
        buf.writeUtf(getDescription());
        buf.writeVarInt(chapterIndex);
    }

    public int getChapterIndex() {
        return chapterIndex;
    }
}
