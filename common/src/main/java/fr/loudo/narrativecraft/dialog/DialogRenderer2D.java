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

import fr.loudo.narrativecraft.client.gui.GuiGraphicsExtractorExtension;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.util.ARGB;

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

    private static final float SKIP_INDICATOR_SIZE = 8f;

    private final Anchor anchor;
    private final int anchorOffsetX;
    private final int anchorOffsetY;

    private static final float SKIP_SLIDE_OFFSET = -5f;

    public DialogRenderer2D(DialogData data, Anchor anchor, int anchorOffsetX, int anchorOffsetY) {
        super(data);
        this.anchor = anchor;
        this.anchorOffsetX = anchorOffsetX;
        this.anchorOffsetY = anchorOffsetY;
        if (data.getTextAlignment() == DialogData.TextAlignment.LEFT) {
            data.setTextAlignment(DialogData.TextAlignment.CENTER);
        }
    }

    public DialogRenderer2D(DialogData data) {
        this(data, Anchor.BOTTOM_CENTER, 0, -40);
    }

    @Override
    public void tick() {
        super.tick();
    }

    public void render(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker) {
        if (animator.isStopped()) return;

        float partialTick = deltaTracker.getGameTimeDeltaPartialTick(true);
        float scale = animator.getScale(partialTick);
        if (scale <= 0f) return;

        float opacity = animator.getOpacity(partialTick);
        Minecraft mc = Minecraft.getInstance();
        int screenWidth = mc.getWindow().getGuiScaledWidth();
        int screenHeight = mc.getWindow().getGuiScaledHeight();

        // Recompute layout
        layout.compute(data, scrollText, mc.font);

        float totalWidth = layout.getTotalWidth() * data.getScale();
        float totalHeight = layout.getTotalHeight() * data.getScale();

        // Anchor position
        float[] origin = computeAnchorOrigin(screenWidth, screenHeight, totalWidth, totalHeight);
        float dialogX = origin[0] + anchorOffsetX;
        float dialogY = origin[1] + anchorOffsetY;

        // Apply scale from the dialog centre
        float centerX = dialogX + totalWidth / 2f;
        float centerY = dialogY + totalHeight / 2f;

        var pose = graphics.pose();
        pose.pushMatrix();
        pose.translate(centerX, centerY);
        pose.scale(scale, scale);
        pose.translate(-totalWidth / 2f, -totalHeight / 2f);

        renderBackground(graphics, totalWidth, totalHeight, opacity);
        renderText(graphics, totalWidth, totalHeight, partialTick, opacity);

        renderSkipIndicator(graphics, totalWidth, totalHeight, opacity, partialTick);

        pose.popMatrix();
    }

    private void renderBackground(GuiGraphicsExtractor graphics, float totalWidth, float totalHeight, float opacity) {
        int color = applyOpacity(data.getBackgroundColor(), opacity);
        graphics.fill(0, 0, (int) totalWidth, (int) totalHeight, color);
    }

    private void renderText(
            GuiGraphicsExtractor graphics, float totalWidth, float totalHeight, float partialTick, float opacity) {
        float textX =
                switch (data.getTextAlignment()) {
                    case LEFT -> data.getPaddingX() * data.getScale();
                    case CENTER -> totalWidth / 2f;
                    case RIGHT -> totalWidth - data.getPaddingX() * data.getScale();
                };
        float textY = (totalHeight - layout.getHeight()) / 2f;
        scrollText.render2D(graphics, textX, textY, data, partialTick);
    }

    private void renderSkipIndicator(
            GuiGraphicsExtractor graphics, float totalWidth, float totalHeight, float opacity, float partialTick) {
        float skipT = getSkipProgress(partialTick);
        if (skipT <= 0f) return;

        float finalX = totalWidth - (4f * data.getScale()) - SKIP_INDICATOR_SIZE;
        float iy = totalHeight - (4f * data.getScale()) - SKIP_INDICATOR_SIZE;
        float x = finalX + SKIP_SLIDE_OFFSET * (1f - skipT);
        int color = applyOpacity(0xFFFFFFFF, skipT * 0.9f * opacity);

        float halfWidth = SKIP_INDICATOR_SIZE / 2f;
        float halfHeight = SKIP_INDICATOR_SIZE / 2f;
        float cx = x + halfWidth;
        float cy = iy + halfHeight;

        GuiGraphicsExtractorExtension graphicsExtension = new GuiGraphicsExtractorExtension(graphics);
        graphicsExtension.skipArrow(cx, cy, halfWidth, halfHeight, color);
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
        int alpha = (int) (ARGB.alpha(color) * opacity);
        return ARGB.color(alpha, ARGB.red(color), ARGB.green(color), ARGB.blue(color));
    }
}
