/*
 * NarrativeCraft - Create narrative games inside Minecraft. No coding, no game engine, only text and logic.
 * Copyright (c) 2025 LOUDO and contributors
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

package fr.loudo.narrativecraft.client.editors.cutscene;

import fr.loudo.narrativecraft.NarrativeCraftMod;
import fr.loudo.narrativecraft.api.editors.cutscene.layers.ICutsceneLayerType;
import fr.loudo.narrativecraft.client.screens.components.LayerSelectionButton;
import fr.loudo.narrativecraft.utils.MathUtils;
import fr.loudo.narrativecraft.utils.UtilsClient;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

/**
 * The selection menu to add layers on the timeline
 */
public class CutsceneMakerEditorLayerSelector {

    private static final List<ICutsceneLayerType> LAYER_TYPES =
            NarrativeCraftMod.getInstance().getCutsceneLayerRegistry().getTypes();

    private final ClientCutsceneMakerEditorMaker cutsceneEditor;
    private final List<LayerSelectionButton> layerButtons = new ArrayList<>();
    private final int width;
    private final int maxHeight;
    private final int color;

    private int x;
    private int y;
    private int scrollOffset = 0;
    private boolean visible;

    public CutsceneMakerEditorLayerSelector(
            ClientCutsceneMakerEditorMaker cutsceneEditor, int width, int maxHeight, int color) {
        this.cutsceneEditor = cutsceneEditor;
        this.width = width;
        this.maxHeight = maxHeight;
        this.color = color;

        for (ICutsceneLayerType type : LAYER_TYPES) {
            layerButtons.add(new LayerSelectionButton(0, 0, width, 20, Component.literal(type.getName()), b -> {
                cutsceneEditor.addLayer(type.createLayer());
                hide();
            }));
        }
    }

    public void render(GuiGraphics graphics, float deltaTracker) {
        if (!visible) return;

        int visibleHeight = Math.min(layerButtons.size() * 20, maxHeight);
        graphics.fill(x, y - visibleHeight, x + width, y, color);

        int[] mousePos = UtilsClient.getScaledMousePos();
        graphics.enableScissor(x, y - visibleHeight, x + width, y);
        int currentY = y - visibleHeight - scrollOffset;
        for (LayerSelectionButton button : layerButtons) {
            button.setPosition(x, currentY);
            currentY += button.getHeight();
            button.render(
                    graphics, mousePos[0], mousePos[1], Minecraft.getInstance().getDeltaFrameTime());
        }
        graphics.disableScissor();

        int totalHeight = layerButtons.size() * 20;
        int maxScroll = totalHeight - maxHeight;
        if (maxScroll > 0) {
            int indicatorHeight = maxHeight * maxHeight / totalHeight;
            int indicatorTop = (int) ((float) scrollOffset / maxScroll * (maxHeight - indicatorHeight));
            graphics.fill(
                    x + width - 1,
                    y - maxHeight + indicatorTop,
                    x + width,
                    y - maxHeight + indicatorTop + indicatorHeight,
                    0xFFFFFFFF);
        }
    }

    public void mouseScrolled(double deltaY) {
        if (!visible) return;
        int[] mousePos = UtilsClient.getScaledMousePos();
        int visibleHeight = Math.min(layerButtons.size() * 20, maxHeight);
        if (mousePos[0] < x || mousePos[0] > x + width || mousePos[1] < y - visibleHeight || mousePos[1] > y) return;

        int maxScroll = Math.max(0, layerButtons.size() * 20 - maxHeight);
        scrollOffset = (int) MathUtils.clamp(scrollOffset - deltaY * 20, 0, maxScroll);
    }

    public void mouseClicked(double mouseX, double mouseY, int button, boolean isDoubleClick) {
        if (!visible) return;
        int visibleHeight = Math.min(layerButtons.size() * 20, maxHeight);
        int[] mousePos = UtilsClient.getScaledMousePos();

        // hide if click outside the menu
        if (mousePos[0] < x || mousePos[0] > x + width || mousePos[1] < y - visibleHeight || mousePos[1] > y) {
            hide();
            return;
        }

        // only called mouseClicked if we clicked the button in the visible menu
        int containerTop = y - visibleHeight;
        for (LayerSelectionButton button1 : layerButtons) {
            if (button1.getY() >= containerTop && button1.getY() + button1.getHeight() <= y) {
                button1.mouseClicked(mouseX, mouseY, button);
            }
        }
    }

    public void toggle() {
        visible = !visible;
    }

    public void show() {
        visible = true;
    }

    public void hide() {
        visible = false;
    }

    public void setPosition(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public ClientCutsceneMakerEditorMaker getCutsceneEditor() {
        return cutsceneEditor;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getWidth() {
        return width;
    }

    public boolean isVisible() {
        return visible;
    }
}
