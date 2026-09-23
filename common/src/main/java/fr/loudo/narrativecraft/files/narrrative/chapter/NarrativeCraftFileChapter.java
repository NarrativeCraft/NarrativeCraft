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

package fr.loudo.narrativecraft.files.narrrative.chapter;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import fr.loudo.narrativecraft.NarrativeCraftMod;
import fr.loudo.narrativecraft.files.DeserializationResult;
import fr.loudo.narrativecraft.files.EntryChange;
import fr.loudo.narrativecraft.files.FileTransaction;
import fr.loudo.narrativecraft.files.InkFileGenerator;
import fr.loudo.narrativecraft.files.NarrativeCraftFileDefault;
import fr.loudo.narrativecraft.files.NarrativeCraftFileEditor;
import fr.loudo.narrativecraft.files.NarrativeCraftFileUtil;
import fr.loudo.narrativecraft.files.NarrativeCraftFileWriter;
import fr.loudo.narrativecraft.files.RankedNarrativeCraftFileEditor;
import fr.loudo.narrativecraft.narrative.OperationResult;
import fr.loudo.narrativecraft.narrative.chapter.Chapter;
import fr.loudo.narrativecraft.narrative.chapter.ChapterDeserializer;
import fr.loudo.narrativecraft.narrative.chapter.ChapterSerializer;
import fr.loudo.narrativecraft.narrative.scene.Scene;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

