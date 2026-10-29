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

package fr.loudo.narrativecraft.client.studio.tab;

import fr.loudo.narrativecraft.api.util.NarrativeCraftAPIUtil;
import fr.loudo.narrativecraft.files.NarrativeFiles;
import fr.loudo.narrativecraft.narrative.scene.Scene;
import imgui.ImGui;
import imgui.ImGuiViewport;
import imgui.flag.ImGuiStyleVar;
import imgui.flag.ImGuiTabBarFlags;
import imgui.flag.ImGuiTabItemFlags;
import imgui.flag.ImGuiWindowFlags;
import imgui.type.ImBoolean;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;

public class TabsManagerClient {

    private static final Minecraft MINECRAFT = Minecraft.getInstance();
    private static final NarrativeFiles FILES = NarrativeFiles.getInstance();

    private static final int TABS_BAR_FLAGS = ImGuiWindowFlags.NoDecoration
            | ImGuiWindowFlags.NoMove
            | ImGuiWindowFlags.NoSavedSettings
            | ImGuiWindowFlags.NoDocking
            | ImGuiWindowFlags.MenuBar;

    private final List<Tab> tabs = new ArrayList<>();
    private Tab activeTab;
    private boolean focusActiveTab;
    private float tabsWidth;

    public void renderTabsBar() {
        ImGuiViewport viewport = ImGui.getMainViewport();
        float height = ImGui.getFrameHeight();
        ImGui.setNextWindowPos(viewport.getWorkPosX(), viewport.getWorkPosY());
        ImGui.setNextWindowSize(viewport.getWorkSizeX(), height);
        ImGui.setNextWindowViewport(viewport.getID());
        ImGui.pushStyleVar(ImGuiStyleVar.WindowRounding, 0);
        ImGui.pushStyleVar(ImGuiStyleVar.WindowBorderSize, 0);
        ImGui.pushStyleVar(ImGuiStyleVar.WindowMinSize, 0, 0);
        boolean visible = ImGui.begin("##tabs_bar", TABS_BAR_FLAGS);
        ImGui.popStyleVar(3);
        if (visible && ImGui.beginMenuBar()) {
            renderTabs();
            ImGui.endMenuBar();
        }
        ImGui.end();
        viewport.setWorkPos(viewport.getWorkPosX(), viewport.getWorkPosY() + height);
        viewport.setWorkSize(viewport.getWorkSizeX(), viewport.getWorkSizeY() - height);
    }

    private void renderTabs() {
        float startX = Math.max(ImGui.getCursorPosX(), (ImGui.getWindowWidth() - tabsWidth) * 0.5f);
        ImGui.setCursorPosX(startX);
        if (!ImGui.beginTabBar("##scenes", ImGuiTabBarFlags.Reorderable | ImGuiTabBarFlags.FittingPolicyScroll)) {
            return;
        }
        float maxX = ImGui.getWindowPosX() + startX;
        boolean focusing = focusActiveTab;
        for (Tab tab : List.copyOf(tabs)) {
            ImBoolean open = new ImBoolean(true);
            int flags = tab.isSaved() ? ImGuiTabItemFlags.None : ImGuiTabItemFlags.UnsavedDocument;
            if (focusActiveTab && tab.equals(activeTab)) {
                flags |= ImGuiTabItemFlags.SetSelected;
                focusActiveTab = false;
            }
            Scene scene = tab.getScene();
            if (ImGui.beginTabItem(
                    NarrativeCraftAPIUtil.imGuiLabel(
                            scene.getTabName(), scene.getId().toString()),
                    open,
                    flags)) {
                if (!focusing) {
                    switchTo(tab);
                }
                ImGui.endTabItem();
            }
            maxX = Math.max(maxX, ImGui.getItemRectMaxX());
            if (!open.get()) {
                close(tab);
            }
        }
        tabsWidth = maxX - ImGui.getWindowPosX() - startX;
        ImGui.endTabBar();
    }

    public void open(Scene scene) {
        Tab tab = getTabByScene(scene);
        if (tab == null) {
            tab = new Tab(scene, !scene.isNew());
            tabs.add(tab);
        }
        switchTo(tab);
        focusActiveTab = true;
    }

    public void saveCurrent(@Nullable Path path) {
        if (activeTab == null) return;

        if (activeTab.getPath() == null) {
            saveAs(path);
        } else {
            save();
        }
    }

    public void saveAs(Path path) {
        if (activeTab == null) return;

        // TODO: move this as a packet!!!
        if (FILES.getSceneFiles().save(activeTab.getScene(), path)) {
            activeTab.setSaved(true);
            activeTab.setPath(path);
        }
    }

    public void save() {
        if (activeTab == null) return;
        if (activeTab.isSaved()) return;
        if (activeTab.getPath() == null) return;

        // TODO: move this as a packet!!!
        if (FILES.getSceneFiles().save(activeTab.getScene(), activeTab.getPath())) {
            activeTab.setSaved(true);
        }
    }

    private void switchTo(Tab tab) {
        if (Objects.equals(tab, activeTab)) return;
        LocalPlayer player = MINECRAFT.player;
        if (activeTab != null && player != null) {
            activeTab.getSession().capture(player);
        }
        activeTab = tab;
        if (tab != null && player != null) {
            tab.getSession().restore(player);
        }
    }

    public void openNew() {
        open(Scene.createNew());
    }

    public void close(Scene scene) {
        Tab tab = getTabByScene(scene);
        if (tab == null) return;
        close(tab);
    }

    public void close(Tab tab) {
        if (tab.equals(activeTab)) {
            activeTab = null;
        }
        if (!tab.isSaved()) {
            // TODO: show confirmation popin
        }
        tabs.remove(tab);
    }

    public Tab getTabByScene(UUID id) {
        for (Tab tab : tabs) {
            if (id.equals(tab.getScene().getId())) {
                return tab;
            }
        }
        return null;
    }

    public Tab getTabByScene(Scene scene) {
        for (Tab tab : tabs) {
            if (tab.getScene().getId().equals(scene.getId())) {
                return tab;
            }
        }
        return null;
    }

    public Tab getActiveTab() {
        return activeTab;
    }

    public void setActiveTab(Tab activeTab) {
        switchTo(activeTab);
    }

    public List<Tab> getTabs() {
        return tabs;
    }

    public List<Tab> getUnsavedTabs() {
        return tabs.stream().filter(tab -> !tab.isSaved()).toList();
    }
}
