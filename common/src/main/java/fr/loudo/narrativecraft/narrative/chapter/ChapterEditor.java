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

import fr.loudo.narrativecraft.NarrativeCraftMod;
import fr.loudo.narrativecraft.managers.ChapterManager;
import fr.loudo.narrativecraft.narrative.AbstractRankedNarrativeEntryEditor;
import fr.loudo.narrativecraft.narrative.NarrativeManager;
import java.util.UUID;

public class ChapterEditor extends AbstractRankedNarrativeEntryEditor<ChapterPayload, Chapter> {

    private final ChapterManager chapterManager =
            NarrativeCraftMod.getInstance().getChapterManager();

    public ChapterEditor() {
        super(Chapter.class);
    }

    @Override
    protected String getTypeKey() {
        return "chapter";
    }

    @Override
    protected NarrativeManager<Chapter> getSiblings(ChapterPayload payload) {
        return chapterManager;
    }

    @Override
    protected Chapter build(UUID entryId, ChapterPayload payload, Chapter existing) {
        int chapterIndex = existing == null ? getNextRank(chapterManager) : payload.getChapterIndex();
        return new Chapter(entryId, payload.getName(), chapterIndex);
    }

    @Override
    protected void copyAttributes(Chapter target, Chapter source) {
        target.setChapterIndex(source.getChapterIndex());
    }

    @Override
    protected int getRank(Chapter entry) {
        return entry.getChapterIndex();
    }

    @Override
    protected Chapter withRank(Chapter entry, int rank) {
        return new Chapter(entry.getId(), entry.getName(), rank);
    }
}
