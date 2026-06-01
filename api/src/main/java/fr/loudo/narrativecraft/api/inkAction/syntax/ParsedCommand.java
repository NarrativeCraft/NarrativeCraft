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

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * A validated, typed snapshot of a parsed Ink tag command.
 *
 * <p>Access values with {@link #get(String)} (returns the typed value as {@code Object}) or the
 * typed convenience methods {@link #getString}, {@link #getInt}, {@link #getFloat},
 * {@link #getBoolean}. Test boolean flags with {@link #flag(String)}.
 *
 * <p>Instances are created by {@link CommandSpec#parse} and transported across the network via
 * {@link #toJson()} / {@link #fromJson(String)} for client-side actions.
 */
public final class ParsedCommand {

    private final Map<String, Object> args;
    private final Set<String> flags;

    ParsedCommand(Map<String, Object> args, Set<String> flags) {
        this.args = args;
        this.flags = flags;
    }

    /**
     * Returns the typed value for {@code name}, or {@code null} if absent.
     * Cast to the expected type or use the typed helpers below.
     */
    @SuppressWarnings("unchecked")
    public <T> T get(String name) {
        return (T) args.get(name);
    }

    public String getString(String name) {
        return get(name);
    }

    public int getInt(String name) {
        Object value = args.get(name);
        return value instanceof Number n ? n.intValue() : 0;
    }

    public float getFloat(String name) {
        Object value = args.get(name);
        return value instanceof Number n ? n.floatValue() : 0f;
    }

    public boolean getBoolean(String name) {
        Object value = args.get(name);
        return value instanceof Boolean b && b;
    }

    /** Returns {@code true} if the flag {@code --name} was present in the tag. */
    public boolean flag(String name) {
        return flags.contains(name);
    }

    /**
     * Serialises this command to a compact JSON string for network transport.
     * Types are preserved via a type-tag so the client can reconstruct typed values.
     */
    public String toJson() {
        JsonObject root = new JsonObject();

        JsonObject argsObject = new JsonObject();
        for (Map.Entry<String, Object> entry : args.entrySet()) {
            JsonArray pair = new JsonArray(2);
            Object value = entry.getValue();
            if (value instanceof String s) {
                pair.add("s");
                pair.add(s);
            } else if (value instanceof Integer i) {
                pair.add("i");
                pair.add(i);
            } else if (value instanceof Float f) {
                pair.add("f");
                pair.add(f);
            } else if (value instanceof Boolean b) {
                pair.add("b");
                pair.add(b ? 1 : 0);
            }
            argsObject.add(entry.getKey(), pair);
        }
        root.add("args", argsObject);

        JsonArray flagsArray = new JsonArray();
        flags.forEach(flagsArray::add);
        root.add("flags", flagsArray);

        return root.toString();
    }

    /** Deserialises a {@link ParsedCommand} from the JSON produced by {@link #toJson()}. */
    public static ParsedCommand fromJson(String json) {
        JsonObject root = JsonParser.parseString(json).getAsJsonObject();

        Map<String, Object> args = new HashMap<>();
        JsonObject argsObject = root.getAsJsonObject("args");
        for (Map.Entry<String, com.google.gson.JsonElement> entry : argsObject.entrySet()) {
            JsonArray pair = entry.getValue().getAsJsonArray();
            String typeTag = pair.get(0).getAsString();
            Object value =
                    switch (typeTag) {
                        case "s" -> pair.get(1).getAsString();
                        case "i" -> pair.get(1).getAsInt();
                        case "f" -> pair.get(1).getAsFloat();
                        case "b" -> pair.get(1).getAsInt() != 0;
                        default -> null;
                    };
            if (value != null) args.put(entry.getKey(), value);
        }

        Set<String> flags = new HashSet<>();
        root.getAsJsonArray("flags").forEach(element -> flags.add(element.getAsString()));

        return new ParsedCommand(args, flags);
    }
}
