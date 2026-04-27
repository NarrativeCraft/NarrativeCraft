/*
 * NarrativeCraft - Create your own stories, easily, and freely in Minecraft.
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

public final class InkActionUtil {

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
}
