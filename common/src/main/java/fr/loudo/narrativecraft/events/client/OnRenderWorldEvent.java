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

package fr.loudo.narrativecraft.events.client;

import com.mojang.blaze3d.vertex.PoseStack;
import fr.loudo.narrativecraft.api.inkAction.InkAction;
import fr.loudo.narrativecraft.client.ClientNarrativeCraftMod;
import fr.loudo.narrativecraft.client.editors.cameraangle.CameraAngleMakerEditorCameraRenderer;
import fr.loudo.narrativecraft.client.editors.cutscene.CutsceneMakerEditorCameraRenderer;
import fr.loudo.narrativecraft.client.editors.cutscene.CutsceneMakerEditorPathRenderer;
import fr.loudo.narrativecraft.client.editors.interaction.InteractionMakerEditorRenderer;
import fr.loudo.narrativecraft.client.session.ClientPlayerSession;
import fr.loudo.narrativecraft.dialog.DialogRenderer3D;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;

public class OnRenderWorldEvent {

    public static void renderWorld(PoseStack poseStack, float deltaTracker) {
        poseStack.pushPose();

        CutsceneMakerEditorPathRenderer.render(poseStack, deltaTracker);
        CutsceneMakerEditorCameraRenderer.render(poseStack, deltaTracker);
        CameraAngleMakerEditorCameraRenderer.render(poseStack, deltaTracker);
        InteractionMakerEditorRenderer.render(poseStack, deltaTracker);

        renderDialog3D(poseStack, deltaTracker);
        renderClientInkActions(poseStack, deltaTracker);
        partialTickInkActions(deltaTracker);

        poseStack.popPose();
    }

    private static void renderDialog3D(PoseStack poseStack, float deltaTracker) {
        ClientPlayerSession session = ClientNarrativeCraftMod.getInstance().getPlayerSession();
        List<DialogRenderer3D> dialogs = session.getActiveDialog3DRenderers();
        if (dialogs.isEmpty()) return;

        float partialTick = deltaTracker;
        MultiBufferSource.BufferSource bufferSource =
                Minecraft.getInstance().renderBuffers().bufferSource();

        for (DialogRenderer3D dialog : dialogs) {
            dialog.render(poseStack, bufferSource, partialTick);
        }
    }

    private static void renderClientInkActions(PoseStack poseStack, float deltaTracker) {
        ClientPlayerSession session = ClientNarrativeCraftMod.getInstance().getPlayerSession();
        for (InkAction action : session.getActiveClientInkActions()) {
            action.render(poseStack, deltaTracker);
        }
    }

    private static void partialTickInkActions(float deltaTracker) {
        ClientPlayerSession session = ClientNarrativeCraftMod.getInstance().getPlayerSession();
        for (InkAction action : session.getActiveClientInkActions()) {
            action.partialTick(deltaTracker);
        }
    }
}
