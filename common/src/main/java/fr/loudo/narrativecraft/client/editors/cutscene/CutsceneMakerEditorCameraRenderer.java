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
import fr.loudo.narrativecraft.editors.cutscene.keyframes.KeyframePosition;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Vector3f;

public class CutsceneMakerEditorCameraRenderer {

    private static final float HALF_WIDTH = 0.60f;
    private static final float HALF_HEIGHT = 0.40f;
    private static final float DEPTH = 1.0f;

    public static void render(PoseStack poseStack, DeltaTracker deltaTracker) {
        Minecraft mc = Minecraft.getInstance();
        ClientCutsceneMakerEditor editor = ClientNarrativeCraftMod.getInstance().getCutsceneMakerEditor();
        if (editor == null) return;

        Vec3 cameraPos = mc.gameRenderer.getMainCamera().position();
        VertexConsumer vertexConsumer = mc.renderBuffers().bufferSource().getBuffer(RenderTypes.lines());
        Matrix4f matrix4f = poseStack.last().pose();
        float tick = editor.getTick();

        for (CutsceneMakerEditorLayer editorLayer : editor.getEditorLayers()) {
            if (!(editorLayer.getLayer() instanceof CameraLayer cameraLayer)) continue;

            KeyframePosition kfPos = cameraLayer.getInterpolatedPosition(tick);
            if (kfPos == null) continue;

            renderCamera(vertexConsumer, matrix4f, kfPos, cameraPos);
        }

        mc.renderBuffers().bufferSource().endBatch(RenderTypes.lines());
    }

    private static void renderCamera(VertexConsumer vc, Matrix4f matrix, KeyframePosition kfPos, Vec3 cameraPos) {
        Vec3 worldPos = kfPos.getPosition();
        Vec3 rot = kfPos.getRotation();

        Matrix4f rotation = new Matrix4f()
                .rotateY((float) Math.toRadians(-rot.y + 180))
                .rotateX((float) Math.toRadians(-rot.x))
                .rotateZ((float) Math.toRadians(rot.z));

        float ox = (float) (worldPos.x - cameraPos.x);
        float oy = (float) (worldPos.y - cameraPos.y);
        float oz = (float) (worldPos.z - cameraPos.z);

        // Apex (tail) at z=0 → sits exactly on the path line
        Vector3f e = transform(rotation, 0, 0, 0, ox, oy, oz);

        // Front rectangle (lens) at z=-DEPTH → extends forward from the path
        Vector3f a = transform(rotation, -HALF_WIDTH, +HALF_HEIGHT, -DEPTH, ox, oy, oz);
        Vector3f b = transform(rotation, +HALF_WIDTH, +HALF_HEIGHT, -DEPTH, ox, oy, oz);
        Vector3f c = transform(rotation, +HALF_WIDTH, -HALF_HEIGHT, -DEPTH, ox, oy, oz);
        Vector3f d = transform(rotation, -HALF_WIDTH, -HALF_HEIGHT, -DEPTH, ox, oy, oz);

        drawLine(vc, matrix, a, b);
        drawLine(vc, matrix, b, c);
        drawLine(vc, matrix, c, d);
        drawLine(vc, matrix, d, a);

        drawLine(vc, matrix, a, e);
        drawLine(vc, matrix, b, e);
        drawLine(vc, matrix, c, e);
        drawLine(vc, matrix, d, e);
    }

    private static Vector3f transform(Matrix4f rotation, float lx, float ly, float lz, float ox, float oy, float oz) {
        Vector3f v = new Vector3f(lx, ly, lz);
        rotation.transformPosition(v);
        v.add(ox, oy, oz);
        return v;
    }

    private static void drawLine(VertexConsumer vc, Matrix4f matrix, Vector3f from, Vector3f to) {
        float dx = to.x - from.x;
        float dy = to.y - from.y;
        float dz = to.z - from.z;
        float len = (float) Math.sqrt(dx * dx + dy * dy + dz * dz);
        if (len > 0) {
            dx /= len;
            dy /= len;
            dz /= len;
        }
        vc.addVertex(matrix, from.x, from.y, from.z)
                .setLineWidth(4.0f)
                .setColor(0.0F, 1.0F, 0.0F, 1.0F)
                .setNormal(dx, dy, dz);
        vc.addVertex(matrix, to.x, to.y, to.z)
                .setLineWidth(4.0f)
                .setColor(0.0F, 1.0F, 0.0F, 1.0F)
                .setNormal(dx, dy, dz);
    }
}
