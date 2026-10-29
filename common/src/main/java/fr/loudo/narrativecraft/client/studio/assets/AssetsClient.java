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

import fr.loudo.narrativecraft.api.util.Icons;
import fr.loudo.narrativecraft.client.imgui.ImGuiRenderableClient;
import fr.loudo.narrativecraft.client.network.C2SRequestOpenFile;
import fr.loudo.narrativecraft.client.studio.shortcut.StudioShortcutsClient;
import fr.loudo.narrativecraft.platform.Services;
import fr.loudo.narrativecraft.studio.assets.Assets;
import fr.loudo.narrativecraft.studio.assets.Tree;
import imgui.ImGui;
import imgui.ImGuiIO;
import imgui.flag.ImGuiMouseButton;
import imgui.flag.ImGuiTreeNodeFlags;

public class AssetsClient implements ImGuiRenderableClient {

    private static final int BASE_FLAGS = ImGuiTreeNodeFlags.SpanAvailWidth | ImGuiTreeNodeFlags.DrawLinesToNodes;

    private Tree root;
    private String hash = "";

    @Override
    public void render(ImGuiIO io) {
        if (root == null) return;
        renderRefreshButton();
        for (Tree tree : root.getChild().stream().sorted(Assets.ORDER).toList()) {
            renderTree(tree);
        }
    }

    private void renderRefreshButton() {
        float buttonWidth =
                ImGui.calcTextSizeX(Icons.ROTATE_LEFT) + ImGui.getStyle().getFramePaddingX() * 2;
        ImGui.setCursorPosX(ImGui.getCursorPosX() + ImGui.getContentRegionAvailX() - buttonWidth);
        if (ImGui.button(Icons.ROTATE_LEFT)) {
            StudioShortcutsClient.REFRESH.run();
        }
    }

    private void renderTree(Tree tree) {
        int flags = BASE_FLAGS;
        if (tree.isDirectory()) {
            flags |= ImGuiTreeNodeFlags.OpenOnArrow | ImGuiTreeNodeFlags.OpenOnDoubleClick;
            if (tree.getChild().isEmpty()) {
                flags |= ImGuiTreeNodeFlags.Leaf;
            }
        } else {
            flags |= ImGuiTreeNodeFlags.Leaf | ImGuiTreeNodeFlags.NoTreePushOnOpen;
        }

        boolean open = ImGui.treeNodeEx("/" + tree.getPath(), flags, tree.resolveName());
        if (ImGui.isItemHovered() && ImGui.isMouseDoubleClicked(ImGuiMouseButton.Left) && !ImGui.isItemToggledOpen()) {
            Services.PACKET.sendToServer(new C2SRequestOpenFile(tree.getPath()));
        }
        if (!open || !tree.isDirectory()) return;

        for (Tree child : tree.getChild().stream().sorted(Assets.ORDER).toList()) {
            renderTree(child);
        }
        ImGui.treePop();
    }

    public Tree getRoot() {
        return root;
    }

    public void setRoot(Tree root) {
        this.root = root;
    }

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }
}
