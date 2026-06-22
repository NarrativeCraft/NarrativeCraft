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

package fr.loudo.narrativecraft.dialog.geometric;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import fr.loudo.narrativecraft.client.rendering.Dialog3DRendererHelper;
import fr.loudo.narrativecraft.dialog.DialogRenderer3D;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.util.LightCoordsUtil;

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

    public void render(PoseStack poseStack, SubmitNodeCollector collector, float opacity) {
        TailDirection tailDirection = getTailDirection();

        poseStack.pushPose();

        float topRight = -width / 2 + offset;
        float topLeft = width / 2 + offset;

        Dialog3DRendererHelper.geometry(
                collector,
                Dialog3DRendererHelper.LAYER_BACKGROUND,
                poseStack,
                RenderTypes.textBackgroundSeeThrough(),
                (pose, vertexConsumer) -> {
                    switch (tailDirection) {
                        case TOP -> drawTailTop(pose, vertexConsumer, topRight, topLeft, opacity);
                        case BOTTOM -> drawTailBottom(pose, vertexConsumer, topRight, topLeft, opacity);
                        case RIGHT -> drawTailRight(pose, vertexConsumer, opacity);
                        case RIGHT_UP_CORNER -> drawTailUpRightCorner(pose, vertexConsumer, opacity);
                        case RIGHT_DOWN_CORNER -> drawTailDownRightCorner(pose, vertexConsumer, opacity);
                        case LEFT -> drawTailLeft(pose, vertexConsumer, opacity);
                        case LEFT_UP_CORNER -> drawTailUpLeftCorner(pose, vertexConsumer, opacity);
                        case LEFT_DOWN_CORNER -> drawTailDownLeftCorner(pose, vertexConsumer, opacity);
                    }
                });

        poseStack.popPose();
    }

    private TailDirection getTailDirection() {
        float offsetX = dialog.getDialogOffsetX();
        float offsetY = dialog.getDialogOffsetY();

        TailDirection tailDirection;
        if (offsetX > 0 && offsetY > 0) tailDirection = TailDirection.LEFT_DOWN_CORNER;
        else if (offsetX < 0 && offsetY > 0) tailDirection = TailDirection.RIGHT_DOWN_CORNER;
        else if (offsetX > 0 && offsetY < 0) tailDirection = TailDirection.LEFT_UP_CORNER;
        else if (offsetX < 0 && offsetY < 0) tailDirection = TailDirection.RIGHT_UP_CORNER;
        else if (offsetX > 0) tailDirection = TailDirection.LEFT;
        else if (offsetX < 0) tailDirection = TailDirection.RIGHT;
        else if (offsetY > 0) tailDirection = TailDirection.BOTTOM;
        else tailDirection = TailDirection.TOP;
        return tailDirection;
    }

    private void addVertex(PoseStack.Pose matrix, VertexConsumer consumer, float x, float y, float opacity) {
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

    private void drawTailTop(PoseStack.Pose m, VertexConsumer c, float topRight, float topLeft, float op) {
        addVertex(m, c, 0, -height, op);
        addVertex(m, c, -topRight, height, op);
        addVertex(m, c, -topLeft, 0, op);
        addVertex(m, c, -topRight, 0, op);
    }

    private void drawTailBottom(PoseStack.Pose m, VertexConsumer c, float topRight, float topLeft, float op) {
        addVertex(m, c, -topRight, 0, op);
        addVertex(m, c, -topLeft, 0, op);
        addVertex(m, c, 0, height, op);
        addVertex(m, c, -topRight, 0, op);
    }

    private void drawTailLeft(PoseStack.Pose m, VertexConsumer c, float op) {
        addVertex(m, c, -height, 0, op);
        addVertex(m, c, 0, -width / 2, op);
        addVertex(m, c, 0, width / 2, op);
        addVertex(m, c, 0, -width / 2, op);
    }

    private void drawTailRight(PoseStack.Pose m, VertexConsumer c, float op) {
        addVertex(m, c, height, 0, op);
        addVertex(m, c, 0, width / 2, op);
        addVertex(m, c, 0, -width / 2, op);
        addVertex(m, c, 0, width / 2, op);
    }

    private void drawTailUpRightCorner(PoseStack.Pose m, VertexConsumer c, float op) {
        addVertex(m, c, 0, 0, op);
        addVertex(m, c, 0, height / 2, op);
        addVertex(m, c, width / 2, -height / 2, op);
        addVertex(m, c, -width / 2, 0, op);
    }

    private void drawTailDownRightCorner(PoseStack.Pose m, VertexConsumer c, float op) {
        addVertex(m, c, 0, 0, op);
        addVertex(m, c, -width / 2, 0, op);
        addVertex(m, c, width / 2, height / 2, op);
        addVertex(m, c, 0, -height / 2, op);
    }

    private void drawTailUpLeftCorner(PoseStack.Pose m, VertexConsumer c, float op) {
        addVertex(m, c, 0, 0, op);
        addVertex(m, c, width / 2, 0, op);
        addVertex(m, c, -width / 2, -height / 2, op);
        addVertex(m, c, 0, height / 2, op);
    }

    private void drawTailDownLeftCorner(PoseStack.Pose m, VertexConsumer c, float op) {
        addVertex(m, c, -width / 2, height / 2, op);
        addVertex(m, c, width / 2, 0, op);
        addVertex(m, c, 0, 0, op);
        addVertex(m, c, 0, -height / 2, op);
    }
}
