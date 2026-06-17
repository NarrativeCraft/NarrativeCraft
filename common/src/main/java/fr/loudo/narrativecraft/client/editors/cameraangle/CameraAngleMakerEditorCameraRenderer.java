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

package fr.loudo.narrativecraft.client.editors.cameraangle;

import com.mojang.blaze3d.vertex.PoseStack;
import fr.loudo.narrativecraft.client.ClientNarrativeCraftMod;
import fr.loudo.narrativecraft.client.editors.rendering.CameraWireframeRenderer;
import fr.loudo.narrativecraft.editors.EditorMaker;
import fr.loudo.narrativecraft.narrative.cameraangle.CameraView;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.network.chat.Style;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.phys.Vec3;

public class CameraAngleMakerEditorCameraRenderer {

    private static final float NAME_TAG_SCALE = 0.025f;

    public static void render(SubmitNodeCollector collector, PoseStack poseStack, DeltaTracker deltaTracker) {
        EditorMaker editorMaker =
                ClientNarrativeCraftMod.getInstance().getPlayerSession().getEditor();
        if (!(editorMaker instanceof ClientCameraAngleMakerEditorMaker cameraAngleEditor)) return;

        cameraAngleEditor.renderAnchorPoint(collector, poseStack);

        if (cameraAngleEditor.getPreviewCamera() != null) return;

        Minecraft minecraft = Minecraft.getInstance();
        Vec3 cameraPos = minecraft.gameRenderer.mainCamera().position();

        CameraView preview = cameraAngleEditor.getPreviewCamera();
        collector.submitCustomGeometry(poseStack, RenderTypes.lines(), (pose, vertexConsumer) -> {
            for (CameraView cameraView : cameraAngleEditor.getCameraViews()) {
                boolean isPreview = cameraView == preview;
                float red = isPreview ? 1.0F : 0.2F;
                float green = isPreview ? 0.6F : 0.8F;
                float blue = isPreview ? 0.2F : 1.0F;
                CameraWireframeRenderer.renderWireframe(
                        vertexConsumer,
                        pose.pose(),
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
        });

        renderNameTags(collector, poseStack, cameraAngleEditor, cameraPos, minecraft);
    }

    private static void renderNameTags(
            SubmitNodeCollector collector,
            PoseStack poseStack,
            ClientCameraAngleMakerEditorMaker editor,
            Vec3 cameraPos,
            Minecraft minecraft) {
        Font font = minecraft.font;

        for (CameraView cameraView : editor.getCameraViews()) {
            Vec3 position = cameraView.getPosition();
            poseStack.pushPose();
            poseStack.translate(position.x - cameraPos.x, position.y - cameraPos.y + 0.6, position.z - cameraPos.z);
            poseStack.mulPose(minecraft.gameRenderer.mainCamera().rotation());
            poseStack.scale(NAME_TAG_SCALE, -NAME_TAG_SCALE, NAME_TAG_SCALE);

            int width = font.width(cameraView.getName());
            collector.submitText(
                    poseStack,
                    -width / 2f,
                    0,
                    FormattedCharSequence.forward(cameraView.getName(), Style.EMPTY),
                    false,
                    Font.DisplayMode.SEE_THROUGH,
                    0xF000F0,
                    0xFFFFFFFF,
                    0x40000000,
                    0);
            poseStack.popPose();
        }
    }
}
