/*
 * NarrativeCraft - Create your own stories, easily, and freely in Minecraft.
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

import fr.loudo.narrativecraft.api.editors.cutscene.keyframes.Keyframe;
import fr.loudo.narrativecraft.api.editors.cutscene.layers.CutsceneLayer;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;

public class CutsceneMakerEditorLayer {

    private static final int BTN_SIZE = 9;
    private static final int MOVE_BTN_X = 2;

    public static final int NAME_X = MOVE_BTN_X + BTN_SIZE + 3;

    private final CutsceneLayer layer;
    private final int layerGap;

    public CutsceneMakerEditorLayer(CutsceneLayer layer, int layerGap) {
        this.layer = layer;
        this.layerGap = layerGap;
    }

    public void render(
            GuiGraphicsExtractor graphics,
            DeltaTracker delta,
            int layerY,
            int layerHeight,
            int timelineWidth,
            float visibleTicks,
            float viewStartTick,
            int mouseX,
            int mouseY,
            boolean isFirst,
            boolean isLast) {
        if (!isFirst) renderMoveUpButton(graphics, layerY, mouseX, mouseY);
        if (!isLast) renderMoveDownButton(graphics, layerY, mouseX, mouseY);
        renderAddButton(graphics, layerY, layerHeight, mouseX, mouseY);
        renderRemoveButton(graphics, layerY, layerHeight, mouseX, mouseY);
        renderKeyframes(graphics, delta, layerY, layerHeight, timelineWidth, visibleTicks, viewStartTick);
    }

    private void renderMoveUpButton(GuiGraphicsExtractor graphics, int layerY, int mouseX, int mouseY) {
        int btnY = layerY + 1;
        boolean hovered = isMoveUpButtonHovered(mouseX, mouseY, layerY);
        renderButton(graphics, MOVE_BTN_X, btnY, hovered, "⮝");
    }

    private void renderMoveDownButton(GuiGraphicsExtractor graphics, int layerY, int mouseX, int mouseY) {
        int btnY = layerY + BTN_SIZE + 1;
        boolean hovered = isMoveDownButtonHovered(mouseX, mouseY, layerY);
        renderButton(graphics, MOVE_BTN_X, btnY, hovered, "⮟");
    }

    private void renderAddButton(GuiGraphicsExtractor graphics, int layerY, int layerHeight, int mouseX, int mouseY) {
        int btnX = getBtnXAdd();
        int btnY = layerY + (layerHeight - BTN_SIZE) / 2;
        boolean hovered = isAddButtonHovered(mouseX, mouseY, layerY, layerHeight);
        renderButton(graphics, btnX, btnY, hovered, "+");
    }

    private void renderRemoveButton(
            GuiGraphicsExtractor graphics, int layerY, int layerHeight, int mouseX, int mouseY) {
        int btnX = getBtnXRemove();
        int btnY = layerY + (layerHeight - BTN_SIZE) / 2;
        boolean hovered = isRemoveButtonHovered(mouseX, mouseY, layerY, layerHeight);
        renderButton(graphics, btnX, btnY, hovered, "×");
    }

    private void renderButton(GuiGraphicsExtractor graphics, int btnX, int btnY, boolean hovered, String text) {
        graphics.fill(btnX, btnY, btnX + BTN_SIZE, btnY + BTN_SIZE, hovered ? 0xFFAAAAAA : 0xFF666666);
        graphics.text(Minecraft.getInstance().font, text, btnX + 2, btnY + 1, 0xFFFFFFFF);
    }

    private void renderKeyframes(
            GuiGraphicsExtractor graphics,
            DeltaTracker delta,
            int layerY,
            int layerHeight,
            int timelineWidth,
            float visibleTicks,
            float viewStartTick) {
        for (Keyframe keyframe : layer.getKeyframes()) {
            int kfX = layerGap
                    + (int) ((keyframe.getTick() - viewStartTick) / visibleTicks * timelineWidth)
                    - Keyframe.SIZE / 2;
            if (kfX + Keyframe.SIZE < layerGap || kfX > layerGap + timelineWidth) {
                keyframe.setLayerPosition(Integer.MIN_VALUE, Integer.MIN_VALUE);
                continue;
            }
            int kfY = layerY + (layerHeight - Keyframe.SIZE) / 2;
            keyframe.setLayerPosition(kfX, kfY);
            keyframe.render(graphics, delta);
        }
    }

    public boolean isAddButtonHovered(int mouseX, int mouseY, int layerY, int layerHeight) {
        int btnX = getBtnXAdd();
        int btnY = getBtnY(layerY, layerHeight);
        return isHovered(mouseX, mouseY, btnX, btnY);
    }

    public boolean isRemoveButtonHovered(int mouseX, int mouseY, int layerY, int layerHeight) {
        int btnX = getBtnXRemove();
        int btnY = getBtnY(layerY, layerHeight);
        return isHovered(mouseX, mouseY, btnX, btnY);
    }

    public boolean isMoveUpButtonHovered(int mouseX, int mouseY, int layerY) {
        return isHovered(mouseX, mouseY, MOVE_BTN_X, layerY + 1);
    }

    public boolean isMoveDownButtonHovered(int mouseX, int mouseY, int layerY) {
        return isHovered(mouseX, mouseY, MOVE_BTN_X, layerY + BTN_SIZE + 1);
    }

    public Keyframe getHoveredKeyframe(int mouseX, int mouseY) {
        for (Keyframe keyframe : layer.getKeyframes()) {
            if (keyframe.isHovered(mouseX, mouseY)) return keyframe;
        }
        return null;
    }

    public void addKeyframe(int tick) {
        Keyframe keyframe = layer.createDefaultKeyframe(tick);
        layer.addKeyframe(keyframe);
    }

    private int getBtnXAdd() {
        return layerGap - BTN_SIZE - 5;
    }

    private int getBtnXRemove() {
        return getBtnXAdd() - BTN_SIZE - 5;
    }

    private int getBtnY(int layerY, int layerHeight) {
        return layerY + (layerHeight - BTN_SIZE) / 2;
    }

    private boolean isHovered(int mouseX, int mouseY, int btnX, int btnY) {
        return mouseX >= btnX && mouseX < btnX + BTN_SIZE && mouseY >= btnY && mouseY < btnY + BTN_SIZE;
    }

    public CutsceneLayer getLayer() {
        return layer;
    }
}
