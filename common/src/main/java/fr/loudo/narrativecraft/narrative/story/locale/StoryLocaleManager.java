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

package fr.loudo.narrativecraft.narrative.story.locale;

import fr.loudo.narrativecraft.NarrativeCraftMod;
import fr.loudo.narrativecraft.files.InkFileGenerator;
import fr.loudo.narrativecraft.files.NarrativeCraftFileDefault;
import fr.loudo.narrativecraft.files.NarrativeCraftFileInit;
import fr.loudo.narrativecraft.files.NarrativeCraftFileUtil;
import fr.loudo.narrativecraft.server.settings.NarrativeServerSettings;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.annotation.Nullable;

public final class StoryLocaleManager {

    public static final Pattern LOCALE_PATTERN = Pattern.compile("^[a-z]{2,3}(_[a-z0-9]{2,8})?$");

    private static final Pattern KNOT_HEADER_PATTERN = Pattern.compile("^\\s*===\\s*(\\w+)\\s*=*\\s*$");
    private static final Pattern CHAPTER_INK_PATTERN = Pattern.compile("^chapter_\\d+\\.ink$");

    private StoryLocaleManager() {}

    public static boolean isValidLocale(String locale) {
        return locale != null && LOCALE_PATTERN.matcher(locale).matches();
    }

    public static String getDefaultLocale() {
        return NarrativeServerSettings.defaultLocale;
    }

    public static boolean isDefaultLocale(String locale) {
        return getDefaultLocale().equalsIgnoreCase(locale);
    }

    public static boolean exists(String locale) {
        return isDefaultLocale(locale) || localeDirectory(locale).isDirectory();
    }

    public static List<String> listLocales() {
        File[] directories = localesDirectory().listFiles(File::isDirectory);
        if (directories == null) return List.of();
        return Arrays.stream(directories)
                .map(File::getName)
                .filter(StoryLocaleManager::isValidLocale)
                .filter(locale -> !isDefaultLocale(locale))
                .sorted()
                .toList();
    }

    public static List<String> listAvailableLocales() {
        List<String> locales = new ArrayList<>();
        locales.add(getDefaultLocale());
        locales.addAll(listLocales());
        return List.copyOf(locales);
    }

    public static File inkRootOf(@Nullable String locale) {
        if (locale == null || isDefaultLocale(locale)) return mainDirectory();
        return localeDirectory(locale);
    }

    public static void create(String locale) throws IOException {
        Files.createDirectories(localeDirectory(locale).toPath());
        sync(locale);
    }

    public static void remove(String locale) throws IOException {
        deleteRecursively(localeDirectory(locale));
    }

    public static Map<String, SyncReport> syncAll() throws IOException {
        Map<String, SyncReport> reports = new LinkedHashMap<>();
        for (String locale : listLocales()) {
            reports.put(locale, sync(locale));
        }
        return reports;
    }

    public static SyncReport sync(String locale) throws IOException {
        File localeDirectory = localeDirectory(locale);
        Files.createDirectories(localeDirectory.toPath());

        SyncReport report = new SyncReport();

        copyIfMissing(sourceFile(NarrativeCraftFileInit.VARS_INK_NAME), localeDirectory, report);
        copyIfMissing(sourceFile(NarrativeCraftFileInit.FUNCTIONS_INK_NAME), localeDirectory, report);
        deleteUnexpectedRootEntries(localeDirectory, report);
        syncChapters(localeDirectory, report);

        InkFileGenerator.regenerateLocaleMainInk(locale);
        return report;
    }

    public static void setDefault(String newDefault) throws IOException {
        String previousDefault = getDefaultLocale();
        File previousDefaultDirectory = localeDirectory(previousDefault);
        if (previousDefaultDirectory.exists()) {
            throw new IOException("A locale folder named '" + previousDefault + "' already exists");
        }

        sync(newDefault);

        File newDefaultDirectory = localeDirectory(newDefault);
        List<String> storyPaths = storyInkPaths();

        Map<String, String> incoming = new LinkedHashMap<>();
        for (String path : storyPaths) {
            File translated = new File(newDefaultDirectory, path);
            if (translated.isFile()) {
                incoming.put(path, Files.readString(translated.toPath()));
            }
        }

        for (String path : storyPaths) {
            File source = new File(mainDirectory(), path);
            if (!source.isFile()) continue;
            File target = new File(previousDefaultDirectory, path);
            Files.createDirectories(target.getParentFile().toPath());
            Files.copy(source.toPath(), target.toPath(), StandardCopyOption.REPLACE_EXISTING);
        }

        for (Map.Entry<String, String> entry : incoming.entrySet()) {
            File target = new File(mainDirectory(), entry.getKey());
            Files.createDirectories(target.getParentFile().toPath());
            Files.writeString(target.toPath(), entry.getValue());
        }

        deleteRecursively(newDefaultDirectory);

        NarrativeServerSettings.defaultLocale = newDefault;
        NarrativeServerSettings.save();

        InkFileGenerator.regenerateMainInk();
        for (String locale : listLocales()) {
            InkFileGenerator.regenerateLocaleMainInk(locale);
        }
    }

