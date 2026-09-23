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

package fr.loudo.narrativecraft.files.narrrative.scene;

import com.mojang.serialization.Codec;
import fr.loudo.narrativecraft.NarrativeCraftMod;
import fr.loudo.narrativecraft.files.ChildEntryFileEditor;
import fr.loudo.narrativecraft.files.DeserializationResult;
import fr.loudo.narrativecraft.files.EntryChange;
import fr.loudo.narrativecraft.files.FileTransaction;
import fr.loudo.narrativecraft.files.InkFileGenerator;
import fr.loudo.narrativecraft.files.JsonCodecFile;
import fr.loudo.narrativecraft.files.NarrativeCraftFileDefault;
import fr.loudo.narrativecraft.files.NarrativeCraftFileEditor;
import fr.loudo.narrativecraft.files.NarrativeCraftFileUtil;
import fr.loudo.narrativecraft.files.NarrativeCraftFileWriter;
import fr.loudo.narrativecraft.files.RankedNarrativeCraftFileEditor;
import fr.loudo.narrativecraft.narrative.OperationResult;
import fr.loudo.narrativecraft.narrative.chapter.Chapter;
import fr.loudo.narrativecraft.narrative.scene.Scene;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class NarrativeCraftFileScene extends NarrativeCraftFileDefault
        implements RankedNarrativeCraftFileEditor<Scene>, ChildEntryFileEditor<Scene, Chapter> {

    private static final List<String> SCENE_SUB_FOLDERS = List.of(
            ANIMATIONS_FOLDER_NAME,
            SUBSCENES_FOLDER_NAME,
            CUTSCENES_FOLDER_NAME,
            CAMERA_ANGLES_FOLDER_NAME,
            NPC_FOLDER_NAME,
            INTERACTIONS_FOLDER_NAME);

    @Override
    public OperationResult create(Scene entry) {
        FileTransaction transaction = new FileTransaction();
        try {
            File sceneFolder = NarrativeCraftFileUtil.getSceneFolder(entry);
            if (sceneFolder.exists()) {
                throw new IOException("Scene directory " + sceneFolder + " already exists");
            }
            for (String subFolder : SCENE_SUB_FOLDERS) {
                transaction.createDirectories(new File(sceneFolder, subFolder));
            }
            writeData(transaction, sceneFolder, entry);
            InkFileGenerator.writeSceneInkFile(transaction, sceneFolder, entry);
            transaction.commit();
        } catch (IOException e) {
            transaction.rollback();
            NarrativeCraftMod.LOGGER.error("Failed to create scene {}", entry.getName(), e);
            return NarrativeCraftFileEditor.storageFailure(entry);
        }
        InkFileGenerator.regenerateMainInk();
        return OperationResult.success();
    }

    @Override
    public OperationResult editAll(List<EntryChange<Scene>> changes) {
        FileTransaction transaction = new FileTransaction();
        try {
            applyChanges(transaction, changes);
            transaction.commit();
        } catch (IOException e) {
            transaction.rollback();
            NarrativeCraftMod.LOGGER.error("Failed to edit scenes", e);
            return NarrativeCraftFileEditor.storageFailure(changes.getFirst().existing());
        }
        InkFileGenerator.regenerateMainInk();
        return OperationResult.success();
    }

    @Override
    public OperationResult deleteAndShift(Scene entry, List<EntryChange<Scene>> shiftedEntries) {
        FileTransaction transaction = new FileTransaction();
        try {
            File sceneFolder = NarrativeCraftFileUtil.getSceneFolder(entry);
            if (!sceneFolder.exists()) {
                throw new IOException("Scene directory " + sceneFolder + " does not exist");
            }
            transaction.delete(sceneFolder);
            applyChanges(transaction, shiftedEntries);
            transaction.commit();
        } catch (IOException e) {
            transaction.rollback();
            NarrativeCraftMod.LOGGER.error("Failed to delete scene {}", entry.getName(), e);
            return NarrativeCraftFileEditor.storageFailure(entry);
        }
        InkFileGenerator.regenerateMainInk();
        return OperationResult.success();
    }

    private void applyChanges(FileTransaction transaction, List<EntryChange<Scene>> changes) throws IOException {
        List<FileTransaction.Move> sceneMoves = new ArrayList<>();
        for (EntryChange<Scene> change : changes) {
            sceneMoves.add(new FileTransaction.Move(
                    NarrativeCraftFileUtil.getSceneFolder(change.existing()),
                    NarrativeCraftFileUtil.getSceneFolder(change.updated())));
        }
        transaction.moveAll(sceneMoves);

        for (EntryChange<Scene> change : changes) {
            File sceneFolder = NarrativeCraftFileUtil.getSceneFolder(change.updated());
            if (!change.existing().getNormalizedName().equals(change.updated().getNormalizedName())) {
                InkFileGenerator.renameSceneInkFile(transaction, sceneFolder, change.existing(), change.updated());
            }
            writeData(transaction, sceneFolder, change.updated());
        }
    }

    private void writeData(FileTransaction transaction, File sceneFolder, Scene scene) throws IOException {
        JsonCodecFile.write(transaction, new File(sceneFolder, DATA_FILE_NAME), Scene.codec(scene.getChapter()), scene);
    }

    @Override
    public List<DeserializationResult<Scene>> load(Chapter chapter) {
        List<DeserializationResult<Scene>> deserializationResults = new ArrayList<>();

        File[] sceneFolders = NarrativeCraftFileUtil.getScenesFolder(chapter).listFiles(File::isDirectory);
        if (sceneFolders == null) {
            return deserializationResults;
        }

        Codec<Scene> codec = Scene.codec(chapter);
        for (File sceneFolder : sceneFolders) {
            if (NarrativeCraftFileWriter.isTemporary(sceneFolder)) continue;
            try {
                Scene scene = JsonCodecFile.read(new File(sceneFolder, DATA_FILE_NAME), codec);
                String folderName = migrateFolderName(sceneFolder, scene.toFileName());
                deserializationResults.add(new DeserializationResult<>(scene, false, folderName));
            } catch (IOException e) {
                NarrativeCraftMod.LOGGER.error(
                        "Failed to init scene {} of chapter {}", sceneFolder.getName(), chapter.getName(), e);
                deserializationResults.add(new DeserializationResult<>(null, true, sceneFolder.getName()));
            }
        }

        return deserializationResults;
    }
}
