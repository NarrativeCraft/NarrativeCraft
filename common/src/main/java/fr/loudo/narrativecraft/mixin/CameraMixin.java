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

import fr.loudo.narrativecraft.client.ClientNarrativeCraftMod;
import fr.loudo.narrativecraft.client.editors.cutscene.ClientCutsceneMakerEditor;
import fr.loudo.narrativecraft.client.session.ClientPlayerSession;
import fr.loudo.narrativecraft.editors.cutscene.keyframes.KeyframePosition;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Camera.class)
public abstract class CameraMixin {

    @Shadow
    protected abstract void setPosition(Vec3 position);

    @Shadow
    protected abstract void setPosition(double x, double y, double z);

    @Shadow
    @Deprecated
    protected abstract void setRotation(float yRot, float xRot);

    @Shadow
    @Final
    private Quaternionf rotation;

    @Shadow
    private @Nullable Entity entity;

    @Shadow
    protected abstract float calculateFov(float partialTicks);

    @Shadow
    @Final
    private Minecraft minecraft;

    @Shadow
    private float oldFovModifier;

    @Shadow
    private float fovModifier;

    @Inject(method = "update", at = @At("RETURN"))
    private void narrativecraft$camera(DeltaTracker deltaTracker, CallbackInfo ci) {
        ClientPlayerSession playerSession =
                ClientNarrativeCraftMod.getInstance().getPlayerSession();
        if (playerSession == null) return;

        KeyframePosition keyframePosition =
                playerSession.getCutsceneDataSession().getKeyframePosition();
        if (keyframePosition == null) {
            ClientCutsceneMakerEditor editor =
                    ClientNarrativeCraftMod.getInstance().getCutsceneMakerEditor();
            if (editor != null && editor.getPreviewRoll() != 0f) {
                this.setRotation(minecraft.player.getYRot(), minecraft.player.getXRot());
                this.rotation.rotateZ(-(float) Math.toRadians(editor.getPreviewRoll()));
            }
            return;
        }

        Vec3 position = keyframePosition.getPosition();
        this.setPosition(position);
        this.setRotation((float) keyframePosition.getRotation().y, (float) keyframePosition.getRotation().x);
        this.rotation.rotateZ(
                -(float) Math.toRadians(keyframePosition.getRotation().z()));
    }

    @Inject(method = "calculateFov", at = @At("HEAD"), cancellable = true)
    private void modifyFov(float partialTicks, CallbackInfoReturnable<Float> cir) {
        ClientPlayerSession playerSession =
                ClientNarrativeCraftMod.getInstance().getPlayerSession();
        if (playerSession != null && playerSession.getCutsceneDataSession() != null) {
            float customFov = playerSession.getCutsceneDataSession().getFov();
            if (customFov == -1f) return;
            cir.setReturnValue(customFov * Mth.lerp(partialTicks, this.oldFovModifier, this.fovModifier));
        }
    }
}
