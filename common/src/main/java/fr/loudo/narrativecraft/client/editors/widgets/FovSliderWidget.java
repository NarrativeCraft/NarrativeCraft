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

package fr.loudo.narrativecraft.client.editors.widgets;

import fr.loudo.narrativecraft.utils.Translation;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.util.ARGB;

public class FovSliderWidget {

    private static final int TRACK_WIDTH = 10;
    private static final int TRACK_HEIGHT = 150;
    private static final int THUMB_HEIGHT = 8;
    private static final int PANEL_PADDING_X = 14;
    private static final int PANEL_PADDING_TOP = 18;
    private static final int PANEL_PADDING_BOTTOM = 32;
    private static final int PADDING_LEFT = 14;
    private static final int RESET_BTN_SIZE = 9;
    private static final float MIN = 10f;
    private static final float MAX = 120f;
    private static final float DEFAULT = 70f;

    private boolean visible = false;
    private boolean dragging = false;
    private float value = DEFAULT;

    public void toggle() {
        visible = !visible;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    public boolean isVisible() {
        return visible;
    }

    public float getValue() {
        return value;
    }

    public void setValue(float value) {
        this.value = (float) Math.clamp(value, MIN, MAX);
    }

    public void render(GuiGraphicsExtractor graphics, int screenWidth, int screenHeight, int mouseX, int mouseY) {
        if (!visible) return;

        int trackX = getTrackX();
        int trackY = getTrackY(screenHeight);

        int panelX1 = trackX - PANEL_PADDING_X;
        int panelY1 = trackY - PANEL_PADDING_TOP;
        int panelX2 = trackX + TRACK_WIDTH + PANEL_PADDING_X;
        int panelY2 = trackY + TRACK_HEIGHT + PANEL_PADDING_BOTTOM;
        graphics.fill(panelX1, panelY1, panelX2, panelY2, ARGB.color(200, 0, 0, 0));
        graphics.fill(panelX1, panelY1, panelX2, panelY1 + 1, 0xFFAAAAAA);
        graphics.fill(panelX1, panelY2 - 1, panelX2, panelY2, 0xFFAAAAAA);
        graphics.fill(panelX1, panelY1, panelX1 + 1, panelY2, 0xFFAAAAAA);
        graphics.fill(panelX2 - 1, panelY1, panelX2, panelY2, 0xFFAAAAAA);

        Minecraft minecraft = Minecraft.getInstance();

        String title = Translation.message("fov").getString();
        int titleX = trackX + TRACK_WIDTH / 2 - minecraft.font.width(title) / 2;
        graphics.text(minecraft.font, title, titleX, trackY - PANEL_PADDING_TOP + 4, 0xFFFFFFFF);

        graphics.fill(trackX, trackY, trackX + TRACK_WIDTH, trackY + TRACK_HEIGHT, 0xFF333333);

        int thumbY = getThumbY(trackY);
        boolean hovered = isOverTrackArea(mouseX, mouseY, trackX, trackY);
        graphics.fill(
                trackX,
                thumbY,
                trackX + TRACK_WIDTH,
                thumbY + THUMB_HEIGHT,
                hovered || dragging ? 0xFFFFFFFF : 0xFFCCCCCC);

        String label = String.format("%.0f", value);
        int labelX = trackX + TRACK_WIDTH / 2 - minecraft.font.width(label) / 2;
        graphics.text(minecraft.font, label, labelX, trackY + TRACK_HEIGHT + 4, 0xFFFFFFFF);

        int resetX = trackX + TRACK_WIDTH / 2 - RESET_BTN_SIZE / 2;
        int resetY = trackY + TRACK_HEIGHT + 4 + minecraft.font.lineHeight + 3;
        boolean resetHovered = isOverResetButton(mouseX, mouseY, resetX, resetY);
        graphics.fill(
                resetX,
                resetY,
                resetX + RESET_BTN_SIZE,
                resetY + RESET_BTN_SIZE,
                resetHovered ? 0xFFAAAAAA : 0xFF666666);
        graphics.text(minecraft.font, "R", resetX + 2, resetY + 1, 0xFFFFFFFF);
    }

    public boolean mouseClicked(MouseButtonEvent event) {
        if (!visible) return false;
        Minecraft minecraft = Minecraft.getInstance();
        int trackX = getTrackX();
        int trackY = getTrackY(minecraft.getWindow().getGuiScaledHeight());
        int resetX = trackX + TRACK_WIDTH / 2 - RESET_BTN_SIZE / 2;
        int resetY = trackY + TRACK_HEIGHT + 4 + minecraft.font.lineHeight + 3;
        if (isOverResetButton((int) event.x(), (int) event.y(), resetX, resetY)) {
            value = DEFAULT;
            return true;
        }
        if (isOverTrackArea((int) event.x(), (int) event.y(), trackX, trackY)) {
            dragging = true;
            updateValue((int) event.y(), trackY);
            return true;
        }
        return false;
    }

    public void mouseReleased() {
        dragging = false;
    }

    public boolean mouseDragged(double mouseY) {
        if (!dragging) return false;
        Minecraft minecraft = Minecraft.getInstance();
        int trackY = getTrackY(minecraft.getWindow().getGuiScaledHeight());
        updateValue((int) mouseY, trackY);
        return true;
    }

    private void updateValue(int mouseY, int trackY) {
        float ratio = (float) (mouseY - trackY) / TRACK_HEIGHT;
        value = Math.clamp(MIN + ratio * (MAX - MIN), MIN, MAX);
    }

    private int getThumbY(int trackY) {
        return trackY + (int) ((value - MIN) / (MAX - MIN) * TRACK_HEIGHT) - THUMB_HEIGHT / 2;
    }

    private boolean isOverResetButton(int mouseX, int mouseY, int resetX, int resetY) {
        return mouseX >= resetX
                && mouseX < resetX + RESET_BTN_SIZE
                && mouseY >= resetY
                && mouseY < resetY + RESET_BTN_SIZE;
    }

    private boolean isOverTrackArea(int mouseX, int mouseY, int trackX, int trackY) {
        return mouseX >= trackX - PANEL_PADDING_X
                && mouseX < trackX + TRACK_WIDTH + PANEL_PADDING_X
                && mouseY >= trackY
                && mouseY < trackY + TRACK_HEIGHT;
    }

    private int getTrackX() {
        return PADDING_LEFT;
    }

    private int getTrackY(int screenHeight) {
        return screenHeight / 2 - TRACK_HEIGHT / 2;
    }
}
