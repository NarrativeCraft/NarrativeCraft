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

package fr.loudo.narrativecraft.dialog;

import com.mojang.blaze3d.vertex.PoseStack;
import fr.loudo.narrativecraft.client.rendering.Dialog3DRendererHelper;
import fr.loudo.narrativecraft.dialog.geometric.DialogTail;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.util.ARGB;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

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

    public void render(PoseStack poseStack, SubmitNodeCollector collector, float partialTick) {
        if (animator.isStopped()) return;

        float scale = data.getScale() * animator.getScale(partialTick, 0f, 1f) * 0.025f;
        if (scale <= 0f) return;

        float opacity = animator.getOpacity(partialTick, 0f, 1f);
        Camera camera = Minecraft.getInstance().gameRenderer.mainCamera();
        Vec3 camRight = new Vec3(camera.leftVector()).scale(-1);

        double interpX = Mth.lerp(partialTick, entity.xo, entity.getX());
        double interpY = Mth.lerp(partialTick, entity.yo, entity.getY());
        double interpZ = Mth.lerp(partialTick, entity.zo, entity.getZ());

        Vec3 cameraPos = camera.position();
        Vec3 entityHeadPos = new Vec3(
                interpX + data.getAncOffsetX(), interpY + entity.getEyeHeight() + data.getAncOffsetY(), interpZ);
        Vec3 dialogFinalPos = new Vec3(
                interpX + data.getAncOffsetX() + camRight.x * data.getOffsetX(),
                interpY
                        + entity.getEyeHeight()
                        + data.getAncOffsetY()
                        + data.getOffsetY()
                        + camRight.y * data.getOffsetX(),
                interpZ + camRight.z * data.getOffsetX());
        Vec3 dialogPos = animator.getPosition(entityHeadPos, dialogFinalPos, partialTick, 0f, 1f);

        layout.compute(data, scrollText, Minecraft.getInstance().font);
        checkAndApplyPendingResize();

        boolean resizing = animator.getState() == DialogAnimator.State.RESIZING;
        float totalWidth = resizing ? animator.getResizeWidth(partialTick, 0f, 1f) : layout.getTotalWidth();
        float totalHeight = resizing ? animator.getResizeHeight(partialTick, 0f, 1f) : layout.getTotalHeight();

        poseStack.pushPose();

        poseStack.translate(dialogPos.x - cameraPos.x, dialogPos.y - cameraPos.y, dialogPos.z - cameraPos.z);

        poseStack.mulPose(camera.rotation());

        poseStack.scale(scale, -scale, scale);

        float anchorX = data.getOffsetX() == 0 ? -totalWidth / 2f : (data.getOffsetX() > 0 ? 0f : -totalWidth);
        float anchorY = data.getOffsetY() == 0 ? -totalHeight / 2f : (data.getOffsetY() > 0 ? -totalHeight : 0f);
        poseStack.translate(anchorX, anchorY, 0);

        renderBackground(poseStack, collector, totalWidth, totalHeight, opacity);
        if (!isAnimating()) {
            renderText(poseStack, collector, partialTick);
        }

        if (data.isTailVisible()) {
            poseStack.pushPose();
            poseStack.translate(-anchorX, -anchorY, 0);
            tail.render(poseStack, collector, opacity);
            poseStack.popPose();
        }

        renderSkipIndicator(poseStack, collector, totalWidth, totalHeight, opacity, partialTick);

        poseStack.popPose();
    }

    public Vec3 getDialogPosition() {
        return new Vec3(entity.getX(), entity.getY() + entity.getBbHeight() + data.getOffsetY(), entity.getZ());
    }

    public Vec3 getAnchorPosition() {
        return new Vec3(
                entity.getX() + data.getAncOffsetX(),
                entity.getY() + entity.getEyeHeight() + data.getAncOffsetY(),
                entity.getZ());
    }

    public Vec3 translateToRelative(Vec3 worldPos) {
        Vec3 cameraPos = Minecraft.getInstance().gameRenderer.mainCamera().position();
        return worldPos.subtract(cameraPos);
    }

    public Vec3 translateToRelativeApplyOffset(Vec3 worldPos) {
        Camera camera = Minecraft.getInstance().gameRenderer.mainCamera();
        Vec3 relative = translateToRelative(worldPos);
        Vec3 right = new Vec3(camera.leftVector()).scale(-1);
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
            PoseStack poseStack, SubmitNodeCollector collector, float totalWidth, float totalHeight, float opacity) {
        if (data.getBackgroundImage() != null) {
            int color = applyOpacity(0xFFFFFFFF, opacity);
            Dialog3DRendererHelper.geometry(
                    collector,
                    Dialog3DRendererHelper.LAYER_BACKGROUND,
                    poseStack,
                    RenderTypes.textSeeThrough(data.getBackgroundImage()),
                    (pose, consumer) -> {
                        consumer.addVertex(pose, 0, 0, 0)
                                .setColor(color)
                                .setUv(0f, 0f)
                                .setLight(LightCoordsUtil.FULL_BRIGHT);
                        consumer.addVertex(pose, 0, totalHeight, 0)
                                .setColor(color)
                                .setUv(0f, 1f)
                                .setLight(LightCoordsUtil.FULL_BRIGHT);
                        consumer.addVertex(pose, totalWidth, totalHeight, 0)
                                .setColor(color)
                                .setUv(1f, 1f)
                                .setLight(LightCoordsUtil.FULL_BRIGHT);
                        consumer.addVertex(pose, totalWidth, 0, 0)
                                .setColor(color)
                                .setUv(1f, 0f)
                                .setLight(LightCoordsUtil.FULL_BRIGHT);
                    });
        } else {
            int color = applyOpacity(data.getBackgroundColor(), opacity);
            Dialog3DRendererHelper.geometry(
                    collector,
                    Dialog3DRendererHelper.LAYER_BACKGROUND,
                    poseStack,
                    RenderTypes.textBackgroundSeeThrough(),
                    (pose, consumer) -> {
                        consumer.addVertex(pose, 0, 0, 0)
                                .setLight(LightCoordsUtil.FULL_BRIGHT)
                                .setColor(color);
                        consumer.addVertex(pose, 0, totalHeight, 0)
                                .setLight(LightCoordsUtil.FULL_BRIGHT)
                                .setColor(color);
                        consumer.addVertex(pose, totalWidth, totalHeight, 0)
                                .setLight(LightCoordsUtil.FULL_BRIGHT)
                                .setColor(color);
                        consumer.addVertex(pose, totalWidth, 0, 0)
                                .setLight(LightCoordsUtil.FULL_BRIGHT)
                                .setColor(color);
                    });
        }
    }

    private void renderText(PoseStack poseStack, SubmitNodeCollector collector, float partialTick) {
        float textX = data.getPaddingX();
        float textY = data.getPaddingY();
        scrollText.render3D(poseStack, collector, textX, textY, data, partialTick);
    }

    private void renderSkipIndicator(
            PoseStack poseStack,
            SubmitNodeCollector collector,
            float totalWidth,
            float totalHeight,
            float opacity,
            float partialTick) {
        float skipT = getSkipProgress(partialTick);
        if (skipT <= 0f || animator.isStopping()) return;

        float finalX = totalWidth - data.getPaddingX() - SKIP_INDICATOR_SIZE;
        float y = totalHeight - SKIP_INDICATOR_SIZE / 2;
        float x = finalX + SKIP_SLIDE_OFFSET * (1f - skipT);

        int color = applyOpacity(0xFFFFFFFF, skipT * 0.9f * opacity);
        Dialog3DRendererHelper.geometry(
                collector,
                Dialog3DRendererHelper.LAYER_FOREGROUND,
                poseStack,
                RenderTypes.textBackgroundSeeThrough(),
                (pose, consumer) -> {
                    consumer.addVertex(pose, x, y, 0)
                            .setLight(LightCoordsUtil.FULL_BRIGHT)
                            .setColor(color);
                    consumer.addVertex(pose, x, y + SKIP_INDICATOR_SIZE, 0)
                            .setLight(LightCoordsUtil.FULL_BRIGHT)
                            .setColor(color);
                    consumer.addVertex(pose, x + SKIP_INDICATOR_SIZE, y + SKIP_INDICATOR_SIZE / 2f, 0)
                            .setLight(LightCoordsUtil.FULL_BRIGHT)
                            .setColor(color);
                    consumer.addVertex(pose, x, y, 0)
                            .setLight(LightCoordsUtil.FULL_BRIGHT)
                            .setColor(color);
                });
    }

    private int applyOpacity(int color, float opacity) {
        int alpha = (int) (ARGB.alpha(color) * opacity);
        return ARGB.color(alpha, ARGB.red(color), ARGB.green(color), ARGB.blue(color));
    }
}
