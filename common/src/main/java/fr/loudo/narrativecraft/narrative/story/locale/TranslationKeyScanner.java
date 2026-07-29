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
import fr.loudo.narrativecraft.files.NarrativeCraftFileInit;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class TranslationKeyScanner {

    public static final String GLOBAL_FILE_NAME = "global" + StoryTranslations.YAML_EXTENSION;

    private static final Pattern TRANSLATION_KEY_PATTERN = Pattern.compile("@trans\\(\\s*([A-Za-z0-9_.\\-]+)");
    private static final String COMMENT_PREFIX = "//";

    private TranslationKeyScanner() {}

    public static Map<String, List<String>> scanByTargetFile() {
        Map<String, Set<String>> keysByFile = new LinkedHashMap<>();

        NarrativeCraftFileInit fileInit =
                NarrativeCraftMod.getInstance().getFile().getInit();
        for (File rootInkFile :
                List.of(fileInit.getMainInk(), fileInit.getVariablesInk(), fileInit.getFunctionsInk())) {
            collectInto(rootInkFile, GLOBAL_FILE_NAME, keysByFile);
        }

        for (File storyInkFile : InkFileGenerator.collectStoryInkFiles()) {
            collectInto(storyInkFile, targetFileNameOf(storyInkFile), keysByFile);
        }

        Map<String, List<String>> result = new LinkedHashMap<>();
        keysByFile.forEach((fileName, keys) -> {
            if (!keys.isEmpty()) result.put(fileName, List.copyOf(keys));
        });
        return result;
    }

    public static List<String> scanAllKeys() {
        Set<String> keys = new LinkedHashSet<>();
        scanByTargetFile().values().forEach(keys::addAll);
        return List.copyOf(keys);
    }

    private static void collectInto(File inkFile, String targetFileName, Map<String, Set<String>> keysByFile) {
        Set<String> keys = keysByFile.computeIfAbsent(targetFileName, name -> new LinkedHashSet<>());

        List<String> lines;
        try {
            lines = Files.readAllLines(inkFile.toPath(), StandardCharsets.UTF_8);
        } catch (IOException exception) {
            NarrativeCraftMod.LOGGER.error("Failed to scan translation keys in {}", inkFile, exception);
            return;
        }

        for (String line : lines) {
            if (line.trim().startsWith(COMMENT_PREFIX)) continue;
            Matcher matcher = TRANSLATION_KEY_PATTERN.matcher(line);
            while (matcher.find()) {
                keys.add(matcher.group(1));
            }
        }
    }

    private static String targetFileNameOf(File inkFile) {
        File chapterDirectory = chapterDirectoryOf(inkFile);
        if (chapterDirectory == null) return GLOBAL_FILE_NAME;

        String directoryName = chapterDirectory.getName();
        int underscore = directoryName.indexOf('_');
        if (underscore > 0) {
            try {
                int chapterIndex = Integer.parseInt(directoryName.substring(0, underscore));
                return "chapter_" + chapterIndex + StoryTranslations.YAML_EXTENSION;
            } catch (NumberFormatException ignored) {
            }
        }
        return directoryName + StoryTranslations.YAML_EXTENSION;
    }

    private static File chapterDirectoryOf(File inkFile) {
        File chaptersDirectory =
                NarrativeCraftMod.getInstance().getFile().getInit().getChaptersDirectory();
        List<File> ancestors = new ArrayList<>();
        for (File current = inkFile.getParentFile(); current != null; current = current.getParentFile()) {
            if (current.equals(chaptersDirectory)) {
                return ancestors.isEmpty() ? null : ancestors.get(ancestors.size() - 1);
            }
            ancestors.add(current);
        }
        return null;
    }
}
