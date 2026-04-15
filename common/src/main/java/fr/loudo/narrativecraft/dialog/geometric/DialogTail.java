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

package fr.loudo.narrativecraft.dialog.geometric;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import fr.loudo.narrativecraft.dialog.DialogRenderer3D;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;

public class DialogTail {

    private enum TailDirection {
        TOP,
        BOTTOM,
        LEFT,
        LEFT_UP_CORNER,
        LEFT_DOWN_CORNER,
        RIGHT,
        RIGHT_UP_CORNER,
        RIGHT_DOWN_CORNER
    }

    private final DialogRenderer3D dialog;
    private final float width;
    private final float height;
    private final float offset;

    public DialogTail(DialogRenderer3D dialog, float width, float height, float offset) {
        this.dialog = dialog;
        this.width = width;
        this.height = height;
        this.offset = offset;
    }

    public void render(
            PoseStack poseStack,
            float partialTick,
            MultiBufferSource.BufferSource bufferSource,
            Camera camera,
            float opacity) {
        TailDirection tailDirection = TailDirection.TOP;
        float offsetX = dialog.getDialogOffsetX();
        float offsetY = dialog.getDialogOffsetY();

        if (offsetX > 0) {
            tailDirection = TailDirection.LEFT;
        } else if (offsetX < 0) {
            tailDirection = TailDirection.RIGHT;
        } else if (offsetY > 0) {
            tailDirection = TailDirection.BOTTOM;
        }

        Vec2 tailOffset = getTailOffset(camera);

        poseStack.pushPose();

        if (offsetX != 0) {
            if (offsetY < 0) {
                poseStack.translate(0, (height / 2.0f) - width / 2.0f, 0);
            } else if (offsetY > 0) {
                poseStack.translate(0, -(height / 2.0f) + width / 2.0f, 0);
            }
        }

        if (tailDirection == TailDirection.RIGHT || tailDirection == TailDirection.LEFT) {
            float halfHeight = dialog.getDialogHeight() / 2f;
            float minY = -halfHeight + width / 2f;
            float maxY = halfHeight - width / 2f;

            if (offsetY != 0) {
                if (tailOffset.y < minY) {
                    tailDirection = tailDirection == TailDirection.RIGHT
                            ? TailDirection.RIGHT_UP_CORNER
                            : TailDirection.LEFT_UP_CORNER;
                } else if (tailOffset.y > maxY) {
                    tailDirection = tailDirection == TailDirection.RIGHT
                            ? TailDirection.RIGHT_DOWN_CORNER
                            : TailDirection.LEFT_DOWN_CORNER;
                }
            }
        }

        VertexConsumer vertexConsumer = bufferSource.getBuffer(RenderTypes.textBackgroundSeeThrough());
        Matrix4f matrix = poseStack.last().pose();

        float topRight = -width / 2 + offset;
        float topLeft = width / 2 + offset;

        switch (tailDirection) {
            case TOP -> drawTailTop(matrix, vertexConsumer, topRight, topLeft, opacity);
            case BOTTOM -> drawTailBottom(matrix, vertexConsumer, topRight, topLeft, opacity);
            case RIGHT -> drawTailRight(matrix, vertexConsumer, opacity);
            case RIGHT_UP_CORNER -> drawTailUpRightCorner(matrix, vertexConsumer, opacity);
            case RIGHT_DOWN_CORNER -> drawTailDownRightCorner(matrix, vertexConsumer, opacity);
            case LEFT -> drawTailLeft(matrix, vertexConsumer, opacity);
            case LEFT_UP_CORNER -> drawTailUpLeftCorner(matrix, vertexConsumer, opacity);
            case LEFT_DOWN_CORNER -> drawTailDownLeftCorner(matrix, vertexConsumer, opacity);
        }

        bufferSource.endBatch();
        poseStack.popPose();
    }