public class NarrativeCraftFileChapter extends NarrativeCraftFileDefault
        implements RankedNarrativeCraftFileEditor<Chapter> {

    private static final Gson SERIALIZER = new GsonBuilder()
            .registerTypeAdapter(Chapter.class, new ChapterSerializer())
            .create();
    private static final Gson DESERIALIZER = new GsonBuilder()
            .registerTypeAdapter(Chapter.class, new ChapterDeserializer())
            .create();

    @Override
    public OperationResult create(Chapter entry) {
        FileTransaction transaction = new FileTransaction();
        try {
            File chapterFolder = new File(NarrativeCraftFileUtil.getChaptersFolder(), entry.toFileName());
            if (chapterFolder.exists()) {
                throw new IOException("Chapter directory " + chapterFolder + " already exists");
            }
            transaction.createDirectories(new File(chapterFolder, SCENES_FOLDER_NAME));
            writeData(transaction, chapterFolder, entry);
            InkFileGenerator.writeChapterInkFile(transaction, chapterFolder, entry);
            transaction.commit();
        } catch (IOException e) {
            transaction.rollback();
            NarrativeCraftMod.LOGGER.error("Failed to create chapter {}", entry.getName(), e);
            return NarrativeCraftFileEditor.storageFailure(entry);
        }
        InkFileGenerator.regenerateMainInk();
        return OperationResult.success();
    }

    @Override
    public OperationResult editAll(List<EntryChange<Chapter>> changes) {
        FileTransaction transaction = new FileTransaction();
        try {
            applyChanges(transaction, changes);
            transaction.commit();
        } catch (IOException e) {
            transaction.rollback();
            NarrativeCraftMod.LOGGER.error("Failed to edit chapters", e);
            return NarrativeCraftFileEditor.storageFailure(changes.getFirst().existing());
        }
        InkFileGenerator.regenerateMainInk();
        return OperationResult.success();
    }

    @Override
    public OperationResult deleteAndShift(Chapter entry, List<EntryChange<Chapter>> shiftedEntries) {
        FileTransaction transaction = new FileTransaction();
        try {
            File chapterFolder = new File(NarrativeCraftFileUtil.getChaptersFolder(), entry.toFileName());
            if (!chapterFolder.exists()) {
                throw new IOException("Chapter directory " + chapterFolder + " does not exist");
            }
            transaction.delete(chapterFolder);
            applyChanges(transaction, shiftedEntries);
            transaction.commit();
        } catch (IOException e) {
            transaction.rollback();
            NarrativeCraftMod.LOGGER.error("Failed to delete chapter {}", entry.getName(), e);
            return NarrativeCraftFileEditor.storageFailure(entry);
        }
        InkFileGenerator.regenerateMainInk();
        return OperationResult.success();
    }

    private void applyChanges(FileTransaction transaction, List<EntryChange<Chapter>> changes) throws IOException {
        File chaptersFolder = NarrativeCraftFileUtil.getChaptersFolder();

        List<FileTransaction.Move> chapterMoves = new ArrayList<>();
        for (EntryChange<Chapter> change : changes) {
            chapterMoves.add(new FileTransaction.Move(
                    new File(chaptersFolder, change.existing().toFileName()),
                    new File(chaptersFolder, change.updated().toFileName())));
        }
        transaction.moveAll(chapterMoves);

        for (EntryChange<Chapter> change : changes) {
            File chapterFolder = new File(chaptersFolder, change.updated().toFileName());
            int oldIndex = change.existing().getChapterIndex();
            int newIndex = change.updated().getChapterIndex();
            if (oldIndex != newIndex) {
                reindexScenes(transaction, chapterFolder, change.existing(), oldIndex, newIndex);
                InkFileGenerator.reindexChapterInkFiles(
                        transaction, chapterFolder, change.existing(), oldIndex, newIndex);
            }
            writeData(transaction, chapterFolder, change.updated());
        }
    }

    private void reindexScenes(
            FileTransaction transaction, File chapterFolder, Chapter chapter, int oldIndex, int newIndex)
            throws IOException {
        File scenesFolder = new File(chapterFolder, SCENES_FOLDER_NAME);
        List<FileTransaction.Move> sceneMoves = new ArrayList<>();
        for (Scene scene : chapter.getSceneManager().getList()) {
            File sceneFolder = new File(scenesFolder, scene.toFileName(oldIndex));
            if (!sceneFolder.exists()) continue;
            sceneMoves.add(new FileTransaction.Move(sceneFolder, new File(scenesFolder, scene.toFileName(newIndex))));
        }
        transaction.moveAll(sceneMoves);
    }

    private void writeData(FileTransaction transaction, File chapterFolder, Chapter chapter) throws IOException {
        transaction.write(new File(chapterFolder, DATA_FILE_NAME), writer -> SERIALIZER.toJson(chapter, writer));
    }

    @Override
    public List<DeserializationResult<Chapter>> deserialize() {
        List<DeserializationResult<Chapter>> deserializationResults = new ArrayList<>();

        File chaptersFolder = NarrativeCraftFileUtil.getChaptersFolder();
        File[] allContents = chaptersFolder.listFiles();
        if (allContents == null) {
            return deserializationResults;
        }

        for (File file : allContents) {
            if (NarrativeCraftFileWriter.isTemporary(file)) continue;
            try {
                File dataFile = new File(file, DATA_FILE_NAME);
                String content = Files.readString(dataFile.toPath());
                Chapter chapter = DESERIALIZER.fromJson(content, Chapter.class);
                if (chapter == null) {
                    throw new Exception(String.format("Chapter %s deserialization returned null", file.getName()));
                }
                String folderName = migrateFolderName(chaptersFolder, file, chapter);
                deserializationResults.add(new DeserializationResult<>(chapter, false, folderName));
            } catch (Exception e) {
                NarrativeCraftMod.LOGGER.error("Failed to init chapter {}", file.getName(), e);
                deserializationResults.add(new DeserializationResult<>(null, true, file.getName()));
            }
        }

        return deserializationResults;
    }

    private String migrateFolderName(File chaptersFolder, File chapterFolder, Chapter chapter) throws IOException {
        if (chapterFolder.getName().equals(chapter.toFileName())) {
            return chapterFolder.getName();
        }
        File normalizedFolder = new File(chaptersFolder, chapter.toFileName());
        if (normalizedFolder.exists()) {
            NarrativeCraftMod.LOGGER.warn(
                    "Chapter directory {} should be named {} but that directory already exists",
                    chapterFolder.getName(),
                    normalizedFolder.getName());
            return chapterFolder.getName();
        }
        Files.move(chapterFolder.toPath(), normalizedFolder.toPath());
        NarrativeCraftMod.LOGGER.info(
                "Renamed chapter directory {} to {}", chapterFolder.getName(), normalizedFolder.getName());
        return normalizedFolder.getName();
    }
}
