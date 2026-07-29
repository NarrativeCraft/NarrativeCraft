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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.UnaryOperator;

public final class TranslationResolver {

    public static final String TRANSLATION_PREFIX = "@trans(";

    private static final char PLACEHOLDER_MARKER = '%';
    private static final Set<String> missingKeyWarnings = new HashSet<>();

    private TranslationResolver() {}

    public static String resolve(String text, UnaryOperator<String> keyLookup) {
        if (text == null || text.isEmpty()) return text;

        int expressionStart = text.indexOf(TRANSLATION_PREFIX);
        if (expressionStart < 0) return text;

        StringBuilder resolved = new StringBuilder(text.length());
        int cursor = 0;

        while (expressionStart >= 0) {
            resolved.append(text, cursor, expressionStart);

            int contentStart = expressionStart + TRANSLATION_PREFIX.length();
            int contentEnd = findClosingParenthesis(text, contentStart);
            if (contentEnd < 0) {
                NarrativeCraftMod.LOGGER.warn("Unclosed @trans(...) expression in \"{}\"", text);
                resolved.append(text, expressionStart, text.length());
                return resolved.toString();
            }

            resolved.append(resolveExpression(text.substring(contentStart, contentEnd), keyLookup));
            cursor = contentEnd + 1;
            expressionStart = text.indexOf(TRANSLATION_PREFIX, cursor);
        }

        resolved.append(text, cursor, text.length());
        return resolved.toString();
    }

    public static void clearMissingKeyWarnings() {
        missingKeyWarnings.clear();
    }

    private static String resolveExpression(String content, UnaryOperator<String> keyLookup) {
        List<String> segments = splitTopLevel(content);
        String key = segments.get(0).trim();
        if (key.isEmpty()) return "";

        String value = keyLookup.apply(key);
        if (value == null) {
            warnMissingKey(key);
            return key;
        }

        return applyArguments(value, parseArguments(segments));
    }

    private static Map<String, String> parseArguments(List<String> segments) {
        if (segments.size() == 1) return Map.of();

        Map<String, String> arguments = new HashMap<>();
        for (String segment : segments.subList(1, segments.size())) {
            int separator = segment.indexOf('=');
            if (separator < 0) continue;
            String name = segment.substring(0, separator).trim();
            if (name.isEmpty()) continue;
            arguments.put(name, unquote(segment.substring(separator + 1).trim()));
        }
        return arguments;
    }

    private static String applyArguments(String value, Map<String, String> arguments) {
        if (arguments.isEmpty()) return value;

        StringBuilder result = new StringBuilder(value.length());
        int cursor = 0;

        while (true) {
            int openMarker = value.indexOf(PLACEHOLDER_MARKER, cursor);
            if (openMarker < 0) break;
            int closeMarker = value.indexOf(PLACEHOLDER_MARKER, openMarker + 1);
            if (closeMarker < 0) break;

            String argument = arguments.get(value.substring(openMarker + 1, closeMarker));
            if (argument == null) {
                result.append(value, cursor, closeMarker);
                cursor = closeMarker;
                continue;
            }

            result.append(value, cursor, openMarker).append(argument);
            cursor = closeMarker + 1;
        }

        result.append(value, cursor, value.length());
        return result.toString();
    }

    private static int findClosingParenthesis(String text, int from) {
        int depth = 1;
        boolean insideQuotes = false;

        for (int index = from; index < text.length(); index++) {
            char character = text.charAt(index);
            if (character == '"') {
                insideQuotes = !insideQuotes;
            } else if (!insideQuotes) {
                if (character == '(') {
                    depth++;
                } else if (character == ')' && --depth == 0) {
                    return index;
                }
            }
        }

        return -1;
    }

    private static List<String> splitTopLevel(String content) {
        List<String> segments = new ArrayList<>();
        int depth = 0;
        int start = 0;
        boolean insideQuotes = false;

        for (int index = 0; index < content.length(); index++) {
            char character = content.charAt(index);
            if (character == '"') {
                insideQuotes = !insideQuotes;
            } else if (!insideQuotes) {
                if (character == '(') {
                    depth++;
                } else if (character == ')') {
                    depth--;
                } else if (character == ',' && depth == 0) {
                    segments.add(content.substring(start, index));
                    start = index + 1;
                }
            }
        }

        segments.add(content.substring(start));
        return segments;
    }

    private static String unquote(String value) {
        if (value.length() >= 2 && value.charAt(0) == '"' && value.charAt(value.length() - 1) == '"') {
            return value.substring(1, value.length() - 1);
        }
        return value;
    }

    private static void warnMissingKey(String key) {
        if (!missingKeyWarnings.add(key)) return;
        NarrativeCraftMod.LOGGER.warn("Missing translation key '{}'", key);
    }
}
