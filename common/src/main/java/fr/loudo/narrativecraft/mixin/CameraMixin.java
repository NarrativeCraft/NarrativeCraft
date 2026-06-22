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

import fr.loudo.narrativecraft.client.ClientNarrativeCraftMod;
import fr.loudo.narrativecraft.client.editors.cameraangle.ClientCameraAngleMakerEditorMaker;
import fr.loudo.narrativecraft.client.editors.cutscene.ClientCutsceneMakerEditorMaker;
import fr.loudo.narrativecraft.client.session.ClientPlayerSession;
import fr.loudo.narrativecraft.editors.cutscene.keyframes.KeyframePosition;
import fr.loudo.narrativecraft.narrative.cameraangle.CameraView;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

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

    @Inject(method = "setup", at = @At("RETURN"))
    private void narrativecraft$camera(
            BlockGetter p_90576_,
            Entity p_90577_,
            boolean p_90578_,
            boolean p_90579_,
            float p_90580_,
            CallbackInfo ci) {
        ClientPlayerSession playerSession =
                ClientNarrativeCraftMod.getInstance().getPlayerSession();
        if (playerSession == null) return;

        narrativecraft$handleCutsceneKeyframe(playerSession);
        narrativecraft$handleCameraAngle(playerSession);
    }

    private void narrativecraft$handleCutsceneKeyframe(ClientPlayerSession playerSession) {
        KeyframePosition keyframePosition =
                playerSession.getCutsceneDataSession().getKeyframePosition();
        if (keyframePosition == null) {
            ClientCutsceneMakerEditorMaker editor =
                    ClientNarrativeCraftMod.getInstance().getCutsceneMakerEditor();
            if (editor != null && editor.getPreviewRoll() != 0f) {
                this.setRotation(
                        Minecraft.getInstance().player.getYRot(),
                        Minecraft.getInstance().player.getXRot());
                this.rotation.rotateZ(-(float) Math.toRadians(editor.getPreviewRoll()));
            }
            return;
        }
        narrativecraft$overrideCamera(keyframePosition.getPosition(), keyframePosition.getRotation());
    }

    private void narrativecraft$handleCameraAngle(ClientPlayerSession playerSession) {
        CameraView cameraView = playerSession.getCameraView();
        if (cameraView == null) return;
        ClientCameraAngleMakerEditorMaker editor =
                ClientNarrativeCraftMod.getInstance().getCameraAngleMakerEditor();
        if (editor != null && editor.isEditingCameraViewPosition()) return;

        narrativecraft$overrideCamera(cameraView.getPosition(), cameraView.getRotation());
    }

    private void narrativecraft$overrideCamera(Vec3 position, Vec3 rotation) {
        this.setPosition(position);
        this.setRotation((float) rotation.y, (float) rotation.x);
        this.rotation.rotateZ(-(float) Math.toRadians(rotation.z()));
    }
}
