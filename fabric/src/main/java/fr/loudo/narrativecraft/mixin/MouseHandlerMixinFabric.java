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

package fr.loudo.narrativecraft.mixin;

import fr.loudo.narrativecraft.events.client.OnPressButtonEvent;
import fr.loudo.narrativecraft.events.client.OnScreenMouseClickEvent;
import fr.loudo.narrativecraft.events.client.OnScreenMouseDragEvent;
import fr.loudo.narrativecraft.events.client.OnScreenMouseScrollEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.MouseHandler;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.input.MouseButtonInfo;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MouseHandler.class)
public abstract class MouseHandlerMixinFabric {

    @Shadow
    @Final
    private Minecraft minecraft;

    @Inject(method = "onButton", at = @At("RETURN"))
    private void narrativecraft$onButtonPress(long handle, MouseButtonInfo rawButtonInfo, int action, CallbackInfo ci) {
        OnPressButtonEvent.pressButton(action);
    }

    @Redirect(
            method = "onButton",
            at =
                    @At(
                            value = "INVOKE",
                            target =
                                    "Lnet/minecraft/client/gui/screens/Screen;mouseClicked(Lnet/minecraft/client/input/MouseButtonEvent;Z)Z"))
    private boolean narrativecraft$onScreenMouseClick(
            Screen instance, MouseButtonEvent mouseButtonEvent, boolean isDoubleClick) {
        OnScreenMouseClickEvent.cutsceneHudClick(mouseButtonEvent, isDoubleClick);
        return instance.mouseClicked(mouseButtonEvent, isDoubleClick);
    }

    @Redirect(
            method = "onButton",
            at =
                    @At(
                            value = "INVOKE",
                            target =
                                    "Lnet/minecraft/client/gui/screens/Screen;mouseReleased(Lnet/minecraft/client/input/MouseButtonEvent;)Z"))
    private boolean narrativecraft$onScreenMouseRelease(Screen instance, MouseButtonEvent mouseButtonEvent) {
        OnScreenMouseClickEvent.cutsceneHudRelease(mouseButtonEvent);
        return instance.mouseReleased(mouseButtonEvent);
    }

    @Inject(
            method = "onScroll",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screens/Screen;mouseScrolled(DDDD)Z"))
    private void narrativecraft$onMouseScroll(long windowPointer, double xOffset, double yOffset, CallbackInfo ci) {
        boolean flag = this.minecraft.options.discreteMouseScroll().get();
        double d0 = this.minecraft.options.mouseWheelSensitivity().get();
        double deltaX = (flag ? Math.signum(xOffset) : xOffset) * d0;
        double deltaY = (flag ? Math.signum(yOffset) : yOffset) * d0;

        OnScreenMouseScrollEvent.onCutsceneLayerMouseScroll(deltaX, deltaY);
    }

    @Redirect(
            method = "handleAccumulatedMovement",
            at =
                    @At(
                            value = "INVOKE",
                            target =
                                    "Lnet/minecraft/client/gui/screens/Screen;mouseDragged(Lnet/minecraft/client/input/MouseButtonEvent;DD)Z"))
    private boolean narrativecraft$onMouseDragged(Screen instance, MouseButtonEvent event, double dragX, double dragY) {
        OnScreenMouseDragEvent.onCutsceneTimelineDrag(event, dragX, dragY);
        return instance.mouseDragged(event, dragX, dragY);
    }
}
