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

package fr.loudo.narrativecraft.api.client.studio.menu;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

/**
 * A menu of the NarrativeCraft studio menu bar.
 * <p>
 * The studio opens and closes the menu, the menu only draws its items with ImGui
 * ({@code ImGui.menuItem}, submenus with {@code ImGui.beginMenu}/{@code ImGui.endMenu}...).
 */
public interface NarrativeMenuClient {

    /**
     * @return the unique id of the menu, namespaced with your mod id (e.g. {@code myaddon:tools}).
     */
    Identifier id();

    /**
     * @return the name displayed on the menu bar
     */
    Component title();

    /**
     * Draws the menu content, called every frame while the menu is open.
     * <p>
     * Never call {@code ImGui.beginMenu}/{@code ImGui.endMenu} for this menu, the studio already does it.
     * If this method throws, it logs an error and the menu is disabled until the game restarts.
     */
    void render();
}
