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
import fr.loudo.narrativecraft.server.settings.NarrativeServerSettings;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public final class StoryLocaleManager {

    public static final Pattern LOCALE_PATTERN = Pattern.compile("^[a-z]{2,3}(_[a-z0-9]{2,8})?$");

    private static final String SYNC_SEPARATOR = "# --- added by /nc locale sync ---";

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
        return isValidLocale(locale) && localeDirectory(locale).isDirectory();
    }

    public static List<String> listLocales() {
        File[] directories = localesDirectory().listFiles(File::isDirectory);
        if (directories == null) return List.of();
        return Arrays.stream(directories)
                .map(File::getName)
                .filter(StoryLocaleManager::isValidLocale)
                .sorted()
                .toList();
    }

    public static List<String> listAvailableLocales() {
        List<String> locales = new ArrayList<>();
        locales.add(getDefaultLocale());
        listLocales().stream().filter(locale -> !isDefaultLocale(locale)).forEach(locales::add);
        return List.copyOf(locales);
    }

    public static void ensureDefaultLocaleExists() {
        String defaultLocale = getDefaultLocale();
        if (!isValidLocale(defaultLocale) || exists(defaultLocale)) return;

        try {
            Files.createDirectories(localeDirectory(defaultLocale).toPath());
        } catch (IOException exception) {
            NarrativeCraftMod.LOGGER.error("Failed to create the default locale folder '{}'", defaultLocale, exception);
        }
    }

    public static int create(String locale) throws IOException {
        File localeDirectory = localeDirectory(locale);
        Files.createDirectories(localeDirectory.toPath());

        int keyCount = 0;
        for (Map.Entry<String, List<String>> entry :
                TranslationKeyScanner.scanByTargetFile().entrySet()) {
            appendKeys(new File(localeDirectory, entry.getKey()), entry.getValue());
            keyCount += entry.getValue().size();
        }
        return keyCount;
    }

    public static void remove(String locale) throws IOException {
        deleteRecursively(localeDirectory(locale));
    }

    public static Map<String, SyncReport> syncAll() throws IOException {
        StoryTranslations.reload();

        Map<String, List<String>> keysByFile = TranslationKeyScanner.scanByTargetFile();
        Map<String, SyncReport> reports = new LinkedHashMap<>();
        for (String locale : listLocales()) {
            reports.put(locale, sync(locale, keysByFile));
        }

        StoryTranslations.reload();
        return reports;
    }

    private static SyncReport sync(String locale, Map<String, List<String>> keysByFile) throws IOException {
        File localeDirectory = localeDirectory(locale);
        Files.createDirectories(localeDirectory.toPath());

        Map<String, String> existingEntries = StoryTranslations.entriesOf(locale);
        Set<String> expectedKeys = new HashSet<>();
        int addedKeyCount = 0;

        for (Map.Entry<String, List<String>> entry : keysByFile.entrySet()) {
            expectedKeys.addAll(entry.getValue());
            List<String> missingKeys = entry.getValue().stream()
                    .filter(key -> !existingEntries.containsKey(key))
                    .toList();
            if (missingKeys.isEmpty()) continue;

            appendKeys(new File(localeDirectory, entry.getKey()), missingKeys);
            addedKeyCount += missingKeys.size();
        }

        List<String> orphanKeys = existingEntries.keySet().stream()
                .filter(key -> !expectedKeys.contains(key))
                .sorted()
                .toList();

        return new SyncReport(addedKeyCount, orphanKeys);
    }

    private static void appendKeys(File file, List<String> keys) throws IOException {
        StringBuilder builder = new StringBuilder();
        if (file.isFile() && file.length() > 0) {
            builder.append("\n").append(SYNC_SEPARATOR).append("\n");
        }
        for (String key : keys) {
            builder.append(key).append(": \"\"\n");
        }

        Files.writeString(
                file.toPath(),
                builder.toString(),
                StandardCharsets.UTF_8,
                StandardOpenOption.CREATE,
                StandardOpenOption.APPEND);
    }

    private static void deleteRecursively(File file) throws IOException {
        if (!file.exists()) return;
        try (Stream<Path> paths = Files.walk(file.toPath())) {
            for (Path path : paths.sorted(Comparator.reverseOrder()).toList()) {
                Files.deleteIfExists(path);
            }
        }
    }

    private static File localesDirectory() {
        return NarrativeCraftMod.getInstance().getFile().getInit().getLocalesDirectory();
    }

    private static File localeDirectory(String locale) {
        return NarrativeCraftMod.getInstance().getFile().getInit().getLocaleDirectory(locale);
    }

    public record SyncReport(int addedKeyCount, List<String> orphanKeys) {

        public boolean isEmpty() {
            return addedKeyCount == 0 && orphanKeys.isEmpty();
        }
    }
}
