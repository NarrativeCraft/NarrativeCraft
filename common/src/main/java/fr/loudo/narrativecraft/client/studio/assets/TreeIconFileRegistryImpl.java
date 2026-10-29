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

package fr.loudo.narrativecraft.client.studio.assets;

import fr.loudo.narrativecraft.api.client.studio.assets.TreeIconFileRegistry;
import fr.loudo.narrativecraft.api.util.Icons;
import fr.loudo.narrativecraft.files.SceneFiles;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class TreeIconFileRegistryImpl implements TreeIconFileRegistry {

    private static final TreeIconFileRegistryImpl INSTANCE = new TreeIconFileRegistryImpl();

    private final Map<String, String> icons = new HashMap<>();

    private TreeIconFileRegistryImpl() {}

    public static void initialize() {
        INSTANCE.register(SceneFiles.FORMAT, Icons.CLAPPERBOARD);
    }

    @Override
    public synchronized void register(String extension, String icon) {
        Objects.requireNonNull(extension, "extension");
        Objects.requireNonNull(icon, "icon");
        if (icons.putIfAbsent(extension.toLowerCase(), icon) != null) {
            throw new IllegalArgumentException(
                    "An icon is already registered for the extension " + extension.toLowerCase());
        }
    }

    /**
     * Returns the icon registered for this extension, or {@code null} if there is none.
     */
    public synchronized String getIcon(String extension) {
        return icons.get(extension);
    }

    public static TreeIconFileRegistryImpl getInstance() {
        return INSTANCE;
    }
}
