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

package fr.loudo.narrativecraft.dialog;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import fr.loudo.narrativecraft.dialog.geometric.DialogTail;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.util.FastColor;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;

public class DialogRenderer3D extends DialogRenderer {

    private static final float SKIP_INDICATOR_SIZE = 3.5f;

    private final Entity entity;
    private final DialogTail tail;

    private static final float SKIP_SLIDE_OFFSET = -4f;

    public DialogRenderer3D(DialogData data, Entity entity) {
        super(data);
        this.entity = entity;
        this.tail = new DialogTail(this, 3f, 5f, 0f);
    }

    @Override
    public void tick() {
        super.tick();
    }

    public void render(PoseStack poseStack, MultiBufferSource.BufferSource bufferSource, float partialTick) {
        if (animator.isStopped()) return;

        float scale = data.getScale() * animator.getScale(partialTick) * 0.025f;
        if (scale <= 0f) return;

        float opacity = animator.getOpacity(partialTick);
        Camera camera = Minecraft.getInstance().gameRenderer.getMainCamera();
        Vec3 camRight = new Vec3(camera.getLeftVector()).scale(-1);

        double interpX = Mth.lerp(partialTick, entity.xo, entity.getX());
        double interpY = Mth.lerp(partialTick, entity.yo, entity.getY());
        double interpZ = Mth.lerp(partialTick, entity.zo, entity.getZ());

        Vec3 cameraPos = camera.getPosition();
        // Animation: dialog slides from entity head (t=0) to its final position (t=1)
        Vec3 entityHeadPos = new Vec3(interpX, interpY + entity.getEyeHeight(), interpZ);
        Vec3 dialogFinalPos = new Vec3(
                interpX + camRight.x * data.getOffsetX(),
                interpY + entity.getEyeHeight() + data.getOffsetY() + camRight.y * data.getOffsetX(),
                interpZ + camRight.z * data.getOffsetX());
        Vec3 dialogPos = animator.getPosition(entityHeadPos, dialogFinalPos, partialTick);

        // Recompute layout
        layout.compute(data, scrollText, Minecraft.getInstance().font);
        checkAndApplyPendingResize();

        boolean resizing = animator.getState() == DialogAnimator.State.RESIZING;
        float totalWidth = resizing ? animator.getResizeWidth(partialTick) : layout.getTotalWidth();
        float totalHeight = resizing ? animator.getResizeHeight(partialTick) : layout.getTotalHeight();

        poseStack.pushPose();

        // Translate to dialog position (relative to camera), offset already included in dialogFinalPos
        poseStack.translate(dialogPos.x - cameraPos.x, dialogPos.y - cameraPos.y, dialogPos.z - cameraPos.z);

        // Billboard: align to camera plane
        poseStack.mulPose(camera.rotation());

        // Scale from entity head (scale origin = anchor corner of dialog)
        poseStack.scale(scale, -scale, scale);

        // Anchor point: determined by offset direction so the dialog never covers the entity head
        float anchorX = data.getOffsetX() == 0 ? -totalWidth / 2f : (data.getOffsetX() > 0 ? 0f : -totalWidth);
        float anchorY = data.getOffsetY() == 0 ? -totalHeight / 2f : (data.getOffsetY() > 0 ? -totalHeight : 0f);
        poseStack.translate(anchorX, anchorY, 0);

        renderBackground(poseStack, bufferSource, totalWidth, totalHeight, opacity);
        if (!isAnimating()) {
            renderText(poseStack, bufferSource, partialTick);
            bufferSource.endBatch();
        }

        if (data.isTailVisible()) {
            // Translate tail to the anchor corner (the corner closest to the entity head)
            poseStack.pushPose();
            poseStack.translate(-anchorX, -anchorY, 0);
            tail.render(poseStack, partialTick, bufferSource, camera, opacity);
            poseStack.popPose();
        }

        renderSkipIndicator(poseStack, bufferSource, totalWidth, totalHeight, opacity, partialTick);

        poseStack.popPose();
    }

    public Vec3 getDialogPosition() {
        return new Vec3(entity.getX(), entity.getY() + entity.getBbHeight() + data.getOffsetY(), entity.getZ());
    }

    public Vec3 translateToRelative(Vec3 worldPos) {
        Vec3 cameraPos = Minecraft.getInstance().gameRenderer.getMainCamera().getPosition();
        return worldPos.subtract(cameraPos);
    }

    public Vec3 translateToRelativeApplyOffset(Vec3 worldPos) {
        Camera camera = Minecraft.getInstance().gameRenderer.getMainCamera();
        Vec3 relative = translateToRelative(worldPos);
        Vec3 right = new Vec3(camera.getLeftVector()).scale(-1);
        return relative.add(right.scale(data.getOffsetX()));
    }

