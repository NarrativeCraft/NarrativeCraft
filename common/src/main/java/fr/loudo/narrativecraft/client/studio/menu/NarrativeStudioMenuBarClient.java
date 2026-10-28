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

package fr.loudo.narrativecraft.client.studio.menu;

import fr.loudo.narrativecraft.NarrativeCraftMod;
import fr.loudo.narrativecraft.api.client.studio.menu.NarrativeMenuClient;
import fr.loudo.narrativecraft.api.util.NarrativeCraftAPIUtil;
import fr.loudo.narrativecraft.client.imgui.ImGuiRenderableClient;
import fr.loudo.narrativecraft.client.studio.NarrativeStudioClient;
import fr.loudo.narrativecraft.registry.NarrativeMenuRegistryImpl;
import imgui.ImGui;
import imgui.ImGuiIO;
import imgui.flag.ImGuiCol;
import java.util.ArrayList;
import java.util.List;

public class NarrativeStudioMenuBarClient implements ImGuiRenderableClient {

    private final List<MenuState> menus = new ArrayList<>();
    private boolean loaded;

    @Override
    public void render(ImGuiIO io) {
        if (!loaded) {
            loadMenus();
        }
        if (!ImGui.beginMainMenuBar()) return;
        ImGui.textColored(ImGui.getColorU32(ImGuiCol.CheckMark), NarrativeCraftMod.MOD_NAME);
        for (MenuState state : menus) {
            renderMenu(state);
        }
        ImGui.endMainMenuBar();
        NarrativeStudioClient.getInstance().getTabsManager().renderTabsBar();
    }

    private void loadMenus() {
        loaded = true;
        for (NarrativeMenuClient menu : NarrativeMenuRegistryImpl.getInstance().freeze()) {
            menus.add(new MenuState(menu));
        }
    }

    private void renderMenu(MenuState state) {
        if (!ImGui.beginMenu(state.label(), !state.disabled)) return;
        try {
            state.menu.render();
        } catch (Exception | LinkageError e) {
            NarrativeCraftMod.LOGGER.error(
                    "Menu {} crashed and is disabled until the game restarts", state.menu.id(), e);
            state.disabled = true;
        } finally {
            ImGui.endMenu();
        }
    }

    private static final class MenuState {
        private final NarrativeMenuClient menu;
        private boolean disabled;

        private MenuState(NarrativeMenuClient menu) {
            this.menu = menu;
        }

        private String label() {
            return NarrativeCraftAPIUtil.imGuiLabel(
                    menu.title().getString(), menu.id().toString());
        }
    }
}
