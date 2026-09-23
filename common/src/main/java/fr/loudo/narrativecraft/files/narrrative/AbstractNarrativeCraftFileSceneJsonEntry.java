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

import com.mojang.serialization.Codec;
import fr.loudo.narrativecraft.NarrativeCraftMod;
import fr.loudo.narrativecraft.files.ChildEntryFileEditor;
import fr.loudo.narrativecraft.files.DeserializationResult;
import fr.loudo.narrativecraft.files.FileTransaction;
import fr.loudo.narrativecraft.files.JsonCodecFile;
import fr.loudo.narrativecraft.files.NarrativeCraftFileDefault;
import fr.loudo.narrativecraft.files.NarrativeCraftFileEditor;
import fr.loudo.narrativecraft.files.NarrativeCraftFileUtil;
import fr.loudo.narrativecraft.files.NarrativeCraftFileWriter;
import fr.loudo.narrativecraft.narrative.NarrativeEntry;
import fr.loudo.narrativecraft.narrative.OperationResult;
import fr.loudo.narrativecraft.narrative.scene.Scene;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public abstract class AbstractNarrativeCraftFileSceneJsonEntry<T extends NarrativeEntry<?>>
        extends NarrativeCraftFileDefault implements ChildEntryFileEditor<T, Scene> {

    private final Function<Scene, Codec<T>> codecFactory;

    protected AbstractNarrativeCraftFileSceneJsonEntry(Function<Scene, Codec<T>> codecFactory) {
        this.codecFactory = codecFactory;
    }

    protected abstract String getSubFolderName();

    protected abstract Scene getScene(T entry);

    protected boolean entryHasOwnFolder() {
        return false;
    }

    private File getEntriesFolder(Scene scene) {
        return new File(NarrativeCraftFileUtil.getSceneFolder(scene), getSubFolderName());
    }

    private File getEntryTarget(T entry) {
        return new File(getEntriesFolder(getScene(entry)), entry.toFileName());
    }

    private File getDataFile(File entryTarget) {
        return entryHasOwnFolder() ? new File(entryTarget, DATA_FILE_NAME) : entryTarget;
    }

    @Override
    public OperationResult create(T entry) {
        FileTransaction transaction = new FileTransaction();
        try {
            File target = getEntryTarget(entry);
            if (target.exists()) {
                throw new IOException(target + " already exists");
            }
            transaction.createDirectories(entryHasOwnFolder() ? target : getEntriesFolder(getScene(entry)));
            writeData(transaction, entry);
            transaction.commit();
        } catch (IOException e) {
            transaction.rollback();
            NarrativeCraftMod.LOGGER.error("Failed to create {}", entry.getName(), e);
            return NarrativeCraftFileEditor.storageFailure(entry);
        }
        return OperationResult.success();
    }

    @Override
    public OperationResult edit(T existing, T updated) {
        FileTransaction transaction = new FileTransaction();
        try {
            transaction.move(getEntryTarget(existing), getEntryTarget(updated));
            writeData(transaction, updated);
            transaction.commit();
        } catch (IOException e) {
            transaction.rollback();
            NarrativeCraftMod.LOGGER.error("Failed to edit {}", existing.getName(), e);
            return NarrativeCraftFileEditor.storageFailure(existing);
        }
        return OperationResult.success();
    }

    @Override
    public OperationResult delete(T entry) {
        FileTransaction transaction = new FileTransaction();
        try {
            File target = getEntryTarget(entry);
            if (!target.exists()) {
                throw new IOException(target + " does not exist");
            }
            transaction.delete(target);
            transaction.commit();
        } catch (IOException e) {
            transaction.rollback();
            NarrativeCraftMod.LOGGER.error("Failed to delete {}", entry.getName(), e);
            return NarrativeCraftFileEditor.storageFailure(entry);
        }
        return OperationResult.success();
    }

    private void writeData(FileTransaction transaction, T entry) throws IOException {
        JsonCodecFile.write(
                transaction, getDataFile(getEntryTarget(entry)), codecFactory.apply(getScene(entry)), entry);
    }

    @Override
    public List<DeserializationResult<T>> load(Scene scene) {
        List<DeserializationResult<T>> results = new ArrayList<>();
        File entriesFolder = getEntriesFolder(scene);
        migrateLooseEntries(entriesFolder);

        File[] entryFiles = entriesFolder.listFiles();
        if (entryFiles == null) return results;

        Codec<T> codec = codecFactory.apply(scene);
        for (File entryFile : entryFiles) {
            if (NarrativeCraftFileWriter.isTemporary(entryFile)) continue;
            if (entryHasOwnFolder() != entryFile.isDirectory()) continue;
            if (!entryHasOwnFolder() && !entryFile.getName().endsWith(EXTENSION_DATA_FILE)) continue;
            try {
                T entry = JsonCodecFile.read(getDataFile(entryFile), codec);
                results.add(new DeserializationResult<>(entry, false, entryFile.getName()));
            } catch (IOException e) {
                NarrativeCraftMod.LOGGER.error("Failed to init {}", entryFile.getName(), e);
                results.add(new DeserializationResult<>(null, true, entryFile.getName()));
            }
        }
        return results;
    }

    private void migrateLooseEntries(File entriesFolder) {
        if (!entryHasOwnFolder()) return;

        File[] children = entriesFolder.listFiles();
        if (children == null) return;

        for (File child : children) {
            if (!child.isFile() || !child.getName().endsWith(EXTENSION_DATA_FILE)) continue;

            String folderName = child.getName().substring(0, child.getName().length() - EXTENSION_DATA_FILE.length());
            File entryDirectory = new File(entriesFolder, folderName);
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
    }
}
