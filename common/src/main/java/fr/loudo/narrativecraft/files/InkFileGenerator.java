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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class InkFileGenerator {

    public static void writeChapterInkFile(FileTransaction transaction, File chapterFolder, Chapter chapter)
            throws IOException {
        transaction.writeString(
                new File(chapterFolder, chapterInkFileName(chapter.getChapterIndex())),
                chapterInkContent(chapter.getChapterIndex()));
    }

    public static void writeSceneInkFile(FileTransaction transaction, File sceneFolder, Scene scene)
            throws IOException {
        transaction.writeString(new File(sceneFolder, scene.inkFileName()), sceneInkContent(scene.knotName()));
    }

    public static void renameSceneInkFile(FileTransaction transaction, File sceneFolder, Scene existing, Scene updated)
            throws IOException {
        File oldInkFile = new File(sceneFolder, existing.inkFileName());
        File newInkFile = new File(sceneFolder, updated.inkFileName());
        rewriteInkFile(
                transaction,
                oldInkFile,
                newInkFile,
                Map.of(existing.knotName(), updated.knotName()),
                sceneInkContent(updated.knotName()));
    }

    public static void reindexChapterInkFiles(
            FileTransaction transaction, File chapterFolder, Chapter chapter, int oldIndex, int newIndex)
            throws IOException {
        Map<String, String> knotRenames = new LinkedHashMap<>();
        knotRenames.put(chapterKnotName(oldIndex), chapterKnotName(newIndex));
        for (Scene scene : chapter.getSceneManager().getList()) {
            knotRenames.put(scene.knotName(oldIndex), scene.knotName(newIndex));
        }

        rewriteInkFile(
                transaction,
                new File(chapterFolder, chapterInkFileName(oldIndex)),
                new File(chapterFolder, chapterInkFileName(newIndex)),
                knotRenames,
                chapterInkContent(newIndex));

        File scenesFolder = new File(chapterFolder, NarrativeCraftFileDefault.SCENES_FOLDER_NAME);
        for (Scene scene : chapter.getSceneManager().getList()) {
            File sceneInkFile = new File(new File(scenesFolder, scene.toFileName(newIndex)), scene.inkFileName());
            rewriteInkFile(
                    transaction, sceneInkFile, sceneInkFile, knotRenames, sceneInkContent(scene.knotName(newIndex)));
        }
    }

    private static void rewriteInkFile(
            FileTransaction transaction,
            File oldInkFile,
            File newInkFile,
            Map<String, String> knotRenames,
            String defaultContent)
            throws IOException {
        if (!oldInkFile.exists()) {
            transaction.writeString(newInkFile, defaultContent);
            return;
        }
        String content = Files.readString(oldInkFile.toPath());
        for (Map.Entry<String, String> knotRename : knotRenames.entrySet()) {
            content = content.replaceAll(
                    "\\b" + Pattern.quote(knotRename.getKey()) + "\\b",
                    Matcher.quoteReplacement(knotRename.getValue()));
        }
        transaction.writeString(newInkFile, content);
        if (!oldInkFile.equals(newInkFile)) {
            transaction.delete(oldInkFile);
        }
    }

    public static List<File> collectStoryInkFiles() {
        File chaptersFolder = NarrativeCraftFileUtil.getChaptersFolder();
        List<File> inkFiles = new ArrayList<>();

        File[] chapterDirs = chaptersFolder.listFiles(InkFileGenerator::isStoryDirectory);
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

            File[] sceneDirs = scenesDir.listFiles(InkFileGenerator::isStoryDirectory);
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

    public static void regenerateMainInk() {
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

        File mainInkFile = new File(mainDirectory, NarrativeCraftFileInit.MAIN_INK_NAME);
        try {
            NarrativeCraftFileWriter.writeString(mainInkFile, builder.toString());
        } catch (IOException e) {
            NarrativeCraftMod.LOGGER.error("Failed to regenerate {}", mainInkFile, e);
        }
    }

    public static File getMainFile() {
        return getSourceFile(NarrativeCraftFileInit.MAIN_INK_NAME);
    }

    private static boolean isStoryDirectory(File file) {
        return file.isDirectory() && !NarrativeCraftFileWriter.isTemporary(file);
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

    private static String chapterInkFileName(int chapterIndex) {
        return chapterKnotName(chapterIndex) + NarrativeCraftFileDefault.EXTENSION_SCRIPT_FILE;
    }

    private static String chapterKnotName(int chapterIndex) {
        return "chapter_" + chapterIndex;
    }

    private static String chapterInkContent(int chapterIndex) {
        return "=== " + chapterKnotName(chapterIndex) + " ===\n";
    }

    private static String sceneInkContent(String knotName) {
        return "=== " + knotName + " ===\n# on_enter " + knotName + "\n-> END\n";
    }
}
