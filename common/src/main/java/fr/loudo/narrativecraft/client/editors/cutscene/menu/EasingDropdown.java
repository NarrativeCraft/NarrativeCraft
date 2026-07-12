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

import fr.loudo.narrativecraft.api.editors.cutscene.keyframes.EasingType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.input.MouseButtonEvent;

public class EasingDropdown {

    private static final int HEADER_HEIGHT = 12;
    private static final int ITEM_HEIGHT = 10;
    private static final int MAX_VISIBLE = 4;

    private final EasingType[] values = EasingType.values();
    private EasingType selected;
    private boolean open = false;
    private int scrollOffset = 0;
    private int x, y, width;

    public EasingDropdown(EasingType initial) {
        this.selected = initial;
    }

    public void setPosition(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        if (!open) return HEADER_HEIGHT;
        return HEADER_HEIGHT + Math.min(values.length - scrollOffset, MAX_VISIBLE) * ITEM_HEIGHT;
    }

    public void render(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        boolean headerHovered = isOverHeader(mouseX, mouseY);
        graphics.fill(x, y, x + width, y + HEADER_HEIGHT, headerHovered ? 0xFF888888 : 0xFF555555);
        graphics.text(Minecraft.getInstance().font, selected.name(), x + 2, y + 2, 0xFFFFFFFF);
        String arrow = open ? "-" : "+";
        graphics.text(
                Minecraft.getInstance().font,
                arrow,
                x + width - Minecraft.getInstance().font.width(arrow) - 2,
                y + 2,
                0xFFAAAAAA);

        if (!open) return;

        int listY = y + HEADER_HEIGHT;
        int visible = Math.min(values.length - scrollOffset, MAX_VISIBLE);
        for (int i = 0; i < visible; i++) {
            EasingType type = values[i + scrollOffset];
            boolean hovered = mouseX >= x && mouseX < x + width && mouseY >= listY && mouseY < listY + ITEM_HEIGHT;
            boolean isSelected = type == selected;
            graphics.fill(
                    x,
                    listY,
                    x + width,
                    listY + ITEM_HEIGHT,
                    hovered ? 0xFF888888 : (isSelected ? 0xFF334433 : 0xFF333333));
            graphics.text(Minecraft.getInstance().font, type.name(), x + 2, listY + 1, 0xFFFFFFFF);
            listY += ITEM_HEIGHT;
        }
    }

    public boolean mouseClicked(MouseButtonEvent event) {
        if (isOverHeader(event.x(), event.y())) {
            open = !open;
            return true;
        }
        if (!open) return false;
        int listY = y + HEADER_HEIGHT;
        int visible = Math.min(values.length - scrollOffset, MAX_VISIBLE);
        for (int i = 0; i < visible; i++) {
            if (event.x() >= x && event.x() < x + width && event.y() >= listY && event.y() < listY + ITEM_HEIGHT) {
                selected = values[i + scrollOffset];
                open = false;
                return true;
            }
            listY += ITEM_HEIGHT;
        }
        return false;
    }

    public boolean mouseScrolled(double amount) {
        if (!open) return false;
        int maxScroll = Math.max(0, values.length - MAX_VISIBLE);
        scrollOffset = (int) Math.clamp(scrollOffset - amount, 0, maxScroll);
        return true;
    }

    public void close() {
        open = false;
    }

    private boolean isOverHeader(double mx, double my) {
        return mx >= x && mx < x + width && my >= y && my < y + HEADER_HEIGHT;
    }

    public EasingType getSelected() {
        return selected;
    }
}
