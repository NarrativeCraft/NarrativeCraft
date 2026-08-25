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

package fr.loudo.narrativecraft.server.settings;

import fr.loudo.narrativecraft.NarrativeCraftMod;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

public class NarrativeServerSettings {

    private static final String FILE_NAME = "world_settings.properties";
    private static final Properties PROPS = new Properties();
    private static Path settingsFile;

    public static final String DEFAULT_LOCALE = "en_us";

    public static boolean showMainScreenOnJoin = false;
    public static boolean showNametagGlobalCharacter = true;
    public static String defaultLocale = DEFAULT_LOCALE;

    public static void init(Path dataDir) {
        settingsFile = dataDir.resolve(FILE_NAME);

        if (Files.exists(settingsFile)) {
            try {
                load();
                loadFromProps();
            } catch (IOException e) {
                NarrativeCraftMod.LOGGER.error("Failed to load server world settings!", e);
            }
        } else {
            try {
                save();
            } catch (IOException e) {
                NarrativeCraftMod.LOGGER.error("Failed to save default server world settings!", e);
            }
        }
    }

    public static void save() throws IOException {
        setToProps();
        try (OutputStream out = Files.newOutputStream(settingsFile)) {
            PROPS.store(out, "NarrativeCraft World Settings");
        }
    }

    private static void load() throws IOException {
        try (InputStream in = Files.newInputStream(settingsFile)) {
            PROPS.load(in);
        }
    }

    private static void loadFromProps() {
        showMainScreenOnJoin = Boolean.parseBoolean(PROPS.getProperty("showMainScreenOnJoin", "false"));
        showNametagGlobalCharacter = Boolean.parseBoolean(PROPS.getProperty("showNametagGlobalCharacter", "true"));
        defaultLocale = PROPS.getProperty("defaultLocale", DEFAULT_LOCALE);
    }

    private static void setToProps() {
        PROPS.setProperty("showMainScreenOnJoin", String.valueOf(showMainScreenOnJoin));
        PROPS.setProperty("showNametagGlobalCharacter", String.valueOf(showNametagGlobalCharacter));
        PROPS.setProperty("defaultLocale", defaultLocale);
    }
}
