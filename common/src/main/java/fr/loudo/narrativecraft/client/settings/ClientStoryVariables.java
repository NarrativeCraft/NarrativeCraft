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

package fr.loudo.narrativecraft.client.settings;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class ClientStoryVariables {

    private static final Pattern VARIABLE_PATTERN = Pattern.compile("\\{([A-Za-z0-9_]+)}");

    private static Map<String, String> variables = Map.of();

    private ClientStoryVariables() {}

    public static void set(Map<String, String> variables) {
        ClientStoryVariables.variables = Map.copyOf(variables);
    }

    public static void clear() {
        variables = Map.of();
    }

    public static String interpolate(String text) {
        if (text == null || variables.isEmpty() || text.indexOf('{') < 0) return text;

        Matcher matcher = VARIABLE_PATTERN.matcher(text);
        StringBuilder interpolated = new StringBuilder();
        while (matcher.find()) {
            String value = variables.get(matcher.group(1));
            matcher.appendReplacement(interpolated, Matcher.quoteReplacement(value == null ? matcher.group() : value));
        }
        matcher.appendTail(interpolated);
        return interpolated.toString();
    }
}
