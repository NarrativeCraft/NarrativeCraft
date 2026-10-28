/*
 * NarrativeCraft - Create narrative games inside Minecraft. No coding, no game engine, only text and logic.
 * Copyright (c) 2026 LOUDO and contributors
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

import com.mojang.blaze3d.platform.Window;
import fr.loudo.narrativecraft.client.studio.layout.LayoutWindowsClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Window.class)
public class WindowMixin {

    @Shadow
    private int guiScale;

    @Shadow
    private int framebufferWidth;

    @Shadow
    private int framebufferHeight;

    @Inject(method = "getGuiScaledWidth", at = @At("HEAD"), cancellable = true)
    private void narrativecraft$studioGuiScaledWidth(CallbackInfoReturnable<Integer> cir) {
        LayoutWindowsClient layout = LayoutWindowsClient.getInstance();
        if (!layout.hasGameArea()) return;
        cir.setReturnValue((int) Math.ceil((double) layout.getGameWidth() / guiScale));
    }

    @Inject(method = "getGuiScaledHeight", at = @At("HEAD"), cancellable = true)
    private void narrativecraft$studioGuiScaledHeight(CallbackInfoReturnable<Integer> cir) {
        LayoutWindowsClient layout = LayoutWindowsClient.getInstance();
        if (!layout.hasGameArea()) return;
        cir.setReturnValue((int) Math.ceil((double) layout.getGameHeight() / guiScale));
    }

    @Redirect(
            method = "calculateScale",
            at = @At(value = "FIELD", target = "Lcom/mojang/blaze3d/platform/Window;framebufferWidth:I"))
    private int narrativecraft$studioScaleWidth(Window window) {
        LayoutWindowsClient layout = LayoutWindowsClient.getInstance();
        return layout.hasGameArea() ? layout.getGameWidth() : framebufferWidth;
    }

    @Redirect(
            method = "calculateScale",
            at = @At(value = "FIELD", target = "Lcom/mojang/blaze3d/platform/Window;framebufferHeight:I"))
    private int narrativecraft$studioScaleHeight(Window window) {
        LayoutWindowsClient layout = LayoutWindowsClient.getInstance();
        return layout.hasGameArea() ? layout.getGameHeight() : framebufferHeight;
    }
}
