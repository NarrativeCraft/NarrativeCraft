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

package fr.loudo.narrativecraft.files.narrrative.scene;

import com.google.gson.Gson;
import fr.loudo.narrativecraft.NarrativeCraftMod;
import fr.loudo.narrativecraft.files.DeserializationResult;
import fr.loudo.narrativecraft.files.NarrativeCraftFileDefault;
import fr.loudo.narrativecraft.files.NarrativeCraftFileEditor;
import fr.loudo.narrativecraft.managers.SceneManager;
import fr.loudo.narrativecraft.narrative.NarrativeEntryEditorRegistry;
import fr.loudo.narrativecraft.narrative.scene.Scene;
import fr.loudo.narrativecraft.narrative.scene.SceneDeserializer;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.Writer;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

public class NarrativeCraftFileScene extends NarrativeCraftFileDefault implements NarrativeCraftFileEditor<Scene> {

    @Override
    public int create(Scene entry) {

        File sceneFile = createDirectory(getScenesFolder(entry.getChapter()), entry.toFileName());
        if (sceneFile == null) {
            NarrativeCraftMod.LOGGER.error("Failed to create scene directory {}", entry.getName());
            return OPERATION_FAILED;
        }

        File animationsFolder = createDirectory(sceneFile, ANIMATIONS_FOLDER_NAME);
        if (animationsFolder == null) {
            NarrativeCraftMod.LOGGER.error("Failed to create animations directory of scene {}", entry.getName());
            return OPERATION_FAILED;
        }

        return edit(entry);
    }

    @Override
    public int edit(Scene entry) {

        File workingFolder = getScenesFolder(entry.getChapter());
        SceneManager sceneManager = entry.getChapter().getSceneManager();
        Scene oldScene = sceneManager.getById(entry.getId());

        try {

            // If user is editing scene
            if (oldScene != null) {
                File oldSceneDirectory = new File(workingFolder, oldScene.toFileName());
                File newSceneDirectory = new File(workingFolder, entry.toFileName());

                // If the user wants to change the scene rank, let's move everything...
                if (oldScene.getRank() != entry.getRank()) {
                    int oldRank = oldScene.getRank();
                    int newRank = entry.getRank();
                    String errorMsg = String.format("Failed to shift scene directory %s", oldScene.getName());

                    if (newRank > oldRank) {
                        // Shift scenes left to free the target slot
                        if (!shiftSceneRange(sceneManager, workingFolder, oldRank + 1, newRank, -1, true)) {
                            NarrativeCraftMod.LOGGER.error(errorMsg);
                            return OPERATION_FAILED;
                        }
                    } else {
                        // Shift scenes right to free the target slot
                        if (!shiftSceneRange(sceneManager, workingFolder, oldRank - 1, newRank, 1, false)) {
                            NarrativeCraftMod.LOGGER.error(errorMsg);
                            return OPERATION_FAILED;
                        }
                    }

                    if (newSceneDirectory.exists()) {
                        NarrativeCraftMod.LOGGER.error(
                                "Failed to rename scene {} because target directory already exists",
                                oldScene.getName());
                        return OPERATION_FAILED;
                    }
                }

                Files.move(oldSceneDirectory.toPath(), newSceneDirectory.toPath());
            }

            File sceneFile = new File(workingFolder, entry.toFileName());
            if (!sceneFile.exists()) {
                NarrativeCraftMod.LOGGER.error("Failed to shift scene directory {}", entry.getName());
                return OPERATION_FAILED;
            }

            File dataFile = createFile(sceneFile, DATA_FILE_NAME);

            try (Writer writer = new BufferedWriter(new FileWriter(dataFile))) {
                writer.write(entry.toRawJson());
            }

        } catch (Exception e) {
            NarrativeCraftMod.LOGGER.error("Failed to write scene data {}", entry.formattedName(), e);
            return OPERATION_FAILED;
        }

        return OPERATION_SUCCESS;
    }

