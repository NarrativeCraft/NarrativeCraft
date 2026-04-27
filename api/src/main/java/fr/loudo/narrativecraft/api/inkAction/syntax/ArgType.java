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

package fr.loudo.narrativecraft.api.inkAction.syntax;

/** The supported value types for Ink action arguments. */
public enum ArgType {
    STRING,
    INT,
    FLOAT,
    BOOLEAN;

    /** Parses a type name from a syntax token (case-insensitive). */
    public static ArgType fromToken(String token) {
        return switch (token.toLowerCase()) {
            case "string" -> STRING;
            case "int" -> INT;
            case "float" -> FLOAT;
            case "boolean" -> BOOLEAN;
            default ->
                throw new IllegalArgumentException(
                        "Unknown type '" + token + "'. Supported: string, int, float, boolean.");
        };
    }

    /** Parses a raw string value into a typed Java value. */
    public Object parse(String raw) {
        return switch (this) {
            case STRING -> raw;
            case INT -> parseIntSafe(raw);
            case FLOAT -> parseFloatSafe(raw);
            case BOOLEAN -> parseBooleanSafe(raw);
        };
    }

    private static int parseIntSafe(String raw) {
        try {
            return Integer.parseInt(raw);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Expected an integer but got '" + raw + "'.");
        }
    }

    private static float parseFloatSafe(String raw) {
        try {
            return Float.parseFloat(raw);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Expected a float but got '" + raw + "'.");
        }
    }

    private static boolean parseBooleanSafe(String raw) {
        if (raw.equalsIgnoreCase("true")) return true;
        if (raw.equalsIgnoreCase("false")) return false;
        throw new IllegalArgumentException("Expected 'true' or 'false' but got '" + raw + "'.");
    }
}
