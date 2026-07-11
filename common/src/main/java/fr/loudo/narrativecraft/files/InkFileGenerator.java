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
import java.util.regex.Pattern;

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

    public static void renameSceneInkFile(Scene oldScene, Scene newScene) {
        File sceneDir = NarrativeCraftFileUtil.getSceneFolder(newScene);
        File oldInkFile = new File(sceneDir, sceneInkFileName(oldScene));
        File newInkFile = new File(sceneDir, sceneInkFileName(newScene));

        // No previous ink file to preserve: just generate a fresh one
        if (!oldInkFile.exists()) {
            generateSceneInkFile(newScene);
            return;
        }

        try {
            String content = Files.readString(oldInkFile.toPath());
            String oldKnot = sceneKnotName(oldScene);
            String newKnot = sceneKnotName(newScene);

            // Update the knot name (header and any self-divert) while keeping the user's content
            content = content.replaceAll("\\b" + Pattern.quote(oldKnot) + "\\b", newKnot);

            Files.writeString(newInkFile.toPath(), content);
            if (!oldInkFile.equals(newInkFile)) {
                oldInkFile.delete();
            }
        } catch (IOException e) {
            NarrativeCraftMod.LOGGER.error("Failed to rename scene ink file for {}", newScene.getName(), e);
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
        writeMainInk(getMainDirectory());
    }

    public static void regenerateLocaleMainInk(String locale) {
        writeMainInk(NarrativeCraftMod.getInstance().getFile().getInit().getLocaleDirectory(locale));
    }

    public static List<File> collectStoryInkFiles() {
        File chaptersFolder = NarrativeCraftFileUtil.getChaptersFolder();
        List<File> inkFiles = new ArrayList<>();

        File[] chapterDirs = chaptersFolder.listFiles(File::isDirectory);
        if (chapterDirs == null) return inkFiles;

        Arrays.sort(chapterDirs, Comparator.comparingInt(dir -> extractLeadingInt(dir.getName())));

        for (File chapterDir : chapterDirs) {
            File[] chapterInkFiles =
                    chapterDir.listFiles(f -> f.isFile() && f.getName().matches("chapter_\\d+\\.ink"));
            if (chapterInkFiles != null) {
                Arrays.sort(chapterInkFiles, Comparator.comparing(File::getName));
                inkFiles.addAll(Arrays.asList(chapterInkFiles));
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
                Arrays.sort(sceneInkFiles, Comparator.comparing(File::getName));
                inkFiles.addAll(Arrays.asList(sceneInkFiles));
            }
        }

        return inkFiles;
    }

    private static void writeMainInk(File inkRoot) {
        File mainDirectory = getMainDirectory();

        StringBuilder builder = new StringBuilder();
        builder.append("INCLUDE ").append(NarrativeCraftFileInit.VARS_INK_NAME).append("\n");
        builder.append("INCLUDE ")
                .append(NarrativeCraftFileInit.FUNCTIONS_INK_NAME)
                .append("\n");
        for (File storyInkFile : collectStoryInkFiles()) {
            builder.append("INCLUDE ")
                    .append(toRelativePath(mainDirectory, storyInkFile))
                    .append("\n");
        }
        builder.append("\n-> chapter_1\n");

        File mainInkFile = new File(inkRoot, NarrativeCraftFileInit.MAIN_INK_NAME);
        try {
            Files.createDirectories(inkRoot.toPath());
            Files.writeString(mainInkFile.toPath(), builder.toString());
        } catch (IOException e) {
            NarrativeCraftMod.LOGGER.error("Failed to regenerate {}", mainInkFile, e);
        }
    }

    public static File getMainFile() {
        return getSourceFile(NarrativeCraftFileInit.MAIN_INK_NAME);
    }

    private static File getSourceFile(String name) {
        return new File(getMainDirectory(), name);
    }

    private static File getMainDirectory() {
        return NarrativeCraftMod.getInstance().getFile().getInit().getMainDirectory();
    }

    private static String toRelativePath(File base, File target) {
        return base.toPath().relativize(target.toPath()).toString();
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
