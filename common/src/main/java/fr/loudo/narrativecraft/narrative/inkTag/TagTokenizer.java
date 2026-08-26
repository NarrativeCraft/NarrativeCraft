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

package fr.loudo.narrativecraft.narrative.inkTag;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public final class TagTokenizer {

    private static final Pattern NAMED_VALUE_PREFIX = Pattern.compile("\\w+:");
    private static final String COMMENT_START = "//";
    private static final char NO_QUOTE = 0;

    private TagTokenizer() {}

    public static List<String> tokenize(String rawTag) {
        String tag = rawTag.trim();

        List<String> tokens = new ArrayList<>();
        StringBuilder currentValue = new StringBuilder();
        boolean valueStarted = false;
        char openingQuote = NO_QUOTE;
        int openingQuoteIndex = -1;

        for (int index = 0; index < tag.length(); index++) {
            char character = tag.charAt(index);

            if (escapesQuoteAt(tag, index, currentValue, openingQuote)) {
                currentValue.append(tag.charAt(index + 1));
                valueStarted = true;
                index++;

            } else if (openingQuote != NO_QUOTE) {
                if (character == openingQuote && isValueEnd(tag, index + 1)) {
                    openingQuote = NO_QUOTE;
                } else {
                    currentValue.append(character);
                }

            } else if (isQuote(character) && isValueStart(currentValue)) {
                openingQuote = character;
                openingQuoteIndex = index;
                valueStarted = true;

            } else if (tag.startsWith(COMMENT_START, index)) {
                break;

            } else if (Character.isWhitespace(character)) {
                if (valueStarted) {
                    tokens.add(currentValue.toString());
                    currentValue.setLength(0);
                    valueStarted = false;
                }

            } else {
                currentValue.append(character);
                valueStarted = true;
            }
        }

        if (openingQuote != NO_QUOTE) {
            throw new IllegalArgumentException("Unclosed " + openingQuote + " quote at position " + openingQuoteIndex
                    + " in tag '" + tag + "'. Close it before a space or at the end of the tag, or escape it as '\\"
                    + openingQuote + "' to use it as a plain character.");
        }

        if (valueStarted) {
            tokens.add(currentValue.toString());
        }

        return List.copyOf(tokens);
    }

    private static boolean escapesQuoteAt(String tag, int index, CharSequence currentValue, char openingQuote) {
        if (tag.charAt(index) != '\\' || index + 1 >= tag.length()) return false;

        char quote = tag.charAt(index + 1);
        if (!isQuote(quote)) return false;

        if (openingQuote == NO_QUOTE) return isValueStart(currentValue);
        return quote == openingQuote && isValueEnd(tag, index + 2);
    }

    private static boolean isValueStart(CharSequence currentValue) {
        return currentValue.isEmpty()
                || NAMED_VALUE_PREFIX.matcher(currentValue).matches();
    }

    private static boolean isValueEnd(String tag, int index) {
        return index >= tag.length()
                || Character.isWhitespace(tag.charAt(index))
                || tag.startsWith(COMMENT_START, index);
    }

    private static boolean isQuote(char character) {
        return character == '"' || character == '\'';
    }
}