    private static List<String> storyInkPaths() {
        List<String> paths = new ArrayList<>();
        paths.add(NarrativeCraftFileInit.VARS_INK_NAME);
        paths.add(NarrativeCraftFileInit.FUNCTIONS_INK_NAME);
        for (File inkFile : InkFileGenerator.collectStoryInkFiles()) {
            paths.add(relativize(mainDirectory(), inkFile));
        }
        return paths;
    }

    private static void syncChapters(File localeDirectory, SyncReport report) throws IOException {
        File sourceChapters = NarrativeCraftFileUtil.getChaptersFolder();
        File localeChapters = new File(localeDirectory, NarrativeCraftFileInit.CHAPTERS_DIRECTORY_NAME);
        Files.createDirectories(localeChapters.toPath());

        pairDirectories(sourceChapters, localeChapters, report);

        for (File sourceChapter : subDirectories(sourceChapters)) {
            File localeChapter = new File(localeChapters, sourceChapter.getName());
            Files.createDirectories(localeChapter.toPath());

            syncInkFiles(sourceChapter, localeChapter, report);
            repairKnots(localeChapter, chapterIndexOf(sourceChapter), report);

            File sourceScenes = new File(sourceChapter, NarrativeCraftFileDefault.SCENES_FOLDER_NAME);
            if (!sourceScenes.isDirectory()) continue;

            File localeScenes = new File(localeChapter, NarrativeCraftFileDefault.SCENES_FOLDER_NAME);
            Files.createDirectories(localeScenes.toPath());
            pairDirectories(sourceScenes, localeScenes, report);

            for (File sourceScene : subDirectories(sourceScenes)) {
                File localeScene = new File(localeScenes, sourceScene.getName());
                Files.createDirectories(localeScene.toPath());
                syncInkFiles(sourceScene, localeScene, report);
                repairKnots(localeScene, chapterIndexOf(sourceChapter), report);
            }
        }
    }

    private static void pairDirectories(File sourceParent, File localeParent, SyncReport report) throws IOException {
        List<String> missing = missingNames(subDirectoryNames(sourceParent), subDirectoryNames(localeParent));
        List<String> orphans = missingNames(subDirectoryNames(localeParent), subDirectoryNames(sourceParent));

        if (missing.size() == 1 && orphans.size() == 1) {
            File orphan = new File(localeParent, orphans.get(0));
            File renamed = new File(localeParent, missing.get(0));
            Files.move(orphan.toPath(), renamed.toPath(), StandardCopyOption.REPLACE_EXISTING);
            report.renamed.add(orphans.get(0) + " -> " + missing.get(0));
            return;
        }

        for (String orphan : orphans) {
            deleteRecursively(new File(localeParent, orphan));
            report.deleted.add(orphan);
        }
    }

    private static void syncInkFiles(File sourceDirectory, File localeDirectory, SyncReport report) throws IOException {
        List<String> sourceNames = inkFileNames(sourceDirectory);
        List<String> localeNames = inkFileNames(localeDirectory);

        List<String> missing = missingNames(sourceNames, localeNames);
        List<String> orphans = missingNames(localeNames, sourceNames);

        if (missing.size() == 1 && orphans.size() == 1) {
            File orphan = new File(localeDirectory, orphans.get(0));
            File renamed = new File(localeDirectory, missing.get(0));
            Files.move(orphan.toPath(), renamed.toPath(), StandardCopyOption.REPLACE_EXISTING);
            report.renamed.add(orphans.get(0) + " -> " + missing.get(0));
            return;
        }

        for (String orphan : orphans) {
            Files.deleteIfExists(new File(localeDirectory, orphan).toPath());
            report.deleted.add(orphan);
        }
        for (String name : missing) {
            copyIfMissing(new File(sourceDirectory, name), localeDirectory, report);
        }
    }

