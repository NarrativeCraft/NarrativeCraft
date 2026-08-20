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

import com.mojang.blaze3d.vertex.PoseStack;
import fr.loudo.narrativecraft.api.inkAction.InkAction;
import fr.loudo.narrativecraft.client.ClientNarrativeCraftMod;
import fr.loudo.narrativecraft.client.inkTag.actions.ClientShakeScreenInkAction;
import fr.loudo.narrativecraft.client.session.ClientPlayerSession;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(GameRenderer.class)
public class GameRendererMixin {

    @Inject(method = "getFov", at = @At("RETURN"), cancellable = true)
    private void narrativecraft$getFov(
            Camera p_109142_, float p_109143_, boolean p_109144_, CallbackInfoReturnable<Double> cir) {
        ClientPlayerSession playerSession =
                ClientNarrativeCraftMod.getInstance().getPlayerSession();
        if (playerSession == null) return;
        float customFov = -1;

        if (playerSession.getCutsceneDataSession() != null) {
            customFov = playerSession.getCutsceneDataSession().getFov();
        }
        if (playerSession.getCameraView() != null) {
            customFov = playerSession.getCameraView().getFov();
        }
        if (customFov == -1f) return;

        cir.setReturnValue((double) customFov);
    }

    @Inject(method = "bobHurt", at = @At("RETURN"), cancellable = true)
    private void narrativecraft$shakeScreenInkAction(PoseStack poseStack, float partialTick, CallbackInfo ci) {
        ClientPlayerSession session = ClientNarrativeCraftMod.getInstance().getPlayerSession();
        boolean hasShakingActive = false;
        for (InkAction action : session.getActiveClientInkActions()) {
            if (!(action instanceof ClientShakeScreenInkAction clientShakeScreenInkAction)) continue;
            clientShakeScreenInkAction.shakeScreen(poseStack, partialTick);
            hasShakingActive = true;
        }
        if (!hasShakingActive && session.inCamera()) {
            ci.cancel();
        }
    }

    @Inject(method = "bobView", at = @At("HEAD"), cancellable = true)
    private void narrativecraft$cancelBobCamera(PoseStack poseStack, float partialTicks, CallbackInfo ci) {
        ClientPlayerSession session = ClientNarrativeCraftMod.getInstance().getPlayerSession();
        if (session.inCamera()) {
            ci.cancel();
        }
    }
}
