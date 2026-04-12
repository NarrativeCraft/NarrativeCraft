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

import fr.loudo.narrativecraft.utils.Translation;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.util.ARGB;

public class CutsceneMakerRollWidget {

    private static final int TRACK_WIDTH = 10;
    private static final int TRACK_HEIGHT = 150;
    private static final int THUMB_HEIGHT = 8;
    private static final int PANEL_PADDING_X = 14;
    private static final int PANEL_PADDING_TOP = 18;
    private static final int PANEL_PADDING_BOTTOM = 32;
    private static final int PADDING_RIGHT = 14;
    private static final int RESET_BTN_SIZE = 9;
    private static final float MIN = -180f;
    private static final float MAX = 180f;

    private boolean visible = false;
    private boolean dragging = false;
    private float value = 0f;

    public void toggle() {
        visible = !visible;
    }

    public boolean isVisible() {
        return visible;
    }

    public float getValue() {
        return value;
    }

    public void setValue(float v) {
        value = (float) Math.clamp(v, MIN, MAX);
    }

    public void render(GuiGraphicsExtractor graphics, int screenWidth, int screenHeight, int mouseX, int mouseY) {
        if (!visible) return;

        int tx = getTrackX(screenWidth);
        int ty = getTrackY(screenHeight);

        // Background panel
        int panelX1 = tx - PANEL_PADDING_X;
        int panelY1 = ty - PANEL_PADDING_TOP;
        int panelX2 = tx + TRACK_WIDTH + PANEL_PADDING_X;
        int panelY2 = ty + TRACK_HEIGHT + PANEL_PADDING_BOTTOM;
        graphics.fill(panelX1, panelY1, panelX2, panelY2, ARGB.color(200, 0, 0, 0));
        graphics.fill(panelX1, panelY1, panelX2, panelY1 + 1, 0xFFAAAAAA);
        graphics.fill(panelX1, panelY2 - 1, panelX2, panelY2, 0xFFAAAAAA);
        graphics.fill(panelX1, panelY1, panelX1 + 1, panelY2, 0xFFAAAAAA);
        graphics.fill(panelX2 - 1, panelY1, panelX2, panelY2, 0xFFAAAAAA);

        Minecraft mc = Minecraft.getInstance();

        // Title
        String title = Translation.message("roll").getString();
        int titleX = tx + TRACK_WIDTH / 2 - mc.font.width(title) / 2;
        graphics.text(mc.font, title, titleX, ty - PANEL_PADDING_TOP + 4, 0xFFFFFFFF);

        // Track background
        graphics.fill(tx, ty, tx + TRACK_WIDTH, ty + TRACK_HEIGHT, 0xFF333333);

        // Center notch (zero line)
        int centerY = ty + TRACK_HEIGHT / 2;
        graphics.fill(tx - 3, centerY, tx + TRACK_WIDTH + 3, centerY + 1, 0xFF888888);

        // Thumb
        int thumbY = getThumbY(ty);
        boolean hovered = isOverTrackArea(mouseX, mouseY, tx, ty);
        graphics.fill(
                tx, thumbY, tx + TRACK_WIDTH, thumbY + THUMB_HEIGHT, hovered || dragging ? 0xFFFFFFFF : 0xFFCCCCCC);

        // Value label
        String label = String.format("%.0f\u00b0", value);
        int labelX = tx + TRACK_WIDTH / 2 - mc.font.width(label) / 2;
        graphics.text(mc.font, label, labelX, ty + TRACK_HEIGHT + 4, 0xFFFFFFFF);

        // Reset button
        int resetX = tx + TRACK_WIDTH / 2 - RESET_BTN_SIZE / 2;
        int resetY = ty + TRACK_HEIGHT + 4 + mc.font.lineHeight + 3;
        boolean resetHovered = isOverResetButton(mouseX, mouseY, resetX, resetY);
        graphics.fill(
                resetX,
                resetY,
                resetX + RESET_BTN_SIZE,
                resetY + RESET_BTN_SIZE,
                resetHovered ? 0xFFAAAAAA : 0xFF666666);
        graphics.text(mc.font, "0", resetX + 2, resetY + 1, 0xFFFFFFFF);
    }

    public boolean mouseClicked(MouseButtonEvent event) {
        if (!visible) return false;
        Minecraft mc = Minecraft.getInstance();
        int tx = getTrackX(mc.getWindow().getGuiScaledWidth());
        int ty = getTrackY(mc.getWindow().getGuiScaledHeight());
        int resetX = tx + TRACK_WIDTH / 2 - RESET_BTN_SIZE / 2;
        int resetY = ty + TRACK_HEIGHT + 4 + mc.font.lineHeight + 3;
        if (isOverResetButton((int) event.x(), (int) event.y(), resetX, resetY)) {
            value = 0f;
            return true;
        }
        if (isOverTrackArea((int) event.x(), (int) event.y(), tx, ty)) {
            dragging = true;
            updateValue((int) event.y(), ty);
            return true;
        }
        return false;
    }

    public void mouseReleased() {
        dragging = false;
    }

    public boolean mouseDragged(double mouseY) {
        if (!dragging) return false;
        Minecraft mc = Minecraft.getInstance();
        int ty = getTrackY(mc.getWindow().getGuiScaledHeight());
        updateValue((int) mouseY, ty);
        return true;
    }

    private void updateValue(int mouseY, int trackY) {
        float t = (float) (mouseY - trackY) / TRACK_HEIGHT;
        value = Math.clamp(MIN + t * (MAX - MIN), MIN, MAX);
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

    private boolean isOverTrackArea(int mouseX, int mouseY, int tx, int ty) {
        return mouseX >= tx - PANEL_PADDING_X
                && mouseX < tx + TRACK_WIDTH + PANEL_PADDING_X
                && mouseY >= ty
                && mouseY < ty + TRACK_HEIGHT;
    }

    private int getTrackX(int screenWidth) {
        return screenWidth - PADDING_RIGHT - TRACK_WIDTH;
    }

    private int getTrackY(int screenHeight) {
        return screenHeight / 2 - TRACK_HEIGHT / 2;
    }
}
