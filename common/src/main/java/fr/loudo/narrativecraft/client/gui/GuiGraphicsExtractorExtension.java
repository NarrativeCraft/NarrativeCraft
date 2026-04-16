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

package fr.loudo.narrativecraft.client.gui;

import fr.loudo.narrativecraft.mixin.accessor.GuiGraphicsExtractorAccessor;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.render.TextureSetup;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.state.gui.GuiRenderState;
import net.minecraft.client.renderer.state.gui.GuiTextRenderState;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.util.ARGB;
import org.joml.Matrix3x2f;
import org.jspecify.annotations.Nullable;

public record GuiGraphicsExtractorExtension(GuiGraphicsExtractor graphics) {

    public void text(Font font, @Nullable String str, float x, float y, int color, boolean dropShadow) {

        if (str == null || ARGB.alpha(color) == 0) return;

        GuiTextRenderState renderState = new GuiTextRenderState(
                font,
                Language.getInstance().getVisualOrder(FormattedText.of(str)),
                new Matrix3x2f(graphics.pose()),
                (int) x,
                (int) y,
                color,
                0,
                dropShadow,
                false,
                getScreenRectangle());
        ((IGuiTextRenderStateExtension) (Object) renderState).setFloatX(x);
        ((IGuiTextRenderStateExtension) (Object) renderState).setFloatY(y);
        this.getGuiRenderState().addText(renderState);
    }

    public void skipArrow(float x, float y, float halfWidth, float halfHeight, int color) {
        Matrix3x2f pose = new Matrix3x2f(graphics.pose()).translate(x, y);
        this.getGuiRenderState()
                .addGuiElement(new GuiSkipTriangleRenderState(
                        RenderPipelines.GUI,
                        TextureSetup.noTexture(),
                        pose,
                        halfWidth,
                        halfHeight,
                        color,
                        getScreenRectangle()));
    }

    private GuiRenderState getGuiRenderState() {
        return ((GuiGraphicsExtractorAccessor) graphics).getGuiRenderState();
    }

    private ScreenRectangle getScreenRectangle() {
        return ((IGuiGraphicsExtractorExtension) graphics).getPeekScissorStack();
    }
}
