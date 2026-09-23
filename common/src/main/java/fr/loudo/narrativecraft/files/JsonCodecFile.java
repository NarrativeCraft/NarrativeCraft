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

package fr.loudo.narrativecraft.files;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public final class JsonCodecFile {

    private static final Gson GSON = new Gson();

    private JsonCodecFile() {}

    public static <T> void write(FileTransaction transaction, File target, Codec<T> codec, T value) throws IOException {
        JsonElement json = encode(target, codec, value);
        transaction.write(target, writer -> GSON.toJson(json, writer));
    }

    public static <T> void write(File target, Codec<T> codec, T value) throws IOException {
        JsonElement json = encode(target, codec, value);
        NarrativeCraftFileWriter.write(target, writer -> GSON.toJson(json, writer));
    }

    public static <T> T read(File source, Codec<T> codec) throws IOException {
        JsonElement json;
        try {
            json = JsonParser.parseString(Files.readString(source.toPath()));
        } catch (JsonParseException exception) {
            throw new IOException("Malformed JSON in " + source, exception);
        }
        return codec.parse(JsonOps.INSTANCE, json)
                .getOrThrow(message -> new IOException("Invalid data in " + source + ": " + message));
    }

    private static <T> JsonElement encode(File target, Codec<T> codec, T value) throws IOException {
        return codec.encodeStart(JsonOps.INSTANCE, value)
                .getOrThrow(message -> new IOException("Could not encode " + target + ": " + message));
    }
}
