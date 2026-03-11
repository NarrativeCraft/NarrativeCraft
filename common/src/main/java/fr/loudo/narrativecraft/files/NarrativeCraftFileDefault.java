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

import com.google.gson.GsonBuilder;
import fr.loudo.narrativecraft.NarrativeCraftMod;
import fr.loudo.narrativecraft.narrative.chapter.Chapter;
import java.io.*;

public class NarrativeCraftFileDefault {

    public static final String EXTENSION_SCRIPT_FILE = ".ink";
    public static final String EXTENSION_DATA_FILE = ".json";
    public static final String DATA_FILE_NAME = "data" + EXTENSION_DATA_FILE;
    public static final String ANIMATIONS_FOLDER_NAME = "animations";
    public static final String SCENES_FOLDER_NAME = "scenes";

    protected final GsonBuilder gsonBuilder = new GsonBuilder();
    protected final String DIRECTORY_NAME = NarrativeCraftMod.MOD_ID;

    protected File createDirectory(File parent, String name) {
        File directory = new File(parent, name);
        if (!directory.exists()) {
            if (!directory.mkdir()) {
                NarrativeCraftMod.LOGGER.error("Failed to create directory {}", name);
                return null;
            }
        }
        return directory;
    }

    protected File createFile(File parent, String name) {
        File file = new File(parent, name);
        if (!file.exists()) {
            try {
                if (!file.createNewFile()) {
                    NarrativeCraftMod.LOGGER.error("Failed to create file {}!", file.getAbsolutePath());
                    return null;
                }
            } catch (IOException e) {
                NarrativeCraftMod.LOGGER.error("Failed to create file {}", file.getAbsolutePath(), e);
                return null;
            }
        }
        return file;
    }

    protected boolean deleteDirectory(File directoryToBeDeleted) {
        File[] allContents = directoryToBeDeleted.listFiles();
        if (allContents != null) {
            for (File file : allContents) {
                deleteDirectory(file);
            }
        }
        return directoryToBeDeleted.delete();
    }

    protected File getChaptersFolder() {
        return NarrativeCraftMod.getInstance().getFile().getInit().getChaptersDirectory();
    }

    protected File getScenesFolder(Chapter chapter) {
        File chaptersFolder = getChaptersFolder();
        File chapterFolder = new File(chaptersFolder, chapter.toFileName());
        return new File(chapterFolder, SCENES_FOLDER_NAME);
    }
}
