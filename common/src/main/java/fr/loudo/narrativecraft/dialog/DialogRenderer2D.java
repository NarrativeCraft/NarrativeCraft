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

import com.mojang.blaze3d.vertex.VertexConsumer;
import fr.loudo.narrativecraft.NarrativeCraftMod;
import fr.loudo.narrativecraft.client.ClientNarrativeCraftMod;
import fr.loudo.narrativecraft.client.screens.UnRemovableScreen;
import fr.loudo.narrativecraft.utils.UtilsClient;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.util.FastColor;
import org.joml.Matrix4f;

public class DialogRenderer2D extends DialogRenderer {

    public enum Anchor {
        TOP_LEFT,
        TOP_CENTER,
        TOP_RIGHT,
        CENTER_LEFT,
        CENTER,
        CENTER_RIGHT,
        BOTTOM_LEFT,
        BOTTOM_CENTER,
        BOTTOM_RIGHT
    }

    private static final float REFERENCE_GUI_SCALE = 3f;
    private static final float BOX_WIDTH_WITH_IMAGE = 360f;
    private static final float BOX_WIDTH_WITHOUT_IMAGE = 430f;
    private static final float BOX_HEIGHT = 80f;
    private static final float SKIP_INDICATOR_SIZE = 3f;
    private static final float SKIP_SLIDE_OFFSET = -5f;
    private static final float IMAGE_ZONE_PADDING = 6f;
    private static final float TEXT_MARGIN = 50f;

    private final Anchor anchor;
    private final int anchorOffsetX;
    private final int anchorOffsetY;

    public DialogRenderer2D(DialogData data, Anchor anchor, int anchorOffsetX, int anchorOffsetY) {
        super(data);
        this.anchor = anchor;
        this.anchorOffsetX = anchorOffsetX;
        this.anchorOffsetY = anchorOffsetY;
        data.setTextAlignment(DialogData.TextAlignment.CENTER);
    }

    public DialogRenderer2D(DialogData data) {
        this(data, Anchor.BOTTOM_CENTER, 0, -20);
    }

    public void render(GuiGraphics graphics, float deltaTracker) {
        Minecraft minecraft = Minecraft.getInstance();
        if (!(minecraft.screen instanceof UnRemovableScreen)
                && !ClientNarrativeCraftMod.getInstance().getPlayerSession().inCamera()) {
            minecraft.setScreen(new UnRemovableScreen());
        }

        if (animator.isStopped()) return;

        float partialTick = deltaTracker;
        float scale = animator.getScale(partialTick, 0.8f, 1f);
        if (scale <= 0f) return;

        float opacity = animator.getOpacity(partialTick, 0f, 1f);
        Minecraft mc = Minecraft.getInstance();
        int screenWidth = mc.getWindow().getGuiScaledWidth();
        int screenHeight = mc.getWindow().getGuiScaledHeight();

        float centerWidth = centerWidth();
        data.setWidth(centerWidth - TEXT_MARGIN);
        layout.compute(data, scrollText, mc.font, imageZoneHeight());

        float uiScale = UtilsClient.computeUiScale(REFERENCE_GUI_SCALE);

        float leftZoneWidth = imageZoneWidth(layout.getLeftGutterWidth());
        float rightZoneWidth = imageZoneWidth(layout.getRightGutterWidth());
        float boxWidth = centerWidth + leftZoneWidth + rightZoneWidth;

        float boxScale = uiScale * data.getScale();
        float renderedWidth = boxWidth * boxScale;
        float renderedHeight = BOX_HEIGHT * boxScale;

        float[] origin = computeAnchorOrigin(screenWidth, screenHeight, renderedWidth, renderedHeight);
        float dialogX = origin[0] + anchorOffsetX * uiScale;
        float dialogY = origin[1] + anchorOffsetY * uiScale;

        float centerX = dialogX + renderedWidth / 2f;
        float centerY = dialogY + renderedHeight / 2f;

        var pose = graphics.pose();
        pose.pushPose();
        pose.translate(centerX, centerY, 0);
        pose.scale(scale * boxScale, scale * boxScale, 1f);
        pose.translate(-boxWidth / 2f, -BOX_HEIGHT / 2f, 0);

        renderBackground(graphics, boxWidth, BOX_HEIGHT, opacity);
        renderText(graphics, boxWidth, centerWidth, leftZoneWidth, partialTick);
        renderSkipIndicator(graphics, boxWidth - rightZoneWidth, BOX_HEIGHT, opacity, partialTick);

        pose.popPose();
        graphics.flush();
    }

