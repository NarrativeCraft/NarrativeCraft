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
import fr.loudo.narrativecraft.files.NarrativeCraftFileDefault;
import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.SafeConstructor;

public final class StoryTranslations {

    public static final String YAML_EXTENSION = ".yml";
    private static final String ALTERNATE_YAML_EXTENSION = ".yaml";

    private static final Map<String, Map<String, String>> entriesByLocale = new LinkedHashMap<>();

    private StoryTranslations() {}

    public static void reload() {
        entriesByLocale.clear();
        TranslationResolver.clearMissingKeyWarnings();
        StoryLocaleManager.ensureDefaultLocaleExists();

        for (String locale : StoryLocaleManager.listLocales()) {
            entriesByLocale.put(locale, loadLocale(locale));
        }
    }

    public static List<String> listLoadedLocales() {
        return List.copyOf(entriesByLocale.keySet());
    }

    public static Map<String, String> entriesOf(String locale) {
        return entriesByLocale.getOrDefault(locale, Map.of());
    }

    public static int entryCountOf(String locale) {
        return entriesOf(locale).size();
    }

    @Nullable
    public static String find(@Nullable String playerLocale, String key) {
        String value = findIn(playerLocale, key);
        if (value != null) return value;
        return findIn(StoryLocaleManager.getDefaultLocale(), key);
    }

    public static Map<String, String> resolvedEntriesFor(@Nullable String locale) {
        Map<String, String> resolved = new LinkedHashMap<>();
        putNonBlank(resolved, entriesOf(StoryLocaleManager.getDefaultLocale()));
        if (locale != null) {
            putNonBlank(resolved, entriesOf(locale));
        }
        return resolved;
    }

    private static void putNonBlank(Map<String, String> target, Map<String, String> source) {
        source.forEach((key, value) -> {
            if (!value.isBlank()) target.put(key, value);
        });
    }

    public static boolean hasTranslation(String locale, String key) {
        return findIn(locale, key) != null;
    }

    @Nullable
    private static String findIn(@Nullable String locale, String key) {
        if (locale == null) return null;
        String value = entriesOf(locale).get(key);
        return value == null || value.isBlank() ? null : value;
    }

    private static Map<String, String> loadLocale(String locale) {
        Map<String, String> entries = new LinkedHashMap<>();
        Path localeDirectory = NarrativeCraftMod.getInstance()
                .getFile()
                .getInit()
                .getLocaleDirectory(locale)
                .toPath();
        if (!Files.isDirectory(localeDirectory)) return entries;

        try (Stream<Path> paths = Files.walk(localeDirectory)) {
            List<Path> files = paths.filter(Files::isRegularFile)
                    .sorted(Comparator.comparing(Path::toString))
                    .toList();
            for (Path file : files) {
                String fileName = file.getFileName().toString().toLowerCase(Locale.ROOT);
                if (fileName.endsWith(NarrativeCraftFileDefault.EXTENSION_SCRIPT_FILE)) {
                    warnLegacyInkFile(locale, file);
                } else if (fileName.endsWith(YAML_EXTENSION) || fileName.endsWith(ALTERNATE_YAML_EXTENSION)) {
                    readInto(locale, file, entries);
                }
            }
        } catch (IOException exception) {
            NarrativeCraftMod.LOGGER.error("Failed to read translation files of locale '{}'", locale, exception);
        }

        return entries;
    }

    private static void readInto(String locale, Path file, Map<String, String> entries) {
        Object document;
        try (Reader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
            document = newYaml().load(reader);
        } catch (IOException | RuntimeException exception) {
            NarrativeCraftMod.LOGGER.error("Failed to parse translation file {}", file, exception);
            return;
        }

        if (document == null) return;
        if (!(document instanceof Map<?, ?> root)) {
            NarrativeCraftMod.LOGGER.warn("Translation file {} is not a key/value mapping, skipped.", file);
            return;
        }

        flatten("", root, locale, file, entries);
    }

    private static void flatten(String prefix, Map<?, ?> node, String locale, Path file, Map<String, String> entries) {
        for (Map.Entry<?, ?> entry : node.entrySet()) {
            if (entry.getKey() == null) continue;
            String key = prefix + entry.getKey();
            Object value = entry.getValue();

            if (value instanceof Map<?, ?> child) {
                flatten(key + ".", child, locale, file, entries);
                continue;
            }

            String previous = entries.put(key, value == null ? "" : String.valueOf(value));
            if (previous != null) {
                NarrativeCraftMod.LOGGER.warn(
                        "Translation key '{}' of locale '{}' is defined twice, {} wins.", key, locale, file);
            }
        }
    }

    private static Yaml newYaml() {
        return new Yaml(new SafeConstructor(new LoaderOptions()));
    }

    private static void warnLegacyInkFile(String locale, Path file) {
        NarrativeCraftMod.LOGGER.warn(
                "Locale '{}' still contains an ink file ({}). One ink tree per locale is no longer supported: "
                        + "translate with @trans(...) keys in .yml files, then delete the leftover ink files.",
                locale,
                file);
    }
}
