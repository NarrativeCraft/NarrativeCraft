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

package fr.loudo.narrativecraft.client.editors.cameraangle;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import fr.loudo.narrativecraft.client.ClientNarrativeCraftMod;
import fr.loudo.narrativecraft.client.editors.rendering.CameraWireframeRenderer;
import fr.loudo.narrativecraft.editors.EditorMaker;
import fr.loudo.narrativecraft.narrative.cameraangle.CameraView;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;

public class CameraAngleMakerEditorCameraRenderer {

    private static final float NAME_TAG_SCALE = 0.025f;

    public static void render(PoseStack poseStack, DeltaTracker deltaTracker) {
        EditorMaker editorMaker =
                ClientNarrativeCraftMod.getInstance().getPlayerSession().getEditor();
        if (!(editorMaker instanceof ClientCameraAngleMakerEditorMaker cameraAngleEditor)) return;

        cameraAngleEditor.renderAnchorPoint(poseStack);

        if (cameraAngleEditor.getPreviewCamera() != null) return;

        Minecraft minecraft = Minecraft.getInstance();
        Vec3 cameraPos = minecraft.gameRenderer.getMainCamera().getPosition();
        RenderSystem.lineWidth(4.0f);
        VertexConsumer vertexConsumer = minecraft.renderBuffers().bufferSource().getBuffer(RenderType.lines());
        Matrix4f matrix = poseStack.last().pose();

        CameraView preview = cameraAngleEditor.getPreviewCamera();
        for (CameraView cameraView : cameraAngleEditor.getCameraViews()) {
            boolean isPreview = cameraView == preview;
            float red = isPreview ? 1.0F : 0.2F;
            float green = isPreview ? 0.6F : 0.8F;
            float blue = isPreview ? 0.2F : 1.0F;
            CameraWireframeRenderer.renderWireframe(
                    vertexConsumer,
                    matrix,
                    cameraView.getPosition(),
                    cameraView.getRotation(),
                    cameraPos,
                    0.6f,
                    0.4f,
                    0.5f,
                    red,
                    green,
                    blue,
                    1.0F);
        }

        minecraft.renderBuffers().bufferSource().endBatch(RenderType.lines());

        renderNameTags(poseStack, cameraAngleEditor, cameraPos, minecraft);
    }

    private static void renderNameTags(
            PoseStack poseStack, ClientCameraAngleMakerEditorMaker editor, Vec3 cameraPos, Minecraft minecraft) {
        Font font = minecraft.font;
        MultiBufferSource.BufferSource bufferSource = minecraft.renderBuffers().bufferSource();

        for (CameraView cameraView : editor.getCameraViews()) {
            Vec3 position = cameraView.getPosition();
            poseStack.pushPose();
            poseStack.translate(position.x - cameraPos.x, position.y - cameraPos.y + 0.6, position.z - cameraPos.z);
            poseStack.mulPose(minecraft.gameRenderer.getMainCamera().rotation());
            poseStack.scale(NAME_TAG_SCALE, -NAME_TAG_SCALE, NAME_TAG_SCALE);
            Matrix4f matrix = poseStack.last().pose();

            int width = font.width(cameraView.getName());
            font.drawInBatch(
                    cameraView.getName(),
                    -width / 2f,
                    0,
                    0xFFFFFFFF,
                    false,
                    matrix,
                    bufferSource,
                    Font.DisplayMode.SEE_THROUGH,
                    0x40000000,
                    LightTexture.FULL_BRIGHT);
            poseStack.popPose();
        }

        bufferSource.endBatch();
    }
}
