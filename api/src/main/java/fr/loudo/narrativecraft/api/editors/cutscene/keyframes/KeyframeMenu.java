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
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.util.ARGB;

public abstract class KeyframeMenu<T extends Keyframe> {

    protected static final int WIDTH = 110;
    protected static final int PADDING = 5;
    protected static final int BUTTON_HEIGHT = 14;
    private static final int BUTTON_WIDTH = (WIDTH - PADDING * 3) / 2;
    private static final int BACKGROUND_COLOR = ARGB.color(180, 0, 0, 0);

    protected final T keyframe;
    private final Button editButton;
    private final Button deleteButton;
    private boolean visible = true;

    protected KeyframeMenu(T keyframe) {
        this.keyframe = keyframe;
        this.editButton = Button.builder(Component.literal("Edit"), b -> {
                    applyChanges();
                    close();
                })
                .size(BUTTON_WIDTH, BUTTON_HEIGHT)
                .build();
        this.deleteButton = Button.builder(Component.literal("Delete"), b -> {
                    keyframe.getLayer().removeKeyframe(keyframe);
                    close();
                })
                .size(BUTTON_WIDTH, BUTTON_HEIGHT)
                .build();
        initContent();
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
        int totalHeight = PADDING + getContentHeight() + PADDING + BUTTON_HEIGHT + PADDING;
        int x = screenWidth - WIDTH - PADDING;
        int y = PADDING;

        graphics.fill(x, y, x + WIDTH, y + totalHeight, BACKGROUND_COLOR);

        renderContent(graphics, delta, x + PADDING, y + PADDING, WIDTH - PADDING * 2, mouseX, mouseY);

        int btnY = y + PADDING + getContentHeight() + PADDING;
        editButton.setPosition(x + PADDING, btnY);
        deleteButton.setPosition(x + PADDING * 2 + BUTTON_WIDTH, btnY);
        editButton.extractRenderState(graphics, mouseX, mouseY, delta.getGameTimeDeltaTicks());
        deleteButton.extractRenderState(graphics, mouseX, mouseY, delta.getGameTimeDeltaTicks());
    }

    public boolean mouseClicked(MouseButtonEvent event, boolean isDoubleClick) {
        if (!visible) return false;
        Minecraft mc = Minecraft.getInstance();
        int screenWidth = mc.getWindow().getGuiScaledWidth();
        int totalHeight = PADDING + getContentHeight() + PADDING + BUTTON_HEIGHT + PADDING;
        int x = screenWidth - WIDTH - PADDING;
        int y = PADDING;

        if (!isOnMenu(event.x(), event.y(), x, y, totalHeight)) return false;

        editButton.mouseClicked(event, isDoubleClick);
        deleteButton.mouseClicked(event, isDoubleClick);
        onContentMouseClicked(event, isDoubleClick, x + PADDING, y + PADDING, WIDTH - PADDING * 2);
        return true;
    }

    protected void onContentMouseClicked(
            MouseButtonEvent event, boolean isDoubleClick, int contentX, int contentY, int contentWidth) {}

    public void mouseDragged(MouseButtonEvent event, double dragX, double dragY) {}

    public void mouseScrolled(double amount) {}

    public void charTyped(CharacterEvent event) {}

    public void keyPressed(KeyEvent event) {}

    private boolean isOnMenu(double mouseX, double mouseY, int x, int y, int totalHeight) {
        return mouseX >= x && mouseX <= x + WIDTH && mouseY >= y && mouseY <= y + totalHeight;
    }

    public boolean isVisible() {
        return visible;
    }

    public void close() {
        this.visible = false;
    }
}
