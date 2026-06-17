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
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.renderer.SubmitNodeCollector;

public class OnRenderWorldEvent {

    public static void renderWorld(SubmitNodeCollector collector, PoseStack poseStack, DeltaTracker deltaTracker) {
        CutsceneMakerEditorPathRenderer.render(collector, poseStack, deltaTracker);
        CutsceneMakerEditorCameraRenderer.render(collector, poseStack, deltaTracker);
        CameraAngleMakerEditorCameraRenderer.render(collector, poseStack, deltaTracker);
        InteractionMakerEditorRenderer.render(collector, poseStack, deltaTracker);

        renderDialog3D(collector, poseStack, deltaTracker);
        renderClientInkActions(poseStack, deltaTracker);
        partialTickInkActions(deltaTracker);
    }

    private static void renderDialog3D(SubmitNodeCollector collector, PoseStack poseStack, DeltaTracker deltaTracker) {
        ClientPlayerSession session = ClientNarrativeCraftMod.getInstance().getPlayerSession();
        List<DialogRenderer3D> dialogs = session.getActiveDialog3DRenderers();
        if (dialogs.isEmpty()) return;

        float partialTick = deltaTracker.getGameTimeDeltaPartialTick(true);

        for (DialogRenderer3D dialog : dialogs) {
            dialog.render(poseStack, collector, partialTick);
        }
    }

    private static void renderClientInkActions(PoseStack poseStack, DeltaTracker deltaTracker) {
        ClientPlayerSession session = ClientNarrativeCraftMod.getInstance().getPlayerSession();
        for (InkAction action : session.getActiveClientInkActions()) {
            action.render(poseStack, deltaTracker.getGameTimeDeltaPartialTick(true));
        }
    }

    private static void partialTickInkActions(DeltaTracker deltaTracker) {
        ClientPlayerSession session = ClientNarrativeCraftMod.getInstance().getPlayerSession();
        for (InkAction action : session.getActiveClientInkActions()) {
            action.partialTick(deltaTracker.getGameTimeDeltaPartialTick(true));
        }
    }
}
