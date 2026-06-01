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

package fr.loudo.narrativecraft.client.editors.rendering;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Vector3f;

public class CameraWireframeRenderer {

    public static void renderWireframe(
            VertexConsumer vertexConsumer,
            Matrix4f matrix,
            Vec3 worldPos,
            Vec3 rotation,
            Vec3 cameraPos,
            float width,
            float height,
            float depth,
            float red,
            float green,
            float blue,
            float alpha) {

        float halfWidth = width / 2f;
        float halfHeight = height / 2f;

        Matrix4f rotationMatrix = new Matrix4f()
                .rotateY((float) Math.toRadians(-rotation.y + 180))
                .rotateX((float) Math.toRadians(-rotation.x))
                .rotateZ((float) Math.toRadians(rotation.z));

        float ox = (float) (worldPos.x - cameraPos.x);
        float oy = (float) (worldPos.y - cameraPos.y);
        float oz = (float) (worldPos.z - cameraPos.z);

        Vector3f e = transform(rotationMatrix, 0, 0, 0, ox, oy, oz);

        Vector3f a = transform(rotationMatrix, -halfWidth, +halfHeight, -depth, ox, oy, oz);
        Vector3f b = transform(rotationMatrix, +halfWidth, +halfHeight, -depth, ox, oy, oz);
        Vector3f c = transform(rotationMatrix, +halfWidth, -halfHeight, -depth, ox, oy, oz);
        Vector3f d = transform(rotationMatrix, -halfWidth, -halfHeight, -depth, ox, oy, oz);

        drawLine(vertexConsumer, matrix, a, b, red, green, blue, alpha);
        drawLine(vertexConsumer, matrix, b, c, red, green, blue, alpha);
        drawLine(vertexConsumer, matrix, c, d, red, green, blue, alpha);
        drawLine(vertexConsumer, matrix, d, a, red, green, blue, alpha);

        drawLine(vertexConsumer, matrix, a, e, red, green, blue, alpha);
        drawLine(vertexConsumer, matrix, b, e, red, green, blue, alpha);
        drawLine(vertexConsumer, matrix, c, e, red, green, blue, alpha);
        drawLine(vertexConsumer, matrix, d, e, red, green, blue, alpha);
    }

    private static Vector3f transform(Matrix4f rotation, float lx, float ly, float lz, float ox, float oy, float oz) {
        Vector3f vector = new Vector3f(lx, ly, lz);
        rotation.transformPosition(vector);
        vector.add(ox, oy, oz);
        return vector;
    }

    private static void drawLine(
            VertexConsumer vertexConsumer,
            Matrix4f matrix,
            Vector3f from,
            Vector3f to,
            float red,
            float green,
            float blue,
            float alpha) {
        float dx = to.x - from.x;
        float dy = to.y - from.y;
        float dz = to.z - from.z;
        float length = (float) Math.sqrt(dx * dx + dy * dy + dz * dz);
        if (length > 0) {
            dx /= length;
            dy /= length;
            dz /= length;
        }
        vertexConsumer
                .addVertex(matrix, from.x, from.y, from.z)
                .setLineWidth(4.0f)
                .setColor(red, green, blue, alpha)
                .setNormal(dx, dy, dz);
        vertexConsumer
                .addVertex(matrix, to.x, to.y, to.z)
                .setLineWidth(4.0f)
                .setColor(red, green, blue, alpha)
                .setNormal(dx, dy, dz);
    }
}
