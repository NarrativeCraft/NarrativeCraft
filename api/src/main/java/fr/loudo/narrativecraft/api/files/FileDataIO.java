/*
 * NarrativeCraft - Create narrative games inside Minecraft. No coding, no game engine, only text and logic.
 * Copyright (c) 2026 LOUDO and contributors
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

package fr.loudo.narrativecraft.api.files;

import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

/**
 * Versioned JSON serializer of a type of data, used by {@link FileData}.
 * <p>
 * Every JSON written gets a {@value #VERSION_KEY} key holding the current {@link #version()}.
 * When reading, this version is given to {@link #deserialize} so old files can still be loaded
 * after the format changes: bump {@link #version()} and handle the older versions there.
 *
 * @param <T> type of the data to serialize
 */
public abstract class FileDataIO<T> {

    /**
     * Key of the format version in the JSON, reserved: {@link #serialize} must not use it.
     */
    public static final String VERSION_KEY = "version";

    /**
     * @return the current format version, to increase every time the JSON format changes
     */
    protected abstract int version();

    /**
     * Converts the data to JSON, in the current format.
     *
     * @param data data to convert
     * @return the JSON of the data, without the {@value #VERSION_KEY} key
     */
    protected abstract JsonObject serialize(T data);

    /**
     * Converts back JSON written by {@link #serialize}, in the current format or an older one.
     *
     * @param object JSON to convert, still containing the {@value #VERSION_KEY} key
     * @param version format version of the JSON, never higher than {@link #version()}
     * @return the data read from the JSON
     */
    protected abstract T deserialize(JsonObject object, int version);

    /**
     * Converts the data to JSON and adds the {@value #VERSION_KEY} key in first position.
     *
     * @param data data to convert
     * @return the JSON to write to the file
     * @throws IllegalStateException if {@link #serialize} used the reserved {@value #VERSION_KEY} key
     */
    public final JsonObject write(T data) {
        JsonObject content = serialize(data);
        if (content.has(VERSION_KEY)) {
            throw new IllegalStateException("\"" + VERSION_KEY + "\" is a reserved key");
        }
        JsonObject root = new JsonObject();
        root.addProperty(VERSION_KEY, version());
        content.entrySet().forEach(e -> root.add(e.getKey(), e.getValue()));
        return root;
    }

    /**
     * Checks the {@value #VERSION_KEY} key of the JSON then converts it back with {@link #deserialize}.
     *
     * @param json JSON read from the file
     * @return the data read from the JSON
     * @throws JsonParseException if the {@value #VERSION_KEY} key is missing, or if the file was written
     *     by a newer format version than {@link #version()}
     */
    public final T read(JsonObject json) {
        if (!json.has(VERSION_KEY)) {
            throw new JsonParseException("Missing \"" + VERSION_KEY + "\" key");
        }
        int fileVersion = json.get(VERSION_KEY).getAsInt();
        if (fileVersion > version()) {
            throw new JsonParseException("File version " + fileVersion + " is newer than supported " + version());
        }
        return deserialize(json, fileVersion);
    }
}
