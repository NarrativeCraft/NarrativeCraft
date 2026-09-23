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

import fr.loudo.narrativecraft.NarrativeCraftMod;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.UUID;

public class FileTransaction {

    public record Move(File source, File target) {}

    @FunctionalInterface
    private interface Undo {
        void run() throws IOException;
    }

    private final Deque<Undo> undoStack = new ArrayDeque<>();
    private final List<File> pendingDeletions = new ArrayList<>();

    public void createDirectories(File directory) throws IOException {
        if (directory.isDirectory()) return;
        File topmostCreated = directory;
        while (topmostCreated.getParentFile() != null
                && !topmostCreated.getParentFile().exists()) {
            topmostCreated = topmostCreated.getParentFile();
        }
        Files.createDirectories(directory.toPath());
        File created = topmostCreated;
        undoStack.push(() -> deleteRecursively(created));
    }

    public void write(File target, NarrativeCraftFileWriter.WriterContent content) throws IOException {
        byte[] previousContent = target.isFile() ? Files.readAllBytes(target.toPath()) : null;
        NarrativeCraftFileWriter.write(target, content);
        undoStack.push(() -> {
            if (previousContent == null) {
                Files.deleteIfExists(target.toPath());
            } else {
                Files.write(target.toPath(), previousContent);
            }
        });
    }

    public void writeString(File target, String content) throws IOException {
        write(target, writer -> writer.write(content));
    }

    public void move(File source, File target) throws IOException {
        if (source.equals(target)) return;
        if (target.exists()) {
            throw new IOException("Cannot move " + source + " to " + target + ": target already exists");
        }
        Files.move(source.toPath(), target.toPath());
        undoStack.push(() -> Files.move(target.toPath(), source.toPath()));
    }

    public void moveAll(List<Move> moves) throws IOException {
        List<Move> stagedMoves = new ArrayList<>();
        for (Move move : moves) {
            if (move.source().equals(move.target())) continue;
            File staging = temporarySibling(move.source());
            move(move.source(), staging);
            stagedMoves.add(new Move(staging, move.target()));
        }
        for (Move stagedMove : stagedMoves) {
            move(stagedMove.source(), stagedMove.target());
        }
    }

    public void delete(File target) throws IOException {
        if (!target.exists()) return;
        File trash = temporarySibling(target);
        move(target, trash);
        pendingDeletions.add(trash);
    }

    public void commit() {
        undoStack.clear();
        for (File pendingDeletion : pendingDeletions) {
            if (!deleteRecursively(pendingDeletion)) {
                NarrativeCraftMod.LOGGER.warn("Failed to delete {}", pendingDeletion);
            }
        }
        pendingDeletions.clear();
    }

    public void rollback() {
        while (!undoStack.isEmpty()) {
            Undo undo = undoStack.pop();
            try {
                undo.run();
            } catch (IOException e) {
                NarrativeCraftMod.LOGGER.error("Failed to roll back a file operation", e);
            }
        }
        pendingDeletions.clear();
    }

    private static File temporarySibling(File file) {
        return new File(file.getParentFile(), file.getName() + "." + UUID.randomUUID() + ".tmp");
    }

    private static boolean deleteRecursively(File file) {
        File[] children = file.listFiles();
        if (children != null) {
            for (File child : children) {
                deleteRecursively(child);
            }
        }
        return file.delete();
    }
}
