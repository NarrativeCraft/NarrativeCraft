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

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.RenderType;
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

    private static final float BOX_WIDTH = 430f;
    private static final float BOX_HEIGHT = 80f;
    private static final float SKIP_INDICATOR_SIZE = 3f;
    private static final float SKIP_SLIDE_OFFSET = -5f;

    private final Anchor anchor;
    private final int anchorOffsetX;
    private final int anchorOffsetY;

    public DialogRenderer2D(DialogData data, Anchor anchor, int anchorOffsetX, int anchorOffsetY) {
        super(data);
        this.anchor = anchor;
        this.anchorOffsetX = anchorOffsetX;
        this.anchorOffsetY = anchorOffsetY;
        data.setTextAlignment(DialogData.TextAlignment.CENTER);
        data.setWidth(BOX_WIDTH - 50f);
    }

    public DialogRenderer2D(DialogData data) {
        this(data, Anchor.BOTTOM_CENTER, 0, -40);
    }

    @Override
    public void tick() {
        super.tick();
    }

    @Override
    public void update(String newText) {
        super.update(newText);
        data.setWidth(BOX_WIDTH - 50f);
    }

    public void render(GuiGraphics graphics, DeltaTracker deltaTracker) {
        if (animator.isStopped()) return;

        float partialTick = deltaTracker.getGameTimeDeltaPartialTick(true);
        float scale = animator.getScale(partialTick, 0.8f, 1f);
        if (scale <= 0f) return;

        float opacity = animator.getOpacity(partialTick, 0f, 1f);
        Minecraft mc = Minecraft.getInstance();
        int screenWidth = mc.getWindow().getGuiScaledWidth();
        int screenHeight = mc.getWindow().getGuiScaledHeight();

        layout.compute(data, scrollText, mc.font);

        float totalWidth = BOX_WIDTH * data.getScale();
        float totalHeight = BOX_HEIGHT * data.getScale();

        float[] origin = computeAnchorOrigin(screenWidth, screenHeight, totalWidth, totalHeight);
        float dialogX = origin[0] + anchorOffsetX;
        float dialogY = origin[1] + anchorOffsetY;

        float centerX = dialogX + totalWidth / 2f;
        float centerY = dialogY + totalHeight / 2f;

        var pose = graphics.pose();
        pose.pushPose();
        pose.translate(centerX, centerY, 0);
        pose.scale(scale, scale, 0f);
        pose.translate(-totalWidth / 2f, -totalHeight / 2f, 0);

        renderBackground(graphics, totalWidth, totalHeight, opacity);
        renderText(graphics, totalWidth, partialTick);
        renderSkipIndicator(graphics, totalWidth, totalHeight, opacity, partialTick);

        pose.popPose();
        graphics.flush();
    }

    private void renderBackground(GuiGraphics graphics, float totalWidth, float totalHeight, float opacity) {
        int color = applyOpacity(data.getBackgroundColor(), opacity);
        graphics.fill(0, 0, (int) totalWidth, (int) totalHeight, color);
    }

    private void renderText(GuiGraphics graphics, float totalWidth, float partialTick) {
        float textX = (totalWidth - layout.getWidth()) / 2f;
        float textY = (BOX_HEIGHT - layout.getHeight()) / 2f;
        scrollText.render2D(graphics, textX, textY, data, partialTick);
    }

    private void renderSkipIndicator(
            GuiGraphics graphics, float totalWidth, float totalHeight, float opacity, float partialTick) {
        float skipT = getSkipProgress(partialTick);
        if (skipT <= 0f) return;

        int color = applyOpacity(0xFFFFFFFF, skipT * 0.9f * opacity);
        float centerX = totalWidth - (4f * data.getScale()) - SKIP_INDICATOR_SIZE + SKIP_SLIDE_OFFSET * (1f - skipT);
        float centerY = totalHeight - (4f * data.getScale()) - SKIP_INDICATOR_SIZE;

        var pose = graphics.pose();
        pose.pushPose();
        pose.translate(centerX, centerY, 0);

        VertexConsumer consumer = graphics.bufferSource().getBuffer(RenderType.textBackground());
        Matrix4f matrix = pose.last().pose();
        float hw = SKIP_INDICATOR_SIZE;
        float hh = SKIP_INDICATOR_SIZE;
        consumer.addVertex(matrix, -hw, -hh, 0.01f)
                .setLight(LightTexture.FULL_BRIGHT)
                .setColor(color);
        consumer.addVertex(matrix, -hw, hh, 0.01f)
                .setLight(LightTexture.FULL_BRIGHT)
                .setColor(color);
        consumer.addVertex(matrix, hw, 0, 0.01f)
                .setLight(LightTexture.FULL_BRIGHT)
                .setColor(color);
        consumer.addVertex(matrix, -hw, -hh, 0.01f)
                .setLight(LightTexture.FULL_BRIGHT)
                .setColor(color);

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
