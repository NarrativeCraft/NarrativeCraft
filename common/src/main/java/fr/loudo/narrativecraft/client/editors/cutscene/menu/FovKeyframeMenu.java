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

package fr.loudo.narrativecraft.client.editors.cutscene.menu;

import fr.loudo.narrativecraft.api.editors.cutscene.keyframes.KeyframeMenu;
import fr.loudo.narrativecraft.editors.cutscene.keyframes.FovKeyframe;
import java.util.Locale;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

public class FovKeyframeMenu extends KeyframeMenu<FovKeyframe> {

    private static final int FIELD_LABEL_HEIGHT = 7;
    private static final int FIELD_HEIGHT = 12;
    private static final int FIELD_GAP = 4;

    private EditBox fieldFov;
    private EasingDropdown easingDropdown;

    public FovKeyframeMenu(FovKeyframe keyframe) {
        super(keyframe);
    }

    @Override
    protected void initContent() {
        int fieldWidth = width - padding * 2;
        fieldFov = new EditBox(Minecraft.getInstance().font, 0, 0, fieldWidth, FIELD_HEIGHT, Component.empty());
        fieldFov.setValue(String.format(Locale.US, "%.2f", keyframe.getFov()));
        easingDropdown = new EasingDropdown(keyframe.getEasing());
    }

    @Override
    protected int getContentHeight() {
        return FIELD_LABEL_HEIGHT + FIELD_HEIGHT + FIELD_GAP + FIELD_LABEL_HEIGHT + easingDropdown.getHeight();
    }

    @Override
    protected void renderContent(
            GuiGraphicsExtractor graphics, DeltaTracker delta, int x, int y, int contentWidth, int mouseX, int mouseY) {
        graphics.text(Minecraft.getInstance().font, "FOV", x, y - 2, 0xFFAAAAAA);
        fieldFov.setPosition(x, y + FIELD_LABEL_HEIGHT);
        fieldFov.setWidth(contentWidth);
        fieldFov.extractRenderState(graphics, mouseX, mouseY, delta.getGameTimeDeltaTicks());

        int easingY = y + FIELD_LABEL_HEIGHT + FIELD_HEIGHT + FIELD_GAP;
        graphics.text(Minecraft.getInstance().font, "Easing", x, easingY - 2, 0xFFAAAAAA);
        easingDropdown.setPosition(x, easingY + FIELD_LABEL_HEIGHT);
        easingDropdown.setWidth(contentWidth);
        easingDropdown.render(graphics, mouseX, mouseY);
    }

    @Override
    protected boolean onContentMouseClicked(
            MouseButtonEvent event, boolean isDoubleClick, int contentX, int contentY, int contentWidth) {
        if (easingDropdown.mouseClicked(event)) {
            fieldFov.setFocused(false);
            return true;
        }
        easingDropdown.close();
        boolean hovered = isHovered(event, fieldFov);
        fieldFov.setFocused(hovered);
        if (hovered) fieldFov.mouseClicked(event, isDoubleClick);
        return hovered;
    }

    @Override
    public boolean mouseScrolled(double amount) {
        return easingDropdown.mouseScrolled(amount);
    }

    @Override
    public boolean mouseDragged(MouseButtonEvent event, double dragX, double dragY) {
        if (fieldFov.isFocused()) return fieldFov.mouseDragged(event, dragX, dragY);
        return false;
    }

    @Override
    public boolean charTyped(CharacterEvent event) {
        if (fieldFov.isFocused()) return fieldFov.charTyped(event);
        return false;
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        if (fieldFov.isFocused()) return fieldFov.keyPressed(event);
        return false;
    }

    @Override
    protected void applyChanges() {
        try {
            keyframe.setFov(Float.parseFloat(fieldFov.getValue()));
        } catch (NumberFormatException ignored) {
        }
        keyframe.setEasing(easingDropdown.getSelected());
    }
}
