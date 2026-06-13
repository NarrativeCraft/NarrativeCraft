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

package fr.loudo.narrativecraft.client.screens.components;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;

public class BreadcrumbWidget extends AbstractWidget {

    private static final int LINE_HEIGHT = 10;
    private static final int INDENT = 10;

    private final String[] sections;
    private final Font font;

    public BreadcrumbWidget(int x, int y, String breadcrumb, Font font) {
        super(x, y, computeWidth(breadcrumb, font), computeHeight(breadcrumb), Component.literal(breadcrumb));
        this.sections = breadcrumb.isEmpty() ? new String[0] : breadcrumb.split(";");
        this.font = font;
    }

    private static int computeWidth(String breadcrumb, Font font) {
        if (breadcrumb.isEmpty()) return 0;
        String[] parts = breadcrumb.split(";");
        int maxWidth = 0;
        for (int i = 0; i < parts.length; i++) {
            int w = font.width(parts[i]) + i * INDENT;
            if (w > maxWidth) maxWidth = w;
        }
        return maxWidth;
    }

    private static int computeHeight(String breadcrumb) {
        if (breadcrumb.isEmpty()) return 0;
        return breadcrumb.split(";").length * LINE_HEIGHT;
    }

    @Override
    protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        for (int i = 0; i < sections.length; i++) {
            graphics.drawString(font, sections[i], getX() + i * INDENT, getY() + i * LINE_HEIGHT, 0xFFFFFFFF);
        }
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
        for (String section : sections) {
            narrationElementOutput.add(
                    net.minecraft.client.gui.narration.NarratedElementType.TITLE, Component.literal(section));
        }
    }

    @Override
    public boolean isActive() {
        return false;
    }
}