    public float getDialogOffsetX() {
        return data.getOffsetX();
    }

    public float getDialogOffsetY() {
        return data.getOffsetY();
    }

    public float getDialogHeight() {
        return layout.getTotalHeight();
    }

    private void renderBackground(
            PoseStack poseStack,
            MultiBufferSource.BufferSource bufferSource,
            float totalWidth,
            float totalHeight,
            float opacity) {
        Matrix4f matrix = poseStack.last().pose();

        if (data.getBackgroundImage() != null) {
            int color = applyOpacity(0xFFFFFFFF, opacity);
            VertexConsumer consumer = bufferSource.getBuffer(RenderType.textSeeThrough(data.getBackgroundImage()));
            consumer.addVertex(matrix, 0, 0, 0).setColor(color).setUv(0f, 0f).setLight(LightTexture.FULL_BRIGHT);
            consumer.addVertex(matrix, 0, totalHeight, 0)
                    .setColor(color)
                    .setUv(0f, 1f)
                    .setLight(LightTexture.FULL_BRIGHT);
            consumer.addVertex(matrix, totalWidth, totalHeight, 0)
                    .setColor(color)
                    .setUv(1f, 1f)
                    .setLight(LightTexture.FULL_BRIGHT);
            consumer.addVertex(matrix, totalWidth, 0, 0)
                    .setColor(color)
                    .setUv(1f, 0f)
                    .setLight(LightTexture.FULL_BRIGHT);
        } else {
            int color = applyOpacity(data.getBackgroundColor(), opacity);
            VertexConsumer consumer = bufferSource.getBuffer(RenderType.textBackgroundSeeThrough());
            consumer.addVertex(matrix, 0, 0, 0)
                    .setLight(LightTexture.FULL_BRIGHT)
                    .setColor(color);
            consumer.addVertex(matrix, 0, totalHeight, 0)
                    .setLight(LightTexture.FULL_BRIGHT)
                    .setColor(color);
            consumer.addVertex(matrix, totalWidth, totalHeight, 0)
                    .setLight(LightTexture.FULL_BRIGHT)
                    .setColor(color);
            consumer.addVertex(matrix, totalWidth, 0, 0)
                    .setLight(LightTexture.FULL_BRIGHT)
                    .setColor(color);
        }

        bufferSource.endBatch();
    }

    private void renderText(PoseStack poseStack, MultiBufferSource bufferSource, float partialTick) {
        float textX = data.getPaddingX();
        float textY = data.getPaddingY();
        scrollText.render3D(poseStack, bufferSource, textX, textY, data, partialTick);
    }

    private void renderSkipIndicator(
            PoseStack poseStack,
            MultiBufferSource.BufferSource bufferSource,
            float totalWidth,
            float totalHeight,
            float opacity,
            float partialTick) {
        float skipT = getSkipProgress(partialTick);
        if (skipT <= 0f) return;

        float finalX = totalWidth - data.getPaddingX() - SKIP_INDICATOR_SIZE;
        float y = totalHeight - SKIP_INDICATOR_SIZE / 2;
        // Slide from right (finalX + SLIDE) to finalX as skipT goes 0 → 1
        float x = finalX + SKIP_SLIDE_OFFSET * (1f - skipT);

        int color = applyOpacity(0xFFFFFFFF, skipT * 0.9f * opacity);
        VertexConsumer consumer = bufferSource.getBuffer(RenderType.textBackgroundSeeThrough());
        Matrix4f matrix = poseStack.last().pose();

        consumer.addVertex(matrix, x, y, 0).setLight(LightTexture.FULL_BRIGHT).setColor(color);
        consumer.addVertex(matrix, x, y + SKIP_INDICATOR_SIZE, 0)
                .setLight(LightTexture.FULL_BRIGHT)
                .setColor(color);
        consumer.addVertex(matrix, x + SKIP_INDICATOR_SIZE, y + SKIP_INDICATOR_SIZE / 2f, 0)
                .setLight(LightTexture.FULL_BRIGHT)
                .setColor(color);
        consumer.addVertex(matrix, x, y, 0).setLight(LightTexture.FULL_BRIGHT).setColor(color);

        bufferSource.endBatch();
    }

    private int applyOpacity(int color, float opacity) {
        int alpha = (int) (FastColor.ARGB32.alpha(color) * opacity);
        return FastColor.ARGB32.color(
                alpha, FastColor.ARGB32.red(color), FastColor.ARGB32.green(color), FastColor.ARGB32.blue(color));
    }
}
