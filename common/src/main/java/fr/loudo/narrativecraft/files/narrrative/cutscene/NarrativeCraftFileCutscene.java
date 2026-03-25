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

package fr.loudo.narrativecraft.files.narrrative.cutscene;

import com.google.gson.Gson;
import fr.loudo.narrativecraft.NarrativeCraftMod;
import fr.loudo.narrativecraft.files.DeserializationResult;
import fr.loudo.narrativecraft.files.NarrativeCraftFileDefault;
import fr.loudo.narrativecraft.files.NarrativeCraftFileEditor;
import fr.loudo.narrativecraft.files.NarrativeCraftFileUtil;
import fr.loudo.narrativecraft.narrative.cutscene.Cutscene;
import fr.loudo.narrativecraft.narrative.cutscene.CutsceneDeserializer;
import java.io.*;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

public class NarrativeCraftFileCutscene extends NarrativeCraftFileDefault
        implements NarrativeCraftFileEditor<Cutscene> {

    @Override
    public int create(Cutscene entry) {
        File cutsceneFolder = NarrativeCraftFileUtil.getCutscenesFolder(entry.getScene());
        File cutsceneFile = createFile(cutsceneFolder, entry.toFileName());
        if (cutsceneFile == null) {
            NarrativeCraftMod.LOGGER.error("Failed to create cutscene file {}", entry.getName());
            return OPERATION_FAILED;
        }
        return writeJson(entry, cutsceneFile);
    }

    @Override
    public int edit(Cutscene entry) {
        File cutsceneFolder = NarrativeCraftFileUtil.getCutscenesFolder(entry.getScene());
        Cutscene oldCutscene = entry.getScene().getCutsceneManager().getById(entry.getId());

        try {
            if (oldCutscene != null && !oldCutscene.toFileName().equals(entry.toFileName())) {
                File oldFile = new File(cutsceneFolder, oldCutscene.toFileName());
                File newFile = new File(cutsceneFolder, entry.toFileName());
                Files.move(oldFile.toPath(), newFile.toPath());
            }

            File cutsceneFile = new File(cutsceneFolder, entry.toFileName());
            return writeJson(entry, cutsceneFile);

        } catch (IOException e) {
            NarrativeCraftMod.LOGGER.error("Failed to edit cutscene {}", entry.getName(), e);
            return OPERATION_FAILED;
        }
    }

    @Override
    public int delete(Cutscene entry) {
        File cutsceneFolder = NarrativeCraftFileUtil.getCutscenesFolder(entry.getScene());
        File cutsceneFile = new File(cutsceneFolder, entry.toFileName());

        if (!cutsceneFile.exists()) {
            NarrativeCraftMod.LOGGER.error("Failed to delete cutscene {} because file doesn't exist", entry.getName());
            return OPERATION_FAILED;
        }

        if (!cutsceneFile.delete()) {
            NarrativeCraftMod.LOGGER.error("Failed to delete cutscene {}", entry.getName());
            return OPERATION_FAILED;
        }

        return OPERATION_SUCCESS;
    }

    @Override
    public List<DeserializationResult<Cutscene>> deserialize() {
        List<DeserializationResult<Cutscene>> results = new ArrayList<>();

        gsonBuilder.registerTypeAdapter(Cutscene.class, new CutsceneDeserializer());
        Gson gson = gsonBuilder.create();

        java.io.File chaptersFolder = NarrativeCraftFileUtil.getChaptersFolder();
        java.io.File[] chapterDirs = chaptersFolder.listFiles();
        if (chapterDirs == null) return results;

        for (java.io.File chapterDir : chapterDirs) {
            java.io.File scenesFolder = new java.io.File(chapterDir, SCENES_FOLDER_NAME);
            java.io.File[] sceneDirs = scenesFolder.listFiles();
            if (sceneDirs == null) continue;

            for (java.io.File sceneDir : sceneDirs) {
                java.io.File cutsceneFolder = new java.io.File(sceneDir, CUTSCENES_FOLDER_NAME);
                java.io.File[] cutsceneFiles = cutsceneFolder.listFiles();
                if (cutsceneFiles == null) continue;

                for (java.io.File cutsceneFile : cutsceneFiles) {
                    try {
                        String content = Files.readString(cutsceneFile.toPath());
                        Cutscene cutscene = gson.fromJson(content, Cutscene.class);
                        if (cutscene == null) {
                            throw new Exception(
                                    String.format("Cutscene %s deserialization returned null", cutsceneFile.getName()));
                        }
                        results.add(new DeserializationResult<>(cutscene, false, cutsceneFile.getName()));
                    } catch (Exception e) {
                        NarrativeCraftMod.LOGGER.error("Failed to init cutscene {}", cutsceneFile.getName(), e);
                        results.add(new DeserializationResult<>(null, true, cutsceneFile.getName()));
                    }
                }
            }
        }

        return results;
    }

    private int writeJson(Cutscene entry, File file) {
        try (Writer writer = new BufferedWriter(new FileWriter(file))) {
            writer.write(entry.toRawJson());
            return OPERATION_SUCCESS;
        } catch (IOException e) {
            NarrativeCraftMod.LOGGER.error("Failed to write cutscene data {}", entry.getName(), e);
            return OPERATION_FAILED;
        }
    }
}
