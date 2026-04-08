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

import fr.loudo.narrativecraft.editors.cutscene.keyframes.Keyframe;
import fr.loudo.narrativecraft.editors.cutscene.layers.CutsceneLayer;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;

public class CutsceneMakerEditorLayer {

    private static final int BTN_SIZE = 9;

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
            int maxTick,
            int mouseX,
            int mouseY) {
        renderAddButton(graphics, layerY, layerHeight, mouseX, mouseY);
        renderKeyframes(graphics, delta, layerY, layerHeight, timelineWidth, maxTick);
    }

    private void renderAddButton(GuiGraphicsExtractor graphics, int layerY, int layerHeight, int mouseX, int mouseY) {
        int btnX = getBtnX();
        int btnY = layerY + (layerHeight - BTN_SIZE) / 2;
        boolean hovered = isAddButtonHovered(mouseX, mouseY, layerY, layerHeight);
        graphics.fill(btnX, btnY, btnX + BTN_SIZE, btnY + BTN_SIZE, hovered ? 0xFFAAAAAA : 0xFF666666);
        graphics.text(Minecraft.getInstance().font, "+", btnX + 2, btnY + 1, 0xFFFFFFFF);
    }

    private void renderKeyframes(
            GuiGraphicsExtractor graphics,
            DeltaTracker delta,
            int layerY,
            int layerHeight,
            int timelineWidth,
            int maxTick) {
        for (Keyframe keyframe : layer.getKeyframes()) {
            int kfX = layerGap + (int) ((float) keyframe.getTick() / maxTick * timelineWidth) - Keyframe.SIZE / 2;
            int kfY = layerY + (layerHeight - Keyframe.SIZE) / 2;
            keyframe.setLayerPosition(kfX, kfY);
            keyframe.render(graphics, delta);
        }
    }

    public boolean isAddButtonHovered(int mouseX, int mouseY, int layerY, int layerHeight) {
        int btnX = getBtnX();
        int btnY = layerY + (layerHeight - BTN_SIZE) / 2;
        return mouseX >= btnX && mouseX < btnX + BTN_SIZE && mouseY >= btnY && mouseY < btnY + BTN_SIZE;
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

    private int getBtnX() {
        return layerGap - BTN_SIZE - 2;
    }

    public CutsceneLayer getLayer() {
        return layer;
    }
}
