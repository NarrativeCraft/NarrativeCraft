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

package fr.loudo.narrativecraft.api.inkAction;

import com.bladecoder.ink.runtime.Story;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class InkActionUtil {

    public static final Pattern VARIABLE_NAME = Pattern.compile("%([A-Za-z0-9_]+)%");

    private InkActionUtil() {}

    /**
     * Converts a numeric value and a time-unit string to a duration in seconds.
     *
     * @return the converted seconds, or {@code -1} if {@code unit} is unrecognized
     */
    public static double getSecondsFromTimeValue(double value, String unit) {
        if (unit.contains("second")) return value;
        if (unit.contains("minute")) return value * 60.0;
        if (unit.contains("hour")) return value * 3600.0;
        return -1;
    }

    /**
     * Searches for variable names in a text and parses them by retrieving their value from the story.
     *
     * <p>Variable placeholders follow the pattern {@code %variable_name%}. For example:
     * <pre>{@code
     * I went %place_time_value% times here!
     * }</pre>
     * If the story has {@code place_time_value} assigned (e.g. {@code 3}), the method will return:
     * <pre>{@code
     * I went 3 times here!
     * }</pre>
     *
     * @deprecated Ink already has an internal variable parser. Instead of using {@code %}, it uses
     *             {@code {}}. For example, {@code %place_time_value%} becomes {@code {place_time_value}}.
     * @param story the main story instance
     * @param text  the text to parse
     * @return the text with the variable parsed if found
     */
    @Deprecated(since = "1", forRemoval = true)
    public static String parseVariables(Story story, String text) {
        if (story == null) return text;
        Matcher matcher = VARIABLE_NAME.matcher(text);
        while (matcher.find()) {
            Object variable = story.getVariablesState().get(matcher.group(1));
            if (variable == null) continue;
            text = text.replace("%" + matcher.group(1) + "%", variable.toString());
        }
        return text;
    }
}
