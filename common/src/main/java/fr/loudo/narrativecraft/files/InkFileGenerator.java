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
import fr.loudo.narrativecraft.narrative.scene.Scene;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

public class InkFileGenerator {

    public static void generateChapterInkFile(Chapter chapter) {
        File chapterDir = new File(NarrativeCraftFileUtil.getChaptersFolder(), chapter.toFileName());
        File inkFile = new File(chapterDir, chapterInkFileName(chapter));
        try {
            Files.writeString(inkFile.toPath(), "=== " + chapterKnotName(chapter) + " ===\n");
        } catch (IOException e) {
            NarrativeCraftMod.LOGGER.error("Failed to write chapter ink file for {}", chapter.getName(), e);
        }
    }

    public static void generateSceneInkFile(Scene scene) {
        File inkFile = new File(NarrativeCraftFileUtil.getSceneFolder(scene), sceneInkFileName(scene));
        try {
            String content = "=== " + sceneKnotName(scene) + " ===\n# on_enter\n-> END\n";
            Files.writeString(inkFile.toPath(), content);
        } catch (IOException e) {
            NarrativeCraftMod.LOGGER.error("Failed to write scene ink file for {}", scene.getName(), e);
        }
    }

    public static void renameChapterInkFile(File chapterDir, int oldIndex, int newIndex) {
        File oldFile = new File(chapterDir, "chapter_" + oldIndex + NarrativeCraftFileDefault.EXTENSION_SCRIPT_FILE);
        if (oldFile.exists()) {
            oldFile.delete();
        }
        File newFile = new File(chapterDir, "chapter_" + newIndex + NarrativeCraftFileDefault.EXTENSION_SCRIPT_FILE);
        try {
            Files.writeString(newFile.toPath(), "=== chapter_" + newIndex + " ===\n");
        } catch (IOException e) {
            NarrativeCraftMod.LOGGER.error("Failed to rename chapter ink file from {} to {}", oldIndex, newIndex, e);
        }
    }

    public static void regenerateMainInk() {
        File mainDirectory = NarrativeCraftMod.getInstance().getFile().getInit().getMainDirectory();
        File chaptersFolder = NarrativeCraftFileUtil.getChaptersFolder();
        File mainInkFile = getMainFile();

        File[] chapterDirs = chaptersFolder.listFiles(File::isDirectory);
        if (chapterDirs == null) chapterDirs = new File[0];

        Arrays.sort(chapterDirs, Comparator.comparingInt(dir -> extractLeadingInt(dir.getName())));

        List<String> includes = new ArrayList<>();

        for (File chapterDir : chapterDirs) {
            File[] chapterInkFiles =
                    chapterDir.listFiles(f -> f.isFile() && f.getName().matches("chapter_\\d+\\.ink"));
            if (chapterInkFiles != null) {
                Arrays.sort(chapterInkFiles, Comparator.comparing(File::getName));
                for (File inkFile : chapterInkFiles) {
                    includes.add(toRelativePath(mainDirectory, inkFile));
                }
            }

            File scenesDir = new File(chapterDir, NarrativeCraftFileDefault.SCENES_FOLDER_NAME);
            if (!scenesDir.isDirectory()) continue;

            File[] sceneDirs = scenesDir.listFiles(File::isDirectory);
            if (sceneDirs == null) continue;

            Arrays.sort(sceneDirs, Comparator.comparingInt(dir -> extractSceneRank(dir.getName())));

            for (File sceneDir : sceneDirs) {
                File[] sceneInkFiles = sceneDir.listFiles(f -> f.isFile()
                        && f.getName().endsWith(NarrativeCraftFileDefault.EXTENSION_SCRIPT_FILE)
                        && !f.getName().matches("chapter_\\d+\\.ink"));
                if (sceneInkFiles == null) continue;
                for (File inkFile : sceneInkFiles) {
                    includes.add(toRelativePath(mainDirectory, inkFile));
                }
            }
        }

        StringBuilder sb = new StringBuilder();
        for (String include : includes) {
            sb.append("INCLUDE ").append(include).append("\n");
        }
        sb.append("\n-> chapter_1\n");

        try {
            Files.writeString(mainInkFile.toPath(), sb.toString());
        } catch (IOException e) {
            NarrativeCraftMod.LOGGER.error("Failed to regenerate main.ink", e);
        }
    }

    public static File getMainFile() {
        File mainDirectory = NarrativeCraftMod.getInstance().getFile().getInit().getMainDirectory();
        return new File(mainDirectory, NarrativeCraftFileInit.MAIN_INK_NAME);
    }

    private static String toRelativePath(File base, File target) {
        return base.toPath().relativize(target.toPath()).toString().replace(File.separatorChar, '/');
    }

    private static int extractLeadingInt(String name) {
        int underscore = name.indexOf('_');
        if (underscore > 0) {
            try {
                return Integer.parseInt(name.substring(0, underscore));
            } catch (NumberFormatException ignored) {
            }
        }
        return Integer.MAX_VALUE;
    }

    private static int extractSceneRank(String name) {
        String[] parts = name.split("_", 3);
        if (parts.length >= 2) {
            try {
                return Integer.parseInt(parts[1]);
            } catch (NumberFormatException ignored) {
            }
        }
        return Integer.MAX_VALUE;
    }

    private static String chapterInkFileName(Chapter chapter) {
        return "chapter_" + chapter.getChapterIndex() + NarrativeCraftFileDefault.EXTENSION_SCRIPT_FILE;
    }

    private static String sceneInkFileName(Scene scene) {
        return scene.getName().toLowerCase(Locale.ROOT).replace(' ', '_')
                + NarrativeCraftFileDefault.EXTENSION_SCRIPT_FILE;
    }

    private static String chapterKnotName(Chapter chapter) {
        return "chapter_" + chapter.getChapterIndex();
    }

    private static String sceneKnotName(Scene scene) {
        return "chapter_" + scene.getChapterIndex() + "_"
                + scene.getName().toLowerCase(Locale.ROOT).replace(' ', '_');
    }
}
