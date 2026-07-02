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
import com.mojang.math.Axis;
import fr.loudo.narrativecraft.api.inkAction.InkAction;
import fr.loudo.narrativecraft.client.ClientNarrativeCraftMod;
import fr.loudo.narrativecraft.client.inkTag.actions.ClientShakeScreenInkAction;
import fr.loudo.narrativecraft.client.session.ClientPlayerSession;
import fr.loudo.narrativecraft.editors.cutscene.keyframes.KeyframePosition;
import fr.loudo.narrativecraft.events.client.OnHudRender;
import fr.loudo.narrativecraft.narrative.cameraangle.CameraView;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderBuffers;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(GameRenderer.class)
public class GameRendererMixin {

    @Shadow
    @Final
    private Minecraft minecraft;

    @Shadow
    @Final
    private RenderBuffers renderBuffers;

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

    @Inject(method = "bobHurt", at = @At("RETURN"))
    private void narrativecraft$shakeScreenInkAction(PoseStack poseStack, float partialTick, CallbackInfo ci) {
        ClientPlayerSession session = ClientNarrativeCraftMod.getInstance().getPlayerSession();
        for (InkAction action : session.getActiveClientInkActions()) {
            if (!(action instanceof ClientShakeScreenInkAction clientShakeScreenInkAction)) continue;
            clientShakeScreenInkAction.shakeScreen(poseStack, partialTick);
        }
    }

    @Inject(method = "bobView", at = @At("HEAD"), cancellable = true)
    private void narrativecraft$cancelBobCamera(PoseStack poseStack, float partialTicks, CallbackInfo ci) {
        ClientPlayerSession session = ClientNarrativeCraftMod.getInstance().getPlayerSession();
        if (session.inCamera()) ci.cancel();
    }

    @Redirect(
            method = "render",
            at =
                    @At(
                            value = "NEW",
                            target =
                                    "(Lnet/minecraft/client/Minecraft;Lnet/minecraft/client/renderer/MultiBufferSource$BufferSource;)Lnet/minecraft/client/gui/GuiGraphics;"))
    private GuiGraphics narrativecraft$hudRender(Minecraft minecraft, MultiBufferSource.BufferSource bufferSource) {
        float partialTick = minecraft.getFrameTime();
        GuiGraphics graphics = new GuiGraphics(minecraft, bufferSource);
        OnHudRender.cutsceneHudRender(graphics, partialTick);
        OnHudRender.cameraAngleHudRender(graphics, partialTick);
        OnHudRender.interactionHudRender(graphics, partialTick);
        OnHudRender.clientInkActionsHudRender(graphics, partialTick);
        OnHudRender.saveIconHudRender(graphics, partialTick);
        OnHudRender.dialogHudRender(graphics, partialTick);
        return graphics;
    }

    @Inject(method = "renderLevel", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Camera;getXRot()F"))
    private void narrativecraft$rotateCamera(
            float partialTicks, long finishTimeNano, PoseStack poseStack, CallbackInfo ci) {
        ClientPlayerSession playerSession =
                ClientNarrativeCraftMod.getInstance().getPlayerSession();
        if (playerSession == null) return;
        KeyframePosition keyframePosition =
                playerSession.getCutsceneDataSession().getKeyframePosition();
        if (keyframePosition != null) {
            poseStack.mulPose(Axis.ZP.rotation((float) Math.toRadians(keyframePosition.getRotation().z)));
        }
        CameraView cameraView = playerSession.getCameraView();
        if (cameraView != null) {
            poseStack.mulPose(Axis.ZP.rotation((float) Math.toRadians(cameraView.getRotation().z)));
        }
    }
}
