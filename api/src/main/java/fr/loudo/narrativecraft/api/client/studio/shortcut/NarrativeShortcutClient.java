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

package fr.loudo.narrativecraft.api.client.studio.shortcut;

import java.util.Objects;
import net.minecraft.resources.Identifier;

/**
 * A keyboard shortcut of the NarrativeCraft studio, triggered wherever the focus is in the studio.
 * <p>
 * A text field being edited keeps the shortcuts it uses itself (e.g. {@code Ctrl+Z} undoes the typing).
 */
public interface NarrativeShortcutClient {

    /**
     * @return the unique id of the shortcut, namespaced with your mod id (e.g. {@code myaddon:export}).
     */
    Identifier id();

    /**
     * @return the ImGui key chord: a key combined with its modifiers,
     * e.g. {@code ImGuiKey.ImGuiMod_Ctrl | ImGuiKey.N}
     */
    int chord();

    /**
     * Called once when the shortcut is pressed.
     * <p>
     * If this method throws, it logs an error and the shortcut is disabled until the game restarts.
     */
    void run();

    /**
     * Creates a shortcut running {@code action} when pressed.
     */
    static NarrativeShortcutClient of(Identifier id, int chord, Runnable action) {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(action, "action");
        return new NarrativeShortcutClient() {
            @Override
            public Identifier id() {
                return id;
            }

            @Override
            public int chord() {
                return chord;
            }

            @Override
            public void run() {
                action.run();
            }
        };
    }
}
