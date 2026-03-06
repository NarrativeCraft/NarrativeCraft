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

package fr.loudo.narrativecraft.files;

import fr.loudo.narrativecraft.NarrativeCraftMod;
import fr.loudo.narrativecraft.narrative.chapter.Chapter;
import java.io.*;

public class NarrativeCraftFileChapter extends NarrativeCraftFileDefault implements NarrativeCraftFileEditor<Chapter> {

    @Override
    public int create(Chapter entry) {

        File chapterDirectory = createDirectory(getWorkingFolder(), String.valueOf(entry.getChapterIndex()));
        if (chapterDirectory == null) {
            return OPERATION_FAILED;
        }

        return edit(entry) == OPERATION_SUCCESS ? OPERATION_SUCCESS : OPERATION_FAILED;
    }

    @Override
    public int edit(Chapter entry) {

        File chapterDirectory = new File(getWorkingFolder(), String.valueOf(entry.getChapterIndex()));
        if (!chapterDirectory.exists()) {
            return OPERATION_FAILED;
        }

        File dataFile = createFile(chapterDirectory, DATA_FILE_NAME);

        try (Writer writer = new BufferedWriter(new FileWriter(dataFile))) {
            writer.write(entry.toRawJson());
        } catch (Exception e) {
            NarrativeCraftMod.LOGGER.error("Couldn't write chapter {} data!", entry.formattedName());
            return OPERATION_FAILED;
        }

        return OPERATION_SUCCESS;
    }

    @Override
    public int delete(Chapter entry) {

        File chapterDirectory = new File(getWorkingFolder(), String.valueOf(entry.getChapterIndex()));
        if (!chapterDirectory.exists()) {
            return OPERATION_FAILED;
        }

        return deleteDirectory(chapterDirectory) ? OPERATION_SUCCESS : OPERATION_FAILED;
    }

    @Override
    public File getWorkingFolder() {
        return NarrativeCraftMod.getInstance().getFile().getInit().getChaptersDirectory();
    }
}
