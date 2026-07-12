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

package fr.loudo.narrativecraft.api.editors.cutscene.keyframes;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.util.ARGB;

public abstract class KeyframeMenu<T extends Keyframe> {

    protected int width = 110;
    protected int padding = 5;
    protected int buttonHeight = 14;
    protected int buttonWidth;
    protected int backgroundColor;

    protected final T keyframe;
    private final Button editButton;
    private final Button deleteButton;
    private boolean visible = true;

    protected KeyframeMenu(T keyframe) {
        this.keyframe = keyframe;
        initContent();
        buttonWidth = (width - padding * 3) / 2;
        backgroundColor = ARGB.color(180, 0, 0, 0);
        this.editButton = Button.builder(Component.literal("Edit"), b -> {
                    applyChanges();
                    close();
                })
                .size(buttonWidth, buttonHeight)
                .build();
        this.deleteButton = Button.builder(Component.literal("Delete"), b -> {
                    keyframe.getLayer().removeKeyframe(keyframe);
                    close();
                })
                .size(buttonWidth, buttonHeight)
                .build();
    }

    protected abstract int getContentHeight();

    protected abstract void renderContent(
            GuiGraphicsExtractor graphics, DeltaTracker delta, int x, int y, int contentWidth, int mouseX, int mouseY);

    protected abstract void applyChanges();

    protected abstract void initContent();

    public void render(GuiGraphicsExtractor graphics, DeltaTracker delta, int mouseX, int mouseY) {
        if (!visible) return;
        Minecraft mc = Minecraft.getInstance();
        int screenWidth = mc.getWindow().getGuiScaledWidth();
        int totalHeight = padding + getContentHeight() + padding + buttonHeight + padding;
        int x = screenWidth - width - padding;
        int y = padding;

        graphics.fill(x, y, x + width, y + totalHeight, backgroundColor);

        renderContent(graphics, delta, x + padding, y + padding, width - padding * 2, mouseX, mouseY);

        int btnY = y + padding + getContentHeight() + padding;
        editButton.setPosition(x + padding, btnY);
        deleteButton.setPosition(x + padding * 2 + buttonWidth, btnY);
        editButton.extractRenderState(graphics, mouseX, mouseY, delta.getGameTimeDeltaTicks());
        deleteButton.extractRenderState(graphics, mouseX, mouseY, delta.getGameTimeDeltaTicks());
    }

    public boolean mouseClicked(MouseButtonEvent event, boolean isDoubleClick) {
        if (!visible) return false;
        Minecraft mc = Minecraft.getInstance();
        int screenWidth = mc.getWindow().getGuiScaledWidth();
        int totalHeight = padding + getContentHeight() + padding + buttonHeight + padding;
        int x = screenWidth - width - padding;
        int y = padding;

        if (!isOnMenu(event.x(), event.y(), x, y, totalHeight)) return false;

        editButton.mouseClicked(event, isDoubleClick);
        deleteButton.mouseClicked(event, isDoubleClick);
        onContentMouseClicked(event, isDoubleClick, x + padding, y + padding, width - padding * 2);
        return true;
    }

    protected boolean onContentMouseClicked(
            MouseButtonEvent event, boolean isDoubleClick, int contentX, int contentY, int contentWidth) {
        return false;
    }

    public boolean mouseDragged(MouseButtonEvent event, double dragX, double dragY) {
        return false;
    }

    public boolean mouseScrolled(double amount) {
        return false;
    }

    public boolean charTyped(CharacterEvent event) {
        return false;
    }

    public boolean keyPressed(KeyEvent event) {
        return false;
    }

    protected boolean isHovered(MouseButtonEvent event, AbstractWidget widget) {
        return event.x() >= widget.getX()
                && event.x() < widget.getX() + widget.getWidth()
                && event.y() >= widget.getY()
                && event.y() < widget.getY() + widget.getHeight();
    }

    private boolean isOnMenu(double mouseX, double mouseY, int x, int y, int totalHeight) {
        return mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + totalHeight;
    }

    public boolean isVisible() {
        return visible;
    }

    public void close() {
        this.visible = false;
    }
}
