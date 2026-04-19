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

package fr.loudo.narrativecraft.client.editors.cutscene;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import fr.loudo.narrativecraft.client.ClientNarrativeCraftMod;
import fr.loudo.narrativecraft.client.editors.cutscene.layers.camera.CameraLayer;
import fr.loudo.narrativecraft.client.editors.rendering.CameraWireframeRenderer;
import fr.loudo.narrativecraft.editors.cutscene.keyframes.KeyframePosition;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;

public class CutsceneMakerEditorCameraRenderer {

    public static void render(PoseStack poseStack, DeltaTracker deltaTracker) {
        Minecraft mc = Minecraft.getInstance();
        ClientCutsceneMakerEditor editor = ClientNarrativeCraftMod.getInstance().getCutsceneMakerEditor();
        if (editor == null) return;

        if (editor.getPlayback().isPlaying()) return;
        Vec3 cameraPos = mc.gameRenderer.getMainCamera().position();
        VertexConsumer vertexConsumer = mc.renderBuffers().bufferSource().getBuffer(RenderTypes.lines());
        Matrix4f matrix4f = poseStack.last().pose();
        float tick = editor.getTick();

        for (CutsceneMakerEditorLayer editorLayer : editor.getEditorLayers()) {
            if (!(editorLayer.getLayer() instanceof CameraLayer cameraLayer)) continue;

            KeyframePosition keyframePosition = cameraLayer.getInterpolatedPosition(tick);
            if (keyframePosition == null) continue;

            CameraWireframeRenderer.renderWireframe(
                    vertexConsumer,
                    matrix4f,
                    keyframePosition.getPosition(),
                    keyframePosition.getRotation(),
                    cameraPos,
                    1.2f,
                    0.8f,
                    1.0f,
                    0.0F,
                    1.0F,
                    0.0F,
                    1.0F);
        }

        mc.renderBuffers().bufferSource().endBatch(RenderTypes.lines());
    }
}
