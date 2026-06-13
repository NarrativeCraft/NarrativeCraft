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

import fr.loudo.narrativecraft.events.client.OnScreenMouseClickEvent;
import fr.loudo.narrativecraft.events.client.OnScreenMouseDragEvent;
import net.minecraft.client.gui.components.events.ContainerEventHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ContainerEventHandler.class)
public interface ContainerEventHandlerMixinFabric {

    @Inject(method = "mouseClicked", at = @At("HEAD"))
    private void narrativecraft$mouseClicked(
            double mouseX, double mouseY, int button, CallbackInfoReturnable<Boolean> cir) {
        OnScreenMouseClickEvent.cutsceneHudClick(mouseX, mouseY, button, false);
    }

    @Inject(method = "mouseReleased", at = @At("HEAD"))
    private void narrativecraft$mouseReleased(
            double mouseX, double mouseY, int button, CallbackInfoReturnable<Boolean> cir) {
        OnScreenMouseClickEvent.cutsceneHudRelease(mouseX, mouseY, button);
    }

    @Inject(method = "mouseDragged", at = @At("HEAD"))
    private void narrativecraft$mouseDragged(
            double mouseX, double mouseY, int button, double dragX, double dragY, CallbackInfoReturnable<Boolean> cir) {
        OnScreenMouseDragEvent.onCutsceneTimelineDrag(mouseX, mouseY, button, dragX, dragY);
    }
}
