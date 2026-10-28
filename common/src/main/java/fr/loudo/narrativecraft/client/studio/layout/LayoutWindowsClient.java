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

package fr.loudo.narrativecraft.client.studio.layout;

import com.mojang.blaze3d.pipeline.RenderTarget;
import fr.loudo.narrativecraft.client.imgui.ImGuiClient;
import fr.loudo.narrativecraft.client.imgui.ImGuiRenderableClient;
import imgui.ImGui;
import imgui.ImGuiIO;
import imgui.ImGuiViewport;
import imgui.flag.ImGuiDir;
import imgui.flag.ImGuiDockNodeFlags;
import imgui.internal.ImGuiDockNode;
import imgui.type.ImInt;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.Minecraft;

public class LayoutWindowsClient implements ImGuiRenderableClient {

    public static final String DOCKSPACE_ID = "studio_dockspace";
    private static final LayoutWindowsClient INSTANCE = new LayoutWindowsClient();

    private final List<LayoutWindowClient> layouts = new ArrayList<>();
    private final OutlinerLayoutClient outliner = new OutlinerLayoutClient();
    private final AssetsLayoutClient assets = new AssetsLayoutClient();
    private final ConsoleLayoutClient console = new ConsoleLayoutClient();
    private final InspectorLayoutClient inspector = new InspectorLayoutClient();
    private int gameX;
    private int gameY;
    private int gameWidth;
    private int gameHeight;
    private boolean gameAreaActive;

    public LayoutWindowsClient() {
        init();
    }

    public void render(ImGuiIO io) {
        setupDockspace();
        for (LayoutWindowClient layout : layouts) {
            if (!layout.isVisible()) continue;
            layout.render(io);
        }
    }

    private void init() {
        layouts.add(outliner);
        layouts.add(assets);
        layouts.add(console);
        layouts.add(inspector);
    }

    private void setupDockspace() {
        ImGuiViewport viewport = ImGui.getMainViewport();
        int id = ImGui.getID(DOCKSPACE_ID);
        if (imgui.internal.ImGui.dockBuilderGetNode(id).isNotValidPtr()) {
            imgui.internal.ImGui.dockBuilderAddNode(id, imgui.internal.flag.ImGuiDockNodeFlags.DockSpace);
            imgui.internal.ImGui.dockBuilderSetNodeSize(id, viewport.getWorkSizeX(), viewport.getWorkSizeY());
            ImInt left = new ImInt();
            ImInt leftBottom = new ImInt();
            ImInt right = new ImInt();
            ImInt bottom = new ImInt();
            ImInt center = new ImInt();
            imgui.internal.ImGui.dockBuilderSplitNode(id, ImGuiDir.Left, 0.2f, left, center);
            imgui.internal.ImGui.dockBuilderSplitNode(center.get(), ImGuiDir.Right, 0.25f, right, center);
            imgui.internal.ImGui.dockBuilderSplitNode(center.get(), ImGuiDir.Down, 0.25f, bottom, center);
            imgui.internal.ImGui.dockBuilderSplitNode(left.get(), ImGuiDir.Down, 0.4f, leftBottom, left);
            placeLayouts(left, leftBottom, bottom, right);
            imgui.internal.ImGui.dockBuilderFinish(id);
        }
        keepLayoutRatios(imgui.internal.ImGui.dockBuilderGetNode(id), viewport);
        ImGui.dockSpaceOverViewport(id, viewport, ImGuiDockNodeFlags.PassthruCentralNode);
        updateGameArea(id, viewport);
    }

    private void keepLayoutRatios(ImGuiDockNode root, ImGuiViewport viewport) {
        float oldWidth = root.getSizeRefX() > 0 ? root.getSizeRefX() : root.getSizeX();
        float oldHeight = root.getSizeRefY() > 0 ? root.getSizeRefY() : root.getSizeY();
        float newWidth = viewport.getWorkSizeX();
        float newHeight = viewport.getWorkSizeY();
        if (oldWidth <= 0 || oldHeight <= 0 || newWidth <= 0 || newHeight <= 0) return;
        if (oldWidth == newWidth && oldHeight == newHeight) return;
        scaleSizeRef(root, newWidth / oldWidth, newHeight / oldHeight);
    }

    private void scaleSizeRef(ImGuiDockNode node, float scaleX, float scaleY) {
        if (node == null || node.isNotValidPtr()) return;
        node.setSizeRef(node.getSizeRefX() * scaleX, node.getSizeRefY() * scaleY);
        scaleSizeRef(node.getChildNodeFirst(), scaleX, scaleY);
        scaleSizeRef(node.getChildNodeSecond(), scaleX, scaleY);
    }

    private void updateGameArea(int dockspaceId, ImGuiViewport viewport) {
        ImGuiDockNode centralNode = imgui.internal.ImGui.dockBuilderGetCentralNode(dockspaceId);
        if (centralNode.isNotValidPtr()) return;
        float scaleX = ImGui.getIO().getDisplayFramebufferScaleX();
        float scaleY = ImGui.getIO().getDisplayFramebufferScaleY();
        gameX = Math.round((centralNode.getPosX() - viewport.getPosX()) * scaleX);
        gameY = Math.round((centralNode.getPosY() - viewport.getPosY()) * scaleY);
        int width = Math.round(centralNode.getSizeX() * scaleX);
        int height = Math.round(centralNode.getSizeY() * scaleY);
        if (width == gameWidth && height == gameHeight) return;
        gameWidth = width;
        gameHeight = height;
        if (gameAreaActive) {
            Minecraft.getInstance().resizeGui();
        }
    }

    public void setGameAreaActive(boolean active) {
        gameAreaActive = active;
        Minecraft.getInstance().resizeGui();
    }

    public boolean hasGameArea() {
        return gameAreaActive && gameWidth > 0 && gameHeight > 0;
    }

    public void drawGame(RenderTarget output) {
        RenderTarget game = Minecraft.getInstance().gameRenderer.mainRenderTarget();
        int width = Math.min(game.width, output.width - gameX);
        int height = Math.min(game.height, output.height - gameY);
        if (width <= 0 || height <= 0) return;
        ImGuiClient.getInstance()
                .copyTexture(
                        game.getColorTexture(),
                        output.getColorTexture(),
                        0,
                        game.height - height,
                        gameX,
                        output.height - gameY - height,
                        width,
                        height);
    }

    private void placeLayouts(ImInt left, ImInt leftBottom, ImInt bottom, ImInt right) {
        imgui.internal.ImGui.dockBuilderDockWindow(outliner.id(), left.get());
        imgui.internal.ImGui.dockBuilderDockWindow(assets.id(), leftBottom.get());
        imgui.internal.ImGui.dockBuilderDockWindow(console.id(), bottom.get());
        imgui.internal.ImGui.dockBuilderDockWindow(inspector.id(), right.get());
    }

    public OutlinerLayoutClient getOutliner() {
        return outliner;
    }

    public int getGameX() {
        return gameX;
    }

    public int getGameY() {
        return gameY;
    }

    public int getGameWidth() {
        return gameWidth;
    }

    public int getGameHeight() {
        return gameHeight;
    }

    public List<LayoutWindowClient> getLayouts() {
        return layouts;
    }

    public static LayoutWindowsClient getInstance() {
        return INSTANCE;
    }
}
