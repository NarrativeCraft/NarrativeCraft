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
import fr.loudo.narrativecraft.editors.cutscene.keyframes.CameraKeyframe;
import fr.loudo.narrativecraft.narrative.NarrativeEnvironment;
import java.util.List;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;

public class CutsceneMakerEditorPathRenderer {

    // Number of ticks between each sampled point along the pathString
    private static final int SAMPLE_STEP = 2;

    public static void render(PoseStack poseStack, DeltaTracker deltaTracker) {
        Minecraft mc = Minecraft.getInstance();
        ClientCutsceneMakerEditorMaker editor =
                ClientNarrativeCraftMod.getInstance().getCutsceneMakerEditor();
        if (editor == null || editor.getEnvironment() != NarrativeEnvironment.DEVELOPMENT) return;

        if (editor.getPlayback().isPlaying()) return;
        Vec3 cameraPos = mc.gameRenderer.getMainCamera().position();
        VertexConsumer vertexConsumer = mc.renderBuffers().bufferSource().getBuffer(RenderTypes.lines());
        Matrix4f matrix4f = poseStack.last().pose();

        for (CutsceneMakerEditorLayer editorLayer : editor.getEditorLayers()) {
            if (!(editorLayer.getLayer() instanceof CameraLayer cameraLayer)) continue;
            List<CameraKeyframe> keyframes = cameraLayer.getSortedCameraKeyframes();
            if (keyframes.size() < 2) continue;

            int firstTick = keyframes.get(0).getTick();
            int lastTick = keyframes.get(keyframes.size() - 1).getTick();

            Vec3 prev = cameraLayer.getInterpolatedPosition(firstTick).getPosition();
            int tick = firstTick + SAMPLE_STEP;
            while (tick < lastTick) {
                Vec3 curr = cameraLayer.getInterpolatedPosition(tick).getPosition();
                drawSegment(vertexConsumer, matrix4f, prev, curr, cameraPos);
                prev = curr;
                tick += SAMPLE_STEP;
            }
            // Always connect to the exact last keyframe position
            drawSegment(
                    vertexConsumer,
                    matrix4f,
                    prev,
                    cameraLayer.getInterpolatedPosition(lastTick).getPosition(),
                    cameraPos);
        }

        mc.renderBuffers().bufferSource().endBatch(RenderTypes.lines());
    }

    private static void drawSegment(VertexConsumer vc, Matrix4f matrix, Vec3 from, Vec3 to, Vec3 cameraPos) {
        float x1 = (float) (from.x - cameraPos.x);
        float y1 = (float) (from.y - cameraPos.y);
        float z1 = (float) (from.z - cameraPos.z);
        float x2 = (float) (to.x - cameraPos.x);
        float y2 = (float) (to.y - cameraPos.y);
        float z2 = (float) (to.z - cameraPos.z);
        float dx = x2 - x1, dy = y2 - y1, dz = z2 - z1;
        float len = (float) Math.sqrt(dx * dx + dy * dy + dz * dz);
        if (len > 0) {
            dx /= len;
            dy /= len;
            dz /= len;
        }
        vc.addVertex(matrix, x1, y1, z1)
                .setColor(1.0F, 1.0F, 0.0F, 1.0F)
                .setLineWidth(1.0F)
                .setNormal(dx, dy, dz);
        vc.addVertex(matrix, x2, y2, z2)
                .setColor(1.0F, 1.0F, 0.0F, 1.0F)
                .setLineWidth(.0F)
                .setNormal(dx, dy, dz);
    }
}
