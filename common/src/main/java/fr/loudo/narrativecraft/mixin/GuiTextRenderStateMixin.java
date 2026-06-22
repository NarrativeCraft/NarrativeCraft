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

package fr.loudo.narrativecraft.mixin;

import fr.loudo.narrativecraft.client.gui.IGuiTextRenderStateExtension;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.state.gui.GuiTextRenderState;
import net.minecraft.util.FormattedCharSequence;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * Allows to put float coordinates instead of only integers.
 * This way, text effects are more precise and smooth.
 * To use it, you have to use method {@code text} from {@link fr.loudo.narrativecraft.client.gui.GuiGraphicsExtractorExtension}
 */
@Mixin(GuiTextRenderState.class)
public class GuiTextRenderStateMixin implements IGuiTextRenderStateExtension {
    private float xFloat;
    private float yFloat;

    @Redirect(
            method = "ensurePrepared",
            at =
                    @At(
                            value = "INVOKE",
                            target =
                                    "Lnet/minecraft/client/gui/Font;prepareText(Lnet/minecraft/util/FormattedCharSequence;FFIZZI)Lnet/minecraft/client/gui/Font$PreparedText;"))
    private Font.PreparedText narrativecraft$textFloatEnsuredPrepared(
            Font instance,
            FormattedCharSequence text,
            float x,
            float y,
            int color,
            boolean dropShadow,
            boolean backgroundColor,
            int j) {
        float finalX = (xFloat != 0) ? xFloat : x;
        float finalY = (yFloat != 0) ? yFloat : y;
        return instance.prepareText(text, finalX, finalY, color, dropShadow, backgroundColor, j);
    }

    @Override
    public void setFloatX(float x) {
        this.xFloat = x;
    }

    @Override
    public void setFloatY(float y) {
        this.yFloat = y;
    }
}
