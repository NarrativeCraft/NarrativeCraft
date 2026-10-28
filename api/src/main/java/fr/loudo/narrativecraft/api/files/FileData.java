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

import java.io.IOException;
import java.nio.file.Path;
import javax.annotation.Nullable;

/**
 * Saves and restores a type of data to and from its file on disk.
 * <p>
 * This class only handles the file side (where the file is, reading, writing).
 * Converting the data to JSON and back is done by its {@link #io()}.
 *
 * @param <T> type of the data stored in the file
 */
public interface FileData<T> {

    /**
     * Writes the data to its file, creating it if it doesn't exist yet.
     *
     * @param target data to save
     * @param path folder where to save the file, or {@code null} to save it at its default location
     * @return {@code true} if the file was written, {@code false} if it failed
     * @throws IOException if writing the file failed and the implementation doesn't handle it itself
     */
    boolean save(T target, @Nullable Path path) throws IOException;

    /**
     * Reads the data back from a file written by {@link #save}.
     *
     * @param path path of the file to read
     * @return the data read from the file, or {@code null} if the file couldn't be read or parsed
     */
    T restore(Path path);

    /**
     * @return the serializer converting the data to the JSON stored in the file and back
     */
    FileDataIO<T> io();
}