    private static void repairKnots(File localeDirectory, int chapterIndex, SyncReport report) throws IOException {
        for (String name : inkFileNames(localeDirectory)) {
            File inkFile = new File(localeDirectory, name);
            String expectedKnot = CHAPTER_INK_PATTERN.matcher(name).matches()
                    ? baseName(name)
                    : "chapter_" + chapterIndex + "_" + baseName(name);

            String content = Files.readString(inkFile.toPath());
            String currentKnot = findKnot(content);
            if (currentKnot == null || currentKnot.equals(expectedKnot)) continue;

            String repaired = content.replaceAll("\\b" + Pattern.quote(currentKnot) + "\\b", expectedKnot);
            Files.writeString(inkFile.toPath(), repaired);
            report.repaired.add(currentKnot + " -> " + expectedKnot);
        }
    }

    @Nullable
    private static String findKnot(String content) {
        for (String line : content.split("\n", -1)) {
            Matcher matcher = KNOT_HEADER_PATTERN.matcher(line);
            if (matcher.matches()) return matcher.group(1);
        }
        return null;
    }

    private static void deleteUnexpectedRootEntries(File localeDirectory, SyncReport report) throws IOException {
        File[] entries = localeDirectory.listFiles();
        if (entries == null) return;

        List<String> expected = List.of(
                NarrativeCraftFileInit.MAIN_INK_NAME,
                NarrativeCraftFileInit.VARS_INK_NAME,
                NarrativeCraftFileInit.FUNCTIONS_INK_NAME,
                NarrativeCraftFileInit.CHAPTERS_DIRECTORY_NAME);

        for (File entry : entries) {
            if (expected.contains(entry.getName())) continue;
            deleteRecursively(entry);
            report.deleted.add(entry.getName());
        }
    }

    private static void copyIfMissing(File sourceFile, File targetDirectory, SyncReport report) throws IOException {
        if (!sourceFile.isFile()) return;
        File target = new File(targetDirectory, sourceFile.getName());
        if (target.exists()) return;

        Files.createDirectories(targetDirectory.toPath());
        Files.copy(sourceFile.toPath(), target.toPath());
        report.created.add(sourceFile.getName());
    }

    private static List<String> missingNames(List<String> expected, List<String> present) {
        return expected.stream().filter(name -> !present.contains(name)).toList();
    }

    private static List<File> subDirectories(File directory) {
        File[] directories = directory.listFiles(File::isDirectory);
        if (directories == null) return List.of();
        return Arrays.stream(directories)
                .sorted(Comparator.comparing(File::getName))
                .toList();
    }

    private static List<String> subDirectoryNames(File directory) {
        return subDirectories(directory).stream().map(File::getName).toList();
    }

    private static List<String> inkFileNames(File directory) {
        File[] files = directory.listFiles(
                file -> file.isFile() && file.getName().endsWith(NarrativeCraftFileDefault.EXTENSION_SCRIPT_FILE));
        if (files == null) return List.of();
        return Arrays.stream(files).map(File::getName).sorted().toList();
    }

    private static int chapterIndexOf(File chapterDirectory) {
        String name = chapterDirectory.getName();
        int underscore = name.indexOf('_');
        if (underscore <= 0) return -1;
        try {
            return Integer.parseInt(name.substring(0, underscore));
        } catch (NumberFormatException exception) {
            return -1;
        }
    }

    private static String baseName(String fileName) {
        return fileName.substring(0, fileName.length() - NarrativeCraftFileDefault.EXTENSION_SCRIPT_FILE.length())
                .toLowerCase(Locale.ROOT);
    }

    private static String relativize(File base, File target) {
        return base.toPath().relativize(target.toPath()).toString();
    }

    private static void deleteRecursively(File file) throws IOException {
        File[] children = file.listFiles();
        if (children != null) {
            for (File child : children) {
                deleteRecursively(child);
            }
        }
        Files.deleteIfExists(file.toPath());
    }

    private static File sourceFile(String name) {
        return new File(mainDirectory(), name);
    }

    private static File mainDirectory() {
        return NarrativeCraftMod.getInstance().getFile().getInit().getMainDirectory();
    }

    private static File localesDirectory() {
        return NarrativeCraftMod.getInstance().getFile().getInit().getLocalesDirectory();
    }

    private static File localeDirectory(String locale) {
        return NarrativeCraftMod.getInstance().getFile().getInit().getLocaleDirectory(locale);
    }

    public static class SyncReport {

        private final List<String> created = new ArrayList<>();
        private final List<String> renamed = new ArrayList<>();
        private final List<String> deleted = new ArrayList<>();
        private final List<String> repaired = new ArrayList<>();

        public List<String> getCreated() {
            return created;
        }

        public List<String> getRenamed() {
            return renamed;
        }

        public List<String> getDeleted() {
            return deleted;
        }

        public List<String> getRepaired() {
            return repaired;
        }

        public boolean isEmpty() {
            return created.isEmpty() && renamed.isEmpty() && deleted.isEmpty() && repaired.isEmpty();
        }
    }
}
