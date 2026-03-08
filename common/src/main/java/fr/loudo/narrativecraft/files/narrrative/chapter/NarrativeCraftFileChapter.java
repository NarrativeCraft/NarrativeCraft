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

package fr.loudo.narrativecraft.files.narrrative.chapter;

import com.google.gson.Gson;
import fr.loudo.narrativecraft.NarrativeCraftMod;
import fr.loudo.narrativecraft.files.DeserializationResult;
import fr.loudo.narrativecraft.files.NarrativeCraftFileDefault;
import fr.loudo.narrativecraft.files.NarrativeCraftFileEditor;
import fr.loudo.narrativecraft.managers.ChapterManager;
import fr.loudo.narrativecraft.narrative.NarrativeEntryEditorRegistry;
import fr.loudo.narrativecraft.narrative.chapter.Chapter;
import fr.loudo.narrativecraft.narrative.chapter.ChapterDeserializer;
import java.io.*;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

public class NarrativeCraftFileChapter extends NarrativeCraftFileDefault implements NarrativeCraftFileEditor<Chapter> {

    @Override
    public int create(Chapter entry) {

        File chapterDirectory = createDirectory(getChaptersFolder(), entry.toFileName());
        if (chapterDirectory == null) {
            return OPERATION_FAILED;
        }

        int result = edit(entry);
        if (result == OPERATION_FAILED) {
            return OPERATION_FAILED;
        }

        File scenesFolder = createDirectory(chapterDirectory, SCENES_FOLDER_NAME);
        if (scenesFolder == null) {
            return OPERATION_FAILED;
        }

        return OPERATION_SUCCESS;
    }

    @Override
    public int edit(Chapter entry) {

        File workingFolder = getChaptersFolder();
        ChapterManager chapterManager = NarrativeCraftMod.getInstance().getChapterManager();
        Chapter oldChapter = chapterManager.getById(entry.getId());

        try {

            // If user is editing chapter
            if (oldChapter != null) {
                File oldChapterDirectory = new File(workingFolder, oldChapter.toFileName());
                File newChapterDirectory = new File(workingFolder, entry.toFileName());
                // If the user wants to change the chapter index, then let's move everything...
                if (oldChapter.getChapterIndex() != entry.getChapterIndex()) {
                    int oldIndex = oldChapter.getChapterIndex();
                    int newIndex = entry.getChapterIndex();

                    if (newIndex > oldIndex) {
                        // Shift chapters left to free the target slot
                        if (!shiftChapterRange(chapterManager, workingFolder, oldIndex + 1, newIndex, -1, true)) {
                            return OPERATION_FAILED;
                        }
                    } else {
                        // Shift chapters right to free the target slot
                        if (!shiftChapterRange(chapterManager, workingFolder, oldIndex - 1, newIndex, 1, false)) {
                            return OPERATION_FAILED;
                        }
                    }
                    if (newChapterDirectory.exists()) {
                        return OPERATION_FAILED;
                    }
                }

                Files.move(oldChapterDirectory.toPath(), newChapterDirectory.toPath());
            }

            File chapterDirectory = new File(workingFolder, entry.toFileName());
            if (!chapterDirectory.exists()) {
                return OPERATION_FAILED;
            }

            File dataFile = createFile(chapterDirectory, DATA_FILE_NAME);

            try (Writer writer = new BufferedWriter(new FileWriter(dataFile))) {
                writer.write(entry.toRawJson());
            }

        } catch (Exception e) {
            NarrativeCraftMod.LOGGER.error("Failed to edit chapter {}", entry.formattedName(), e);
            return OPERATION_FAILED;
        }

        return OPERATION_SUCCESS;
    }

    @Override
    public int delete(Chapter entry) {

        File workingFolder = getChaptersFolder();
        File chapterDirectory = new File(workingFolder, entry.toFileName());
        if (!chapterDirectory.exists()) {
            return OPERATION_FAILED;
        }

        ChapterManager chapterManager = NarrativeCraftMod.getInstance().getChapterManager();

        try {
            if (!deleteDirectory(chapterDirectory)) {
                return OPERATION_FAILED;
            }

            // Shift every chapter after the deleted one to fill the gap
            int deletedIndex = entry.getChapterIndex();
            int lastIndex = getLastChapterIndex(chapterManager);

            if (deletedIndex < lastIndex) {
                if (!shiftChapterRange(chapterManager, workingFolder, deletedIndex + 1, lastIndex, -1, true)) {
                    return OPERATION_FAILED;
                }
            }

            return OPERATION_SUCCESS;
        } catch (Exception e) {
            NarrativeCraftMod.LOGGER.error("Failed to delete chapter {}", entry.formattedName(), e);
            return OPERATION_FAILED;
        }
    }

    @Override
    public List<DeserializationResult<Chapter>> deserialize() {

        List<DeserializationResult<Chapter>> deserializationResults = new ArrayList<>();

        File chaptersFolder = getChaptersFolder();
        File[] allContents = chaptersFolder.listFiles();
        if (allContents == null) {
            return null;
        }

        gsonBuilder.registerTypeAdapter(Chapter.class, new ChapterDeserializer());
        Gson gson = gsonBuilder.create();

        for (File file : allContents) {
            try {
                File dataFile = new File(file, DATA_FILE_NAME);
                String content = Files.readString(dataFile.toPath());
                Chapter chapter = gson.fromJson(content, Chapter.class);
                if (chapter == null) {
                    throw new Exception(String.format("Chapter %s deserialization returned null", file.getName()));
                }
                deserializationResults.add(new DeserializationResult<>(chapter, false, file.getName()));
            } catch (Exception e) {
                NarrativeCraftMod.LOGGER.error("Failed to init chapter {}", file.getName(), e);
                deserializationResults.add(new DeserializationResult<>(null, true, file.getName()));
            }
        }

        return deserializationResults;
    }

    private boolean shiftChapterRange(
            ChapterManager chapterManager,
            File workingFolder,
            int fromInclusive,
            int toInclusive,
            int offset,
            boolean ascending)
            throws IOException {
        if (ascending) {
            for (int i = fromInclusive; i <= toInclusive; i++) {
                if (!shiftSingleChapter(chapterManager, workingFolder, i, offset)) {
                    return false;
                }
            }
        } else {
            for (int i = fromInclusive; i >= toInclusive; i--) {
                if (!shiftSingleChapter(chapterManager, workingFolder, i, offset)) {
                    return false;
                }
            }
        }

        return true;
    }

    private boolean shiftSingleChapter(ChapterManager chapterManager, File workingFolder, int currentIndex, int offset)
            throws IOException {
        Chapter chapterToShift = chapterManager.getChapterByIndex(currentIndex);
        if (chapterToShift == null) {
            return true;
        }

        File oldFolder = new File(workingFolder, chapterToShift.toFileName());

        int targetIndex = currentIndex + offset;
        chapterToShift.setChapterIndex(targetIndex);

        File newFolder = new File(workingFolder, chapterToShift.toFileName());

        if (newFolder.exists()) {
            return false;
        }

        Files.move(oldFolder.toPath(), newFolder.toPath());

        NarrativeEntryEditorRegistry.getInstance().edit(chapterToShift.getId(), chapterToShift.toPayload(), null);

        return true;
    }

    private int getLastChapterIndex(ChapterManager chapterManager) {
        int index = 0;
        while (chapterManager.getChapterByIndex(index + 1) != null) {
            index++;
        }
        return index;
    }
}
