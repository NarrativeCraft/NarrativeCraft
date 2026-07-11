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

package fr.loudo.narrativecraft.client.settings;

import fr.loudo.narrativecraft.NarrativeCraftMod;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

public class NarrativeClientSettings {

    private static final String FILE_NAME = "narrativecraft.properties";
    private static final Properties PROPS = new Properties();
    private static Path configFile;

    public static float textSpeed = 1.5f;
    public static boolean autoSkip = false;
    public static String storyLocale = "";

    public static void init(Path configDir) {
        configFile = configDir.resolve(FILE_NAME);

        if (Files.exists(configFile)) {
            try {
                load();
                loadFromProps();
            } catch (IOException e) {
                NarrativeCraftMod.LOGGER.error("Failed to load client config!", e);
            }
        } else {
            try {
                save();
            } catch (IOException e) {
                NarrativeCraftMod.LOGGER.error("Failed to save default client config!", e);
            }
        }
    }

    public static void save() throws IOException {
        setToProps();
        try (OutputStream out = Files.newOutputStream(configFile)) {
            PROPS.store(out, "NarrativeCraft Configuration");
        }
    }

    private static void load() throws IOException {
        try (InputStream in = Files.newInputStream(configFile)) {
            PROPS.load(in);
        }
    }

    private static void loadFromProps() {
        textSpeed = Float.parseFloat(PROPS.getProperty("textSpeed", "1.5"));
        autoSkip = Boolean.parseBoolean(PROPS.getProperty("autoSkip", "false"));
        storyLocale = PROPS.getProperty("storyLocale", "");
    }

    private static void setToProps() {
        PROPS.setProperty("textSpeed", String.valueOf(textSpeed));
        PROPS.setProperty("autoSkip", String.valueOf(autoSkip));
        PROPS.setProperty("storyLocale", storyLocale);
    }
}
