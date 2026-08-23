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

package fr.loudo.narrativecraft.api.inkAction.syntax;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parses an {@link fr.loudo.narrativecraft.api.inkAction.InkCommand#syntax()} string into a
 * {@link CommandSpec} once at startup.
 *
 * <h2>Supported token forms</h2>
 * <ul>
 *   <li>{@code <name:type>}, required positional argument</li>
 *   <li>{@code [name:type=default]}, optional named argument with a default value</li>
 *   <li>{@code [--flag]}, boolean flag; absent = {@code false}</li>
 * </ul>
 *
 * <p>Errors thrown here include the full expected format so the syntax string can be fixed
 * without reading the source code.
 */
public final class SyntaxParser {

    private static final Pattern REQUIRED_ARG = Pattern.compile("<(\\w+):(\\w+)>");
    private static final Pattern OPTIONAL_POSITIONAL_ARG = Pattern.compile("\\((\\w+):(\\w+)\\)");
    private static final Pattern OPTIONAL_ARG = Pattern.compile("\\[(\\w+):(\\w+)(?:=([^\\]]*))?]");
    private static final Pattern FLAG = Pattern.compile("\\[--(\\w+)]");

    private SyntaxParser() {}

    /**
     * Parses the syntax string and returns a compiled {@link CommandSpec}.
     *
     * @param keyword the tag keyword (first token of the syntax string, used in error messages)
     * @param syntax  the full syntax declaration from {@link fr.loudo.narrativecraft.api.inkAction.InkCommand#syntax()}
     */
    public static CommandSpec parse(String keyword, String syntax) {
        List<ArgDef> positionalArgs = new ArrayList<>();
        List<ArgDef> optionalPositionalArgs = new ArrayList<>();
        Map<String, NamedArgDef> namedArgs = new LinkedHashMap<>();
        Map<String, FlagDef> flags = new LinkedHashMap<>();

        Set<String> declaredNames = new HashSet<>();

        String[] tokens = syntax.trim().split("\\s+");

        for (int i = 1; i < tokens.length; i++) {
            String token = tokens[i];

            Matcher requiredMatcher = REQUIRED_ARG.matcher(token);
            if (requiredMatcher.matches()) {
                String name = requiredMatcher.group(1);
                checkUniqueName(keyword, token, name, declaredNames);
                ArgType type = parseType(token, requiredMatcher.group(2));
                positionalArgs.add(new ArgDef(name, type));
                continue;
            }

            Matcher optionalPositionalMatcher = OPTIONAL_POSITIONAL_ARG.matcher(token);
            if (optionalPositionalMatcher.matches()) {
                String name = optionalPositionalMatcher.group(1);
                checkUniqueName(keyword, token, name, declaredNames);
                ArgType type = parseType(token, optionalPositionalMatcher.group(2));
                optionalPositionalArgs.add(new ArgDef(name, type));
                continue;
            }

            Matcher optionalMatcher = OPTIONAL_ARG.matcher(token);
            if (optionalMatcher.matches()) {
                String name = optionalMatcher.group(1);
                checkUniqueName(keyword, token, name, declaredNames);
                ArgType type = parseType(token, optionalMatcher.group(2));
                Object defaultValue = parseDefault(token, type, optionalMatcher.group(3));
                namedArgs.put(name, new NamedArgDef(name, type, defaultValue));
                continue;
            }

            Matcher flagMatcher = FLAG.matcher(token);
            if (flagMatcher.matches()) {
                String name = flagMatcher.group(1);
                flags.put(name, new FlagDef(name));
                continue;
            }

            throw new IllegalArgumentException("Invalid syntax token '" + token + "' in syntax for '" + keyword + "'. "
                    + "Expected one of: <name:type>, (name:type), [name:type=default], [--flag].");
        }

        return new CommandSpec(keyword, positionalArgs, optionalPositionalArgs, namedArgs, flags);
    }

    private static void checkUniqueName(String keyword, String fullToken, String name, Set<String> declaredNames) {
        if (!declaredNames.add(name)) {
            throw new IllegalArgumentException("Duplicate argument name '" + name + "' in token '" + fullToken
                    + "' of syntax for '" + keyword + "'. Argument names must be unique, they are used to "
                    + "resolve 'name:value' tokens.");
        }
    }

    private static ArgType parseType(String fullToken, String typeToken) {
        try {
            return ArgType.fromToken(typeToken);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid type '" + typeToken + "' in token '" + fullToken + "'. "
                    + "Supported types: string, int, float, boolean.");
        }
    }

    private static Object parseDefault(String fullToken, ArgType type, String rawDefault) {
        if (rawDefault == null || rawDefault.isEmpty()) {
            return switch (type) {
                case STRING -> "";
                case INT -> 0;
                case FLOAT -> 0.0f;
                case BOOLEAN ->
                    throw new IllegalArgumentException(
                            "Boolean argument '" + fullToken + "' requires an explicit default value (true or false).");
            };
        }
        try {
            return type.parse(rawDefault);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid default value '" + rawDefault + "' for type "
                    + type.name().toLowerCase() + " in token '" + fullToken + "'.");
        }
    }
}
