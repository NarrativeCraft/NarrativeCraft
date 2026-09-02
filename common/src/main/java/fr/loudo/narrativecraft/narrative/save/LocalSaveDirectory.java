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

package fr.loudo.narrativecraft.narrative.save;

import fr.loudo.narrativecraft.NarrativeCraftMod;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import javax.annotation.Nullable;
import net.minecraft.util.Util;

public class LocalSaveDirectory {

    private static final String SAVES_DIRECTORY_NAME = "saves";

    private LocalSaveDirectory() {}

    @Nullable
    public static File ofStory(String storyId) {
        Path applicationDataDirectory = applicationDataDirectory();
        if (applicationDataDirectory == null) return null;

        Path storyDirectory = applicationDataDirectory
                .resolve(NarrativeCraftMod.MOD_ID)
                .resolve(SAVES_DIRECTORY_NAME)
                .resolve(storyId);
        try {
            Files.createDirectories(storyDirectory);
        } catch (IOException e) {
            NarrativeCraftMod.LOGGER.error("Failed to create local save directory {}", storyDirectory, e);
            return null;
        }

        return storyDirectory.toFile();
    }

    @Nullable
    private static Path applicationDataDirectory() {
        String userHome = System.getProperty("user.home");
        return switch (Util.getPlatform()) {
            case WINDOWS -> {
                Path localAppData = fromEnvironment("LOCALAPPDATA");
                if (localAppData != null) yield localAppData;
                yield userHome == null ? null : Path.of(userHome, "AppData", "Local");
            }
            case OSX -> userHome == null ? null : Path.of(userHome, "Library", "Application Support");
            default -> {
                Path xdgDataHome = fromEnvironment("XDG_DATA_HOME");
                if (xdgDataHome != null) yield xdgDataHome;
                yield userHome == null ? null : Path.of(userHome, ".local", "share");
            }
        };
    }

    @Nullable
    private static Path fromEnvironment(String variable) {
        String value = System.getenv(variable);
        if (value == null || value.isBlank()) return null;

        try {
            return Path.of(value);
        } catch (Exception e) {
            NarrativeCraftMod.LOGGER.error("Invalid path in environment variable {}: {}", variable, value, e);
            return null;
        }
    }
}
