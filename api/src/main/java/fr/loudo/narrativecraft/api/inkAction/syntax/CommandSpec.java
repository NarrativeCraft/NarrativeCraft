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

import java.util.*;

/**
 * Compiled representation of a command syntax declaration.
 *
 * <p>Built once at startup by {@link SyntaxParser} from the {@code syntax} field of an
 * {@link fr.loudo.narrativecraft.api.inkAction.InkCommand} annotation. Validates and extracts
 * a {@link ParsedCommand} from a list of raw tokens.
 *
 * <p>Error messages are intentionally precise so the syntax declaration can be fixed
 * without reading the source code.
 */
public final class CommandSpec {

    private final String keyword;
    private final List<ArgDef> positionalArgs;
    private final List<ArgDef> optionalPositionalArgs;
    private final Map<String, NamedArgDef> namedArgs;
    private final Map<String, FlagDef> flags;

    CommandSpec(
            String keyword,
            List<ArgDef> positionalArgs,
            List<ArgDef> optionalPositionalArgs,
            Map<String, NamedArgDef> namedArgs,
            Map<String, FlagDef> flags) {
        this.keyword = keyword;
        this.positionalArgs = positionalArgs;
        this.optionalPositionalArgs = optionalPositionalArgs;
        this.namedArgs = namedArgs;
        this.flags = flags;
    }

    /**
     * Validates and parses a list of tokens (keyword already removed) into a {@link ParsedCommand}.
     *
     * @throws IllegalArgumentException with a descriptive message if validation fails
     */
    public ParsedCommand parse(List<String> tokens) {
        Map<String, Object> args = new HashMap<>();
        Set<String> activeFlags = new HashSet<>();

        int positionalIndex = 0;
        int optionalPositionalIndex = 0;

        for (String token : tokens) {
            if (token.startsWith("--")) {
                String flagName = token.substring(2);
                if (!flags.containsKey(flagName)) {
                    throw new IllegalArgumentException("Unknown flag '--" + flagName + "' for tag '" + keyword + "'. "
                            + "Declared flags: " + flags.keySet());
                }
                activeFlags.add(flagName);

            } else if (token.contains(":") && namedArgs.containsKey(token.substring(0, token.indexOf(':')))) {
                int colonIndex = token.indexOf(':');
                String name = token.substring(0, colonIndex);
                String rawValue = token.substring(colonIndex + 1);
                args.put(name, namedArgs.get(name).type().parse(rawValue));

            } else {
                if (positionalIndex < positionalArgs.size()) {
                    ArgDef def = positionalArgs.get(positionalIndex++);
                    args.put(def.name(), def.type().parse(token));
                } else if (optionalPositionalIndex < optionalPositionalArgs.size()) {
                    ArgDef def = optionalPositionalArgs.get(optionalPositionalIndex++);
                    args.put(def.name(), def.type().parse(token));
                } else {
                    throw new IllegalArgumentException("Too many positional arguments for tag '" + keyword + "'. "
                            + "Expected at most "
                            + (positionalArgs.size() + optionalPositionalArgs.size()) + ".");
                }
            }
        }

        if (positionalIndex < positionalArgs.size()) {
            ArgDef missing = positionalArgs.get(positionalIndex);
            throw new IllegalArgumentException("Missing required argument '" + missing.name() + "' of type '"
                    + missing.type().name().toLowerCase() + "' for tag '" + keyword + "'.");
        }

        for (NamedArgDef def : namedArgs.values()) {
            args.putIfAbsent(def.name(), def.defaultValue());
        }

        return new ParsedCommand(args, activeFlags);
    }
}
