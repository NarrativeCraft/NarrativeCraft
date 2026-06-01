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

package fr.loudo.narrativecraft.files.narrrative.subscene;

import com.google.gson.Gson;
import fr.loudo.narrativecraft.NarrativeCraftMod;
import fr.loudo.narrativecraft.files.DeserializationResult;
import fr.loudo.narrativecraft.files.NarrativeCraftFileDefault;
import fr.loudo.narrativecraft.files.NarrativeCraftFileEditor;
import fr.loudo.narrativecraft.files.NarrativeCraftFileUtil;
import fr.loudo.narrativecraft.narrative.subscene.Subscene;
import fr.loudo.narrativecraft.narrative.subscene.SubsceneDeserializer;
import fr.loudo.narrativecraft.narrative.subscene.SubsceneSerializer;
import java.io.*;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

public class NarrativeCraftFileSubscene extends NarrativeCraftFileDefault
        implements NarrativeCraftFileEditor<Subscene> {

    @Override
    public int create(Subscene entry) {
        File subsceneFolder = NarrativeCraftFileUtil.getSubscenesFolder(entry.getScene());
        File subsceneFile = createFile(subsceneFolder, entry.toFileName());
        if (subsceneFile == null) {
            NarrativeCraftMod.LOGGER.error("Failed to create subscene file {}", entry.getName());
            return OPERATION_FAILED;
        }
        return writeJson(entry, subsceneFile);
    }

    @Override
    public int edit(Subscene entry) {
        File subsceneFolder = NarrativeCraftFileUtil.getSubscenesFolder(entry.getScene());
        Subscene oldSubscene = entry.getScene().getSubsceneManager().getById(entry.getId());

        try {
            if (oldSubscene != null && !oldSubscene.toFileName().equals(entry.toFileName())) {
                File oldFile = new File(subsceneFolder, oldSubscene.toFileName());
                File newFile = new File(subsceneFolder, entry.toFileName());
                Files.move(oldFile.toPath(), newFile.toPath());
            }

            File subsceneFile = new File(subsceneFolder, entry.toFileName());
            return writeJson(entry, subsceneFile);

        } catch (IOException e) {
            NarrativeCraftMod.LOGGER.error("Failed to edit subscene {}", entry.getName(), e);
            return OPERATION_FAILED;
        }
    }

    @Override
    public int delete(Subscene entry) {
        File subsceneFolder = NarrativeCraftFileUtil.getSubscenesFolder(entry.getScene());
        File subsceneFile = new File(subsceneFolder, entry.toFileName());

        if (!subsceneFile.exists()) {
            NarrativeCraftMod.LOGGER.error("Failed to delete subscene {} because file doesn't exist", entry.getName());
            return OPERATION_FAILED;
        }

        if (!subsceneFile.delete()) {
            NarrativeCraftMod.LOGGER.error("Failed to delete subscene {}", entry.getName());
            return OPERATION_FAILED;
        }

        return OPERATION_SUCCESS;
    }

    @Override
    public List<DeserializationResult<Subscene>> deserialize() {
        List<DeserializationResult<Subscene>> results = new ArrayList<>();

        gsonBuilder.registerTypeAdapter(Subscene.class, new SubsceneDeserializer());
        Gson gson = gsonBuilder.create();

        File chaptersFolder = NarrativeCraftFileUtil.getChaptersFolder();
        File[] chapterDirs = chaptersFolder.listFiles();
        if (chapterDirs == null) return results;

        for (File chapterDir : chapterDirs) {
            File scenesFolder = new File(chapterDir, SCENES_FOLDER_NAME);
            File[] sceneDirs = scenesFolder.listFiles();
            if (sceneDirs == null) continue;

            for (File sceneDir : sceneDirs) {
                File subsceneFolder = new File(sceneDir, SUBSCENES_FOLDER_NAME);
                File[] subsceneFiles = subsceneFolder.listFiles();
                if (subsceneFiles == null) continue;

                for (File subsceneFile : subsceneFiles) {
                    try {
                        String content = Files.readString(subsceneFile.toPath());
                        Subscene subscene = gson.fromJson(content, Subscene.class);
                        if (subscene == null) {
                            throw new Exception(
                                    String.format("Subscene %s deserialization returned null", subsceneFile.getName()));
                        }
                        results.add(new DeserializationResult<>(subscene, false, subsceneFile.getName()));
                    } catch (Exception e) {
                        NarrativeCraftMod.LOGGER.error("Failed to init subscene {}", subsceneFile.getName(), e);
                        results.add(new DeserializationResult<>(null, true, subsceneFile.getName()));
                    }
                }
            }
        }

        return results;
    }

    private int writeJson(Subscene entry, File file) {
        Gson gson = gsonBuilder
                .registerTypeAdapter(Subscene.class, new SubsceneSerializer())
                .create();
        try (Writer writer = new BufferedWriter(new FileWriter(file))) {
            gson.toJson(entry, writer);
            return OPERATION_SUCCESS;
        } catch (IOException e) {
            NarrativeCraftMod.LOGGER.error("Failed to write subscene data {}", entry.getName(), e);
            return OPERATION_FAILED;
        }
    }
}
