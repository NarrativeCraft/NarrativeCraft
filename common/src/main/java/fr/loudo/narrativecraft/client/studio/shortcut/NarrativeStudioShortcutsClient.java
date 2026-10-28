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

package fr.loudo.narrativecraft.client.studio.shortcut;

import fr.loudo.narrativecraft.NarrativeCraftMod;
import fr.loudo.narrativecraft.api.client.studio.shortcut.NarrativeShortcutClient;
import fr.loudo.narrativecraft.client.imgui.ImGuiRenderableClient;
import fr.loudo.narrativecraft.registry.NarrativeShortcutRegistryImpl;
import imgui.ImGui;
import imgui.ImGuiIO;
import java.util.ArrayList;
import java.util.List;

public class NarrativeStudioShortcutsClient implements ImGuiRenderableClient {

    // ImGuiInputFlags_RouteGlobal, imgui-java has no ImGuiInputFlags class
    private static final int ROUTE_GLOBAL = 1 << 12;

    private final List<ShortcutState> shortcuts = new ArrayList<>();
    private boolean loaded;

    @Override
    public void render(ImGuiIO io) {
        if (!loaded) {
            loadShortcuts();
        }
        for (ShortcutState state : shortcuts) {
            if (state.disabled || !ImGui.shortcut(state.shortcut.chord(), ROUTE_GLOBAL)) continue;
            try {
                state.shortcut.run();
            } catch (Exception | LinkageError e) {
                NarrativeCraftMod.LOGGER.error(
                        "Shortcut {} crashed and is disabled until the game restarts", state.shortcut.id(), e);
                state.disabled = true;
            }
        }
    }

    private void loadShortcuts() {
        loaded = true;
        for (NarrativeShortcutClient shortcut :
                NarrativeShortcutRegistryImpl.getInstance().freeze()) {
            shortcuts.add(new ShortcutState(shortcut));
        }
    }

    private static final class ShortcutState {
        private final NarrativeShortcutClient shortcut;
        private boolean disabled;

        private ShortcutState(NarrativeShortcutClient shortcut) {
            this.shortcut = shortcut;
        }
    }
}