    private void renderBackground(GuiGraphics graphics, float boxWidth, float boxHeight, float opacity) {
        int color = applyOpacity(data.getBackgroundColor(), opacity);
        graphics.fill(0, 0, (int) boxWidth, (int) boxHeight, color);
    }

    private void renderText(
            GuiGraphics graphics, float boxWidth, float centerWidth, float leftZoneWidth, float partialTick) {
        Font font = Minecraft.getInstance().font;
        float textX = leftZoneWidth + (centerWidth - layout.getTextWidth()) / 2f;
        float textY = (BOX_HEIGHT - layout.getTextHeight()) / 2f;

        DialogScrollText.LayoutResult result =
                scrollText.computeLayout(textX, textY, data.getWidth(), imageZoneHeight(), font, data);
        scrollText.renderInline2D(graphics, result, data, partialTick);
        scrollText.renderSideImages2D(
                graphics, result, IMAGE_ZONE_PADDING, boxWidth - IMAGE_ZONE_PADDING, BOX_HEIGHT / 2f);
    }

    private float centerWidth() {
        return scrollText.hasSideImages() ? BOX_WIDTH_WITH_IMAGE : BOX_WIDTH_WITHOUT_IMAGE;
    }

    private float imageZoneHeight() {
        return BOX_HEIGHT - IMAGE_ZONE_PADDING * 2f;
    }

    private float imageZoneWidth(float gutterWidth) {
        return gutterWidth > 0f ? gutterWidth + IMAGE_ZONE_PADDING * 2f : 0f;
    }

    private void renderSkipIndicator(
            GuiGraphics graphics, float skipAreaRight, float boxHeight, float opacity, float partialTick) {
        float skipT = getSkipProgress(partialTick);
        if (skipT <= 0f || animator.isStopping()) return;

        int color = applyOpacity(0xFFFFFFFF, skipT * 0.9f * opacity);
        float centerX = skipAreaRight - 4f - SKIP_INDICATOR_SIZE + SKIP_SLIDE_OFFSET * (1f - skipT);
        float centerY = boxHeight - 4f - SKIP_INDICATOR_SIZE;

        var pose = graphics.pose();
        pose.pushPose();
        pose.translate(centerX, centerY, 0);

        VertexConsumer consumer = graphics.bufferSource().getBuffer(NarrativeCraftMod.dialogRenderType);
        Matrix4f matrix = pose.last().pose();
        float hw = SKIP_INDICATOR_SIZE;
        float hh = SKIP_INDICATOR_SIZE;
        consumer.vertex(matrix, -hw, -hh, 0.01f)
                .uv2(LightTexture.FULL_BRIGHT)
                .color(color)
                .endVertex();
        consumer.vertex(matrix, -hw, hh, 0.01f)
                .uv2(LightTexture.FULL_BRIGHT)
                .color(color)
                .endVertex();
        consumer.vertex(matrix, hw, 0, 0.01f)
                .uv2(LightTexture.FULL_BRIGHT)
                .color(color)
                .endVertex();
        consumer.vertex(matrix, -hw, -hh, 0.01f)
                .uv2(LightTexture.FULL_BRIGHT)
                .color(color)
                .endVertex();

        pose.popPose();
    }

    private float[] computeAnchorOrigin(int screenWidth, int screenHeight, float totalWidth, float totalHeight) {
        float x =
                switch (anchor) {
                    case TOP_LEFT, CENTER_LEFT, BOTTOM_LEFT -> 0f;
                    case TOP_CENTER, CENTER, BOTTOM_CENTER -> (screenWidth - totalWidth) / 2f;
                    case TOP_RIGHT, CENTER_RIGHT, BOTTOM_RIGHT -> screenWidth - totalWidth;
                };
        float y =
                switch (anchor) {
                    case TOP_LEFT, TOP_CENTER, TOP_RIGHT -> 0f;
                    case CENTER_LEFT, CENTER, CENTER_RIGHT -> (screenHeight - totalHeight) / 2f;
                    case BOTTOM_LEFT, BOTTOM_CENTER, BOTTOM_RIGHT -> screenHeight - totalHeight;
                };
        return new float[] {x, y};
    }

    private int applyOpacity(int color, float opacity) {
        int alpha = (int) (FastColor.ARGB32.alpha(color) * opacity);
        return FastColor.ARGB32.color(
                alpha, FastColor.ARGB32.red(color), FastColor.ARGB32.green(color), FastColor.ARGB32.blue(color));
    }
}