    public Vec2 getTailOffset(Camera camera) {
        Vec3 entityPos = dialog.translateToRelative(dialog.getDialogPosition());
        Vec3 dialogPos = dialog.translateToRelativeApplyOffset(dialog.getDialogPosition());
        Vec3 toDialog = dialogPos.subtract(entityPos);

        Vec3 camRight = new Vec3(camera.leftVector()).scale(-1);
        Vec3 camUp = new Vec3(camera.upVector());

        float scale = dialog.getData().getScale() * 0.025f;
        return new Vec2((float) (toDialog.dot(camRight) / scale), (float) (toDialog.dot(camUp) / scale));
    }

    private void addVertex(Matrix4f matrix, VertexConsumer consumer, float x, float y, float opacity) {
        int base = dialog.getData().getBackgroundColor();
        int alpha = (int) (net.minecraft.util.ARGB.alpha(base) * opacity);
        int color = net.minecraft.util.ARGB.color(
                alpha,
                net.minecraft.util.ARGB.red(base),
                net.minecraft.util.ARGB.green(base),
                net.minecraft.util.ARGB.blue(base));
        consumer.addVertex(matrix, x, y, 0)
                .setLight(LightCoordsUtil.FULL_BRIGHT)
                .setColor(color);
    }

    private void drawTailTop(Matrix4f m, VertexConsumer c, float topRight, float topLeft, float op) {
        addVertex(m, c, 0, -dialog.getDialogHeight() - height, op);
        addVertex(m, c, -topRight, -dialog.getDialogHeight(), op);
        addVertex(m, c, -topLeft, -dialog.getDialogHeight(), op);
        addVertex(m, c, -topRight, -dialog.getDialogHeight(), op);
    }

    private void drawTailBottom(Matrix4f m, VertexConsumer c, float topRight, float topLeft, float op) {
        addVertex(m, c, -topRight, 0, op);
        addVertex(m, c, -topLeft, 0, op);
        addVertex(m, c, 0, height, op);
        addVertex(m, c, -topRight, 0, op);
    }

    private void drawTailLeft(Matrix4f m, VertexConsumer c, float op) {
        addVertex(m, c, -height, 0, op);
        addVertex(m, c, 0, -width / 2, op);
        addVertex(m, c, 0, width / 2, op);
        addVertex(m, c, 0, -width / 2, op);
    }

    private void drawTailRight(Matrix4f m, VertexConsumer c, float op) {
        addVertex(m, c, height, 0, op);
        addVertex(m, c, 0, width / 2, op);
        addVertex(m, c, 0, -width / 2, op);
        addVertex(m, c, 0, width / 2, op);
    }

    private void drawTailUpRightCorner(Matrix4f m, VertexConsumer c, float op) {
        addVertex(m, c, height / 2, -4, op);
        addVertex(m, c, -width, -width / 2, op);
        addVertex(m, c, 0, width / 2, op);
        addVertex(m, c, height / 2, -4, op);
    }

    private void drawTailDownRightCorner(Matrix4f m, VertexConsumer c, float op) {
        addVertex(m, c, height / 2, 4, op);
        addVertex(m, c, 0, -width / 2, op);
        addVertex(m, c, -width, width / 2, op);
        addVertex(m, c, height / 2, 4, op);
    }

    private void drawTailUpLeftCorner(Matrix4f m, VertexConsumer c, float op) {
        addVertex(m, c, -height / 2, -4, op);
        addVertex(m, c, 0, width / 2, op);
        addVertex(m, c, width, -width / 2, op);
        addVertex(m, c, -height / 2, -4, op);
    }

    private void drawTailDownLeftCorner(Matrix4f m, VertexConsumer c, float op) {
        addVertex(m, c, -height / 2, 4, op);
        addVertex(m, c, width, width / 2, op);
        addVertex(m, c, width, -width * 2, op);
        addVertex(m, c, -height / 2, 4, op);
    }
}
