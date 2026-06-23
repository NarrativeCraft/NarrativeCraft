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

package fr.loudo.narrativecraft.files.narrrative;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import fr.loudo.narrativecraft.NarrativeCraftMod;
import fr.loudo.narrativecraft.files.DeserializationResult;
import fr.loudo.narrativecraft.files.NarrativeCraftFileDefault;
import fr.loudo.narrativecraft.files.NarrativeCraftFileEditor;
import fr.loudo.narrativecraft.files.NarrativeCraftFileUtil;
import fr.loudo.narrativecraft.narrative.NarrativeEntry;
import fr.loudo.narrativecraft.narrative.scene.Scene;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public abstract class AbstractNarrativeCraftFileSceneJsonEntry<T extends NarrativeEntry<?>>
        extends NarrativeCraftFileDefault implements NarrativeCraftFileEditor<T> {

    protected abstract String getSubFolderName();

    protected abstract Scene getScene(T entry);

    protected abstract T getOldEntry(T entry);

    protected abstract void registerDeserializer(GsonBuilder gsonBuilder);

    protected abstract T deserializeEntry(Gson gson, String content) throws Exception;

    protected abstract int writeJson(T entry, File file);

    protected boolean entryHasOwnFolder() {
        return false;
    }

    private File getEntryFolder(T entry) {
        Scene scene = getScene(entry);
        return createDirectory(NarrativeCraftFileUtil.getSceneFolder(scene), getSubFolderName());
    }

    @Override
    public int create(T entry) {
        File folder = getEntryFolder(entry);
        File file;
        if (entryHasOwnFolder()) {
            File entryDirectory = createDirectory(folder, entry.toFileName());
            if (entryDirectory == null) {
                NarrativeCraftMod.LOGGER.error("Failed to create directory {}", entry.getName());
                return OPERATION_FAILED;
            }
            file = createFile(entryDirectory, DATA_FILE_NAME);
        } else {
            file = createFile(folder, entry.toFileName());
        }
        if (file == null) {
            NarrativeCraftMod.LOGGER.error("Failed to create file {}", entry.getName());
            return OPERATION_FAILED;
        }
        return writeJson(entry, file);
    }

    @Override
    public int edit(T entry) {
        File folder = getEntryFolder(entry);
        T oldEntry = getOldEntry(entry);
        try {
            if (oldEntry != null && !oldEntry.toFileName().equals(entry.toFileName())) {
                File oldTarget = new File(folder, oldEntry.toFileName());
                File newTarget = new File(folder, entry.toFileName());
                Files.move(oldTarget.toPath(), newTarget.toPath());
            }
            if (entryHasOwnFolder()) {
                return writeJson(entry, new File(new File(folder, entry.toFileName()), DATA_FILE_NAME));
            }
            return writeJson(entry, new File(folder, entry.toFileName()));
        } catch (IOException e) {
            NarrativeCraftMod.LOGGER.error("Failed to edit {}", entry.getName(), e);
            return OPERATION_FAILED;
        }
    }

    @Override
    public int delete(T entry) {
        File target = new File(getEntryFolder(entry), entry.toFileName());
        if (!target.exists()) {
            NarrativeCraftMod.LOGGER.error("Failed to delete {} because file doesn't exist", entry.getName());
            return OPERATION_FAILED;
        }
        boolean deleted = entryHasOwnFolder() ? deleteDirectory(target) : target.delete();
        if (!deleted) {
            NarrativeCraftMod.LOGGER.error("Failed to delete {}", entry.getName());
            return OPERATION_FAILED;
        }
        return OPERATION_SUCCESS;
    }

    @Override
    public List<DeserializationResult<T>> deserialize() {
        List<DeserializationResult<T>> results = new ArrayList<>();

        migrateLooseEntries();

        registerDeserializer(gsonBuilder);
        Gson gson = gsonBuilder.create();

        forEachEntryFolder(entryFolder -> {
            File[] entryFiles = entryFolder.listFiles();
            if (entryFiles == null) return;

            for (File entryFile : entryFiles) {
                File dataFile;
                if (entryHasOwnFolder()) {
                    if (!entryFile.isDirectory()) continue;
                    dataFile = new File(entryFile, DATA_FILE_NAME);
                } else {
                    dataFile = entryFile;
                }
                try {
                    String content = Files.readString(dataFile.toPath());
                    T entry = deserializeEntry(gson, content);
                    if (entry == null) {
                        throw new Exception(String.format("Deserialization of %s returned null", entryFile.getName()));
                    }
                    results.add(new DeserializationResult<>(entry, false, entryFile.getName()));
                } catch (Exception e) {
                    NarrativeCraftMod.LOGGER.error("Failed to init {}", entryFile.getName(), e);
                    results.add(new DeserializationResult<>(null, true, entryFile.getName()));
                }
            }
        });

        return results;
    }

    private void migrateLooseEntries() {
        if (!entryHasOwnFolder()) return;

        forEachEntryFolder(entryFolder -> {
            File[] children = entryFolder.listFiles();
            if (children == null) return;

            for (File child : children) {
                if (!child.isFile() || !child.getName().endsWith(EXTENSION_DATA_FILE)) continue;

                String folderName =
                        child.getName().substring(0, child.getName().length() - EXTENSION_DATA_FILE.length());
                File entryDirectory = new File(entryFolder, folderName);
                if (entryDirectory.exists() && !entryDirectory.isDirectory()) {
                    NarrativeCraftMod.LOGGER.warn(
                            "Skipping migration of {} because {} exists and is not a directory",
                            child.getName(),
                            entryDirectory.getName());
                    continue;
                }
                File dataFile = new File(entryDirectory, DATA_FILE_NAME);
                if (dataFile.exists()) {
                    NarrativeCraftMod.LOGGER.warn(
                            "Skipping migration of {} because {} already exists", child.getName(), dataFile.getPath());
                    continue;
                }
                if (!entryDirectory.exists() && !entryDirectory.mkdir()) {
                    NarrativeCraftMod.LOGGER.error("Failed to create migration directory {}", entryDirectory.getPath());
                    continue;
                }
                try {
                    Files.move(child.toPath(), dataFile.toPath());
                    NarrativeCraftMod.LOGGER.info("Migrated {} to {}", child.getName(), dataFile.getPath());
                } catch (IOException e) {
                    NarrativeCraftMod.LOGGER.error("Failed to migrate {}", child.getName(), e);
                }
            }
        });
    }

    private void forEachEntryFolder(Consumer<File> consumer) {
        File chaptersFolder = NarrativeCraftFileUtil.getChaptersFolder();
        File[] chapterDirs = chaptersFolder.listFiles();
        if (chapterDirs == null) return;

        for (File chapterDir : chapterDirs) {
            File scenesFolder = new File(chapterDir, SCENES_FOLDER_NAME);
            File[] sceneDirs = scenesFolder.listFiles();
            if (sceneDirs == null) continue;

            for (File sceneDir : sceneDirs) {
                File entryFolder = new File(sceneDir, getSubFolderName());
                if (!entryFolder.exists()) continue;
                consumer.accept(entryFolder);
            }
        }
    }
}
