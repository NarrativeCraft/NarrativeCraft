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

package fr.loudo.narrativecraft.client.screen;

import fr.loudo.narrativecraft.api.util.Icons;
import fr.loudo.narrativecraft.client.imgui.ImGuiRenderableClient;
import fr.loudo.narrativecraft.client.studio.NarrativeStudioClient;
import fr.loudo.narrativecraft.files.SceneFiles;
import fr.loudo.narrativecraft.studio.assets.Assets;
import fr.loudo.narrativecraft.studio.assets.Tree;
import fr.loudo.narrativecraft.util.ModTranslation;
import imgui.ImGui;
import imgui.ImGuiIO;
import imgui.ImGuiStyle;
import imgui.ImGuiViewport;
import imgui.flag.ImGuiChildFlags;
import imgui.flag.ImGuiCond;
import imgui.flag.ImGuiInputTextFlags;
import imgui.flag.ImGuiKey;
import imgui.flag.ImGuiMouseButton;
import imgui.flag.ImGuiSelectableFlags;
import imgui.flag.ImGuiWindowFlags;
import imgui.type.ImString;
import net.minecraft.client.gui.screens.Screen;

public class SaveAsSceneScreen extends Screen implements ImGuiRenderableClient {

    private static final String POPUP_ID = "###save_scene_as";
    private static final float WIDTH = 600.0F;
    private static final float HEIGHT = 400.0F;
    private static final int POPUP_FLAGS =
            ImGuiWindowFlags.NoResize | ImGuiWindowFlags.NoMove | ImGuiWindowFlags.NoSavedSettings;
    private static final String ROOT_LABEL = "assets://";
    private static final String DEFAULT_NAME = "new_scene";

    private final ImString fileName = new ImString(DEFAULT_NAME + SceneFiles.FORMAT, 256);
    private final ImString pathDisplay = new ImString(256);
    private String currentPath = "";

    public SaveAsSceneScreen() {
        super(ModTranslation.message("studio.save_scene.title"));
    }

    @Override
    public void render(ImGuiIO io) {
        String popupId = getTitle().getString() + POPUP_ID;
        if (!ImGui.isPopupOpen(popupId)) {
            ImGui.openPopup(popupId);
        }
        ImGuiViewport viewport = ImGui.getMainViewport();
        ImGui.setNextWindowPos(viewport.getCenterX(), viewport.getCenterY(), ImGuiCond.Always, 0.5F, 0.5F);
        ImGui.setNextWindowSize(WIDTH, HEIGHT, ImGuiCond.Appearing);
        if (!ImGui.beginPopupModal(popupId, POPUP_FLAGS)) return;

        Tree directory = currentDirectory();
        renderPathBar(directory);
        boolean confirmed = renderEntries(directory);
        confirmed |= renderFileName();
        confirmed |= renderButtons();
        if (confirmed && isNameValid()) {
            save();
        }
        ImGui.endPopup();
    }

    private void renderPathBar(Tree directory) {
        ImGui.beginDisabled(directory == null || directory.getParent() == null);
        if (ImGui.button(Icons.ARROW_UP) && directory != null) {
            currentPath = directory.getParent().getPath();
        }
        ImGui.endDisabled();
        ImGui.sameLine();
        ImGui.alignTextToFramePadding();
        ImGui.text(translate("path"));
        ImGui.sameLine();
        ImGui.setNextItemWidth(-1);
        pathDisplay.set(ROOT_LABEL + currentPath);
        ImGui.inputText("##path", pathDisplay, ImGuiInputTextFlags.ReadOnly);
    }

    private boolean renderEntries(Tree directory) {
        ImGui.text(translate("entries"));
        float footerHeight = ImGui.getFrameHeightWithSpacing() * 2;
        boolean confirmed = false;
        if (ImGui.beginChild("##entries", 0, -footerHeight, ImGuiChildFlags.Borders) && directory != null) {
            for (Tree tree : directory.getChild().stream()
                    .filter(SaveAsSceneScreen::isShown)
                    .sorted(Assets.ORDER)
                    .toList()) {
                boolean selected = !tree.isDirectory() && tree.getName().equals(fileName.get());
                if (!ImGui.selectable(tree.resolveName(), selected, ImGuiSelectableFlags.AllowDoubleClick)) continue;

                boolean doubleClicked = ImGui.isMouseDoubleClicked(ImGuiMouseButton.Left);
                if (tree.isDirectory()) {
                    if (doubleClicked) currentPath = tree.getPath();
                } else {
                    fileName.set(tree.getName());
                    confirmed = doubleClicked;
                }
            }
        }
        ImGui.endChild();
        return confirmed;
    }

    private boolean renderFileName() {
        ImGui.alignTextToFramePadding();
        ImGui.text(translate("file"));
        ImGui.sameLine();
        ImGui.setNextItemWidth(-1);
        if (ImGui.isWindowAppearing()) {
            ImGui.setKeyboardFocusHere();
        }
        ImGui.inputText("##file", fileName);
        return ImGui.isItemDeactivated()
                && (ImGui.isKeyPressed(ImGuiKey.Enter) || ImGui.isKeyPressed(ImGuiKey.KeypadEnter));
    }

    private boolean renderButtons() {
        String save = translate("save");
        String cancel = translate("cancel");
        ImGuiStyle style = ImGui.getStyle();
        float buttonsWidth = buttonWidth(save) + style.getItemSpacingX() + buttonWidth(cancel);
        ImGui.setCursorPos(
                ImGui.getWindowWidth() - style.getWindowPaddingX() - buttonsWidth,
                ImGui.getWindowHeight() - style.getWindowPaddingY() - ImGui.getFrameHeight());
        ImGui.beginDisabled(!isNameValid());
        boolean clicked = ImGui.button(save + "###save");
        ImGui.endDisabled();
        ImGui.sameLine();
        if (ImGui.button(cancel + "###cancel")) {
            close();
        }
        return clicked;
    }

    private void save() {
        NarrativeStudioClient.getInstance().getTabsManager().saveAs(currentPath, sceneName());
        close();
    }

    private Tree currentDirectory() {
        Tree root = NarrativeStudioClient.getInstance().getAssets().getRoot();
        if (root == null || currentPath.isEmpty()) return root;
        Tree directory = root;
        for (String name : currentPath.split("/")) {
            directory = directory.getChild().stream()
                    .filter(tree -> tree.isDirectory() && tree.getName().equals(name))
                    .findFirst()
                    .orElse(null);
            if (directory == null) {
                currentPath = "";
                return root;
            }
        }
        return directory;
    }

    private static boolean isShown(Tree tree) {
        return tree.isDirectory() || tree.getName().endsWith(SceneFiles.FORMAT);
    }

    private String sceneName() {
        String name = fileName.get().trim();
        return name.endsWith(SceneFiles.FORMAT) ? name.substring(0, name.length() - SceneFiles.FORMAT.length()) : name;
    }

    private boolean isNameValid() {
        String name = sceneName();
        return !name.isBlank() && name.indexOf('/') == -1 && name.indexOf('\\') == -1;
    }

    private static String translate(String key) {
        return ModTranslation.message("studio.save_scene." + key).getString();
    }

    private static float buttonWidth(String label) {
        return ImGui.calcTextSizeX(label) + ImGui.getStyle().getFramePaddingX() * 2;
    }

    private void close() {
        ImGui.closeCurrentPopup();
        onClose();
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
