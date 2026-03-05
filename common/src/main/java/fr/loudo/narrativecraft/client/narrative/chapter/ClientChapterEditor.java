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

package fr.loudo.narrativecraft.client.narrative.chapter;

import fr.loudo.narrativecraft.client.ClientNarrativeCraftMod;
import fr.loudo.narrativecraft.client.narrative.ClientNarrativeEntryEditor;
import fr.loudo.narrativecraft.managers.ChapterManager;
import fr.loudo.narrativecraft.narrative.chapter.Chapter;
import fr.loudo.narrativecraft.narrative.chapter.ChapterPayload;
import fr.loudo.narrativecraft.utils.UtilsClient;

public class ClientChapterEditor implements ClientNarrativeEntryEditor<ChapterPayload, Chapter> {

    final ChapterManager chapterManager = ClientNarrativeCraftMod.getInstance().getChapterManager();

    @Override
    public Chapter resolve(ChapterPayload payload) {
        return chapterManager.getById(payload.getId());
    }

    @Override
    public void add(ChapterPayload payload) {
        Chapter chapter =
                new Chapter(payload.getId(), payload.getName(), payload.getDescription(), payload.getChapterIndex());

        chapterManager.add(chapter);
        UtilsClient.reloadListScreen();
    }

    @Override
    public void edit(ChapterPayload payload) {
        Chapter oldChapter = resolve(payload);
        oldChapter.setName(payload.getName());
        oldChapter.setDescription(payload.getDescription());
        oldChapter.setChapterIndex(payload.getChapterIndex());
        UtilsClient.reloadListScreen();
    }

    @Override
    public void delete(ChapterPayload payload) {
        Chapter chapter = resolve(payload);
        chapterManager.remove(chapter);
        UtilsClient.reloadListScreen();
    }
}
