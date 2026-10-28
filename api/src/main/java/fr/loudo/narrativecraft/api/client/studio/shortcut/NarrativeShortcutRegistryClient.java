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

import net.minecraft.resources.Identifier;
import org.jspecify.annotations.Nullable;

/**
 * Registry of the studio keyboard shortcuts, use it to add your own {@link NarrativeShortcutClient}.
 * <p>
 * Register your shortcuts during your client initialization: the registry is locked
 * the first time the studio opens.
 */
public interface NarrativeShortcutRegistryClient {

    /**
     * Register your shortcut.
     *
     * @param shortcut shortcut to register
     * @throws IllegalArgumentException if a shortcut with the same id or the same chord is already registered,
     * if its chord has no key, or if its id uses the {@code narrativecraft} namespace
     * @throws IllegalStateException if the studio has already been opened
     */
    void register(NarrativeShortcutClient shortcut);

    /**
     * Text to display next to a menu item triggering the same action,
     * e.g. {@code ImGui.menuItem(label, shortcuts.hint(id))}.
     * <p>
     * Call it while rendering the studio: the key names come from ImGui.
     *
     * @param id id of a registered shortcut
     * @return the chord as text (e.g. {@code Ctrl+Shift+N}), or null if no shortcut has this id
     * @throws IllegalStateException if ImGui is not initialized yet
     */
    @Nullable
    String hint(Identifier id);
}
