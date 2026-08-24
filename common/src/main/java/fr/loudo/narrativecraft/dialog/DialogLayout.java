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

package fr.loudo.narrativecraft.dialog;

import net.minecraft.client.gui.Font;

public class DialogLayout {

    private float width;
    private float height;
    private float textWidth;
    private float textHeight;
    private float leftGutterWidth;
    private float rightGutterWidth;
    private float totalWidth;
    private float totalHeight;

    public void compute(DialogData data, DialogScrollText scrollText, Font font) {
        compute(data, scrollText, font, 0f);
    }

    public void compute(DialogData data, DialogScrollText scrollText, Font font, float sideImageHeight) {
        DialogScrollText.LayoutResult result =
                scrollText.computeLayout(0f, 0f, data.getWidth(), sideImageHeight, font, data);

        width = result.contentWidth();
        height = result.contentHeight();
        textWidth = result.textWidth();
        textHeight = result.textHeight();
        leftGutterWidth = result.leftGutterWidth();
        rightGutterWidth = result.rightGutterWidth();
        totalWidth = width + data.getPaddingX() * 2f;
        totalHeight = height + data.getPaddingY() * 2f;
    }

    public float getWidth() {
        return width;
    }

    public float getHeight() {
        return height;
    }

    public float getTextWidth() {
        return textWidth;
    }

    public float getTextHeight() {
        return textHeight;
    }

    public float getLeftGutterWidth() {
        return leftGutterWidth;
    }

    public float getRightGutterWidth() {
        return rightGutterWidth;
    }

    public float getTotalWidth() {
        return totalWidth;
    }

    public float getTotalHeight() {
        return totalHeight;
    }
}
