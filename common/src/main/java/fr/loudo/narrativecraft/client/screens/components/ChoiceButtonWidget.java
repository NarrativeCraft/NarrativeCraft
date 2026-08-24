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

package fr.loudo.narrativecraft.client.screens.components;

import fr.loudo.narrativecraft.dialog.DialogData;
import fr.loudo.narrativecraft.dialog.DialogScrollText;
import fr.loudo.narrativecraft.utils.CustomFont;
import fr.loudo.narrativecraft.utils.Utils;
import java.util.function.Consumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.network.chat.Component;

public class ChoiceButtonWidget extends AbstractButton {

    private static final int PADDING_X = 9;
    private static final int PADDING_Y = 6;
    private static final int BASE_BACKGROUND_COLOR = 0x000000;
    private static final int BASE_TEXT_COLOR = 0xFFFFFF;
    private static final int BASE_HOVER_COLOR = 0xFFFFFF;

    private final int index;
    private final Consumer<Integer> onPress;
    private final DialogScrollText scrollText;
    private final DialogData dialogData;
    private final boolean locked;

    private int opacity = 255;
    private float renderOffsetX;
    private float renderOffsetY;
    private float baseOffsetX;
    private float baseOffsetY;
    private boolean canPress = true;

    public ChoiceButtonWidget(String text, int index, boolean locked, Consumer<Integer> onPress) {
        super(0, 0, 0, 0, Component.literal(text));
        this.index = index;
        this.onPress = onPress;
        this.locked = locked;

        dialogData = new DialogData();
        dialogData.setWidth(9999f);
        dialogData.setTextColor(0xFFFFFFFF);

        scrollText = new DialogScrollText(null, true);
        scrollText.setText(text);
        if (locked) {
            scrollText.setText(CustomFont.LOCK + " " + text);
        }
        scrollText.forceFinish();

        var font = Minecraft.getInstance().font;
        float[] dims = scrollText.computeTextDimensions(9999f, font, dialogData);
        setWidth((int) dims[0] + PADDING_X * 2);
        this.height = (int) dims[1] + PADDING_Y * 2;
    }

    public void tick() {
        scrollText.tick(0f);
    }

    @Override
    protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        int left = (int) (getX() + renderOffsetX);
        int top = (int) (getY() + renderOffsetY);
        int right = left + getWidth();
        int bottom = top + getHeight();

        if (isHovered && canPress) {
            int hoverColor = Utils.argb(opacity, BASE_HOVER_COLOR);
            graphics.fill(left - 1, top - 1, right + 1, top, hoverColor);
            graphics.fill(left - 1, bottom, right + 1, bottom + 1, hoverColor);
            graphics.fill(left - 1, top, left, bottom, hoverColor);
            graphics.fill(right, top, right + 1, bottom, hoverColor);
        }

        graphics.fill(left, top, right, bottom, Utils.argb(opacity, BASE_BACKGROUND_COLOR));

        dialogData.setTextColor(Utils.argb(opacity, BASE_TEXT_COLOR));
        scrollText.render2D(graphics, left + PADDING_X, top + PADDING_Y, dialogData, partialTick);
    }

    @Override
    public void onPress() {
        if (!canPress || locked) return;
        onPress.accept(index);
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {}

    public void setOpacity(int opacity) {
        this.opacity = opacity;
    }

    public void setCanPress(boolean canPress) {
        this.canPress = canPress;
    }

    public void setRenderOffset(float x, float y) {
        this.renderOffsetX = x;
        this.renderOffsetY = y;
    }

    public void setBaseOffset(float x, float y) {
        this.baseOffsetX = x;
        this.baseOffsetY = y;
    }

    public float getBaseOffsetX() {
        return baseOffsetX;
    }

    public float getBaseOffsetY() {
        return baseOffsetY;
    }

    @Override
    public void playDownSound(SoundManager soundManager) {}
}
