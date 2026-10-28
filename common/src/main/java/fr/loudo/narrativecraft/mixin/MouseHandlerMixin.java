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
import fr.loudo.narrativecraft.client.events.OnGrabClient;
import fr.loudo.narrativecraft.client.events.OnMouseClickClient;
import fr.loudo.narrativecraft.client.studio.layout.LayoutWindowsClient;
import net.minecraft.client.MouseHandler;
import net.minecraft.client.input.MouseButtonInfo;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MouseHandler.class)
public class MouseHandlerMixin {

    @Inject(method = "onButton", at = @At("RETURN"))
    private void narrativecraft$onMouseClickPost(
            long handle, MouseButtonInfo rawButtonInfo, int action, CallbackInfo ci) {
        OnMouseClickClient.onClickPost(handle, rawButtonInfo, action);
    }

    @Inject(method = "grabMouse", at = @At("HEAD"), cancellable = true)
    private void narrativecraft$onGrabPre(CallbackInfo ci) {
        if (OnGrabClient.onGrabPre()) {
            ci.cancel();
        }
    }

    @Inject(method = "getScaledXPos(Lcom/mojang/blaze3d/platform/Window;D)D", at = @At("HEAD"), cancellable = true)
    private static void narrativecraft$studioScaledXPos(Window window, double x, CallbackInfoReturnable<Double> cir) {
        LayoutWindowsClient layout = LayoutWindowsClient.getInstance();
        if (!layout.hasGameArea()) return;
        double framebufferX = x * window.getWidth() / window.getScreenWidth();
        cir.setReturnValue((framebufferX - layout.getGameX()) / window.getGuiScale());
    }

    @Inject(method = "getScaledYPos(Lcom/mojang/blaze3d/platform/Window;D)D", at = @At("HEAD"), cancellable = true)
    private static void narrativecraft$studioScaledYPos(Window window, double y, CallbackInfoReturnable<Double> cir) {
        LayoutWindowsClient layout = LayoutWindowsClient.getInstance();
        if (!layout.hasGameArea()) return;
        double framebufferY = y * window.getHeight() / window.getScreenHeight();
        cir.setReturnValue((framebufferY - layout.getGameY()) / window.getGuiScale());
    }
}