    @Override
    public int delete(Scene entry) {

        File workingFolder = getScenesFolder(entry.getChapter());
        File sceneFile = new File(workingFolder, entry.toFileName());
        if (!sceneFile.exists()) {
            NarrativeCraftMod.LOGGER.error(
                    "Failed to delete scene {} because target directory {} doesn't exists",
                    entry.getName(),
                    entry.toFileName());
            return OPERATION_FAILED;
        }

        SceneManager sceneManager = entry.getChapter().getSceneManager();

        try {
            if (!deleteDirectory(sceneFile)) {
                NarrativeCraftMod.LOGGER.error("Failed to delete scene {}", entry.getName());
                return OPERATION_FAILED;
            }

            // Shift every scene after the deleted one to fill the gap
            int deletedRank = entry.getRank();
            int lastRank = getLastSceneRank(sceneManager);

            if (deletedRank < lastRank) {
                if (!shiftSceneRange(sceneManager, workingFolder, deletedRank + 1, lastRank, -1, true)) {
                    return OPERATION_FAILED;
                }
            }

            return OPERATION_SUCCESS;
        } catch (Exception e) {
            NarrativeCraftMod.LOGGER.error("Failed to delete scene {}", entry.formattedName(), e);
            return OPERATION_FAILED;
        }
    }

    @Override
    public List<DeserializationResult<Scene>> deserialize() {

        List<DeserializationResult<Scene>> deserializationResults = new ArrayList<>();

        File chaptersFolder = getChaptersFolder();
        File[] allContents = chaptersFolder.listFiles();

        if (allContents == null) {
            return null;
        }

        gsonBuilder.registerTypeAdapter(Scene.class, new SceneDeserializer());
        Gson gson = gsonBuilder.create();

        for (File file : allContents) {
            File scenesFolder = new File(file, SCENES_FOLDER_NAME);
            File[] scenes = scenesFolder.listFiles();
            if (scenes == null) {
                continue;
            }

            for (File file1 : scenes) {
                File dataFile = new File(file1, DATA_FILE_NAME);
                try {
                    String content = Files.readString(dataFile.toPath());
                    Scene scene = gson.fromJson(content, Scene.class);
                    if (scene == null) {
                        throw new Exception(String.format(
                                "Scene %s of chapter %s deserialization returned null",
                                file1.getName(), file.getName()));
                    }
                    deserializationResults.add(new DeserializationResult<>(scene, false, file1.getName()));
                } catch (Exception e) {
                    NarrativeCraftMod.LOGGER.error(
                            "Failed to init scene {} of chapter {}", file.getName(), file1.getName(), e);
                    deserializationResults.add(new DeserializationResult<>(null, true, file1.getName()));
                }
            }
        }

        return deserializationResults;
    }

    private boolean shiftSceneRange(
            SceneManager sceneManager,
            File workingFolder,
            int fromInclusive,
            int toInclusive,
            int offset,
            boolean ascending)
            throws Exception {
        if (ascending) {
            for (int i = fromInclusive; i <= toInclusive; i++) {
                if (!shiftSingleScene(sceneManager, workingFolder, i, offset)) {
                    return false;
                }
            }
        } else {
            for (int i = fromInclusive; i >= toInclusive; i--) {
                if (!shiftSingleScene(sceneManager, workingFolder, i, offset)) {
                    return false;
                }
            }
        }

        return true;
    }

    private boolean shiftSingleScene(SceneManager sceneManager, File workingFolder, int currentRank, int offset)
            throws Exception {
        Scene sceneToShift = sceneManager.getByRank(currentRank);
        if (sceneToShift == null) {
            return true;
        }

        File oldFolder = new File(workingFolder, sceneToShift.toFileName());

        int targetRank = currentRank + offset;
        sceneToShift.setRank(targetRank);

        File newFolder = new File(workingFolder, sceneToShift.toFileName());

        if (newFolder.exists()) {
            return false;
        }

        Files.move(oldFolder.toPath(), newFolder.toPath());

        NarrativeEntryEditorRegistry.getInstance().edit(sceneToShift.getId(), sceneToShift.toPayload(), null);

        return true;
    }

    private int getLastSceneRank(SceneManager sceneManager) {
        int rank = 0;
        while (sceneManager.getByRank(rank + 1) != null) {
            rank++;
        }
        return rank;
    }
}
