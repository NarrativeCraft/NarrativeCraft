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

package fr.loudo.narrativecraft.client.inkTag.actions.choiceTime;

import fr.loudo.narrativecraft.api.editors.cutscene.keyframes.EasingType;
import fr.loudo.narrativecraft.api.editors.cutscene.keyframes.Interpolation;
import fr.loudo.narrativecraft.client.gui.GuiGraphicsExtractorExtension;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.util.ARGB;

public class ChoiceTimeLineBar {

    private static final float MIN_OPACITY = 0.01f;

    private final LoadingBar loadingBar;

    private double lineWidth;
    private double lineHeight;
    private double offsetY;
    private EasingType easingType;

    private int fadeTotalTick;
    private int totalTick;
    private int currentTick;
    private int fadeOutTick;
    private boolean showText;
    private boolean finished;
    private boolean frozen;
    private float frozenProgress;

    private final Runnable onFinish;

    public ChoiceTimeLineBar(
            int loadingColor,
            int outlineColor,
            float outlinePadding,
            double lineWidth,
            double lineHeight,
            double offsetY,
            EasingType easingType,
            boolean showText,
            int fadeTotalTick,
            int totalTick,
            Runnable onFinish) {
        this.loadingBar = new LoadingBar(loadingColor, new Outline(outlineColor, outlinePadding));
        this.lineWidth = lineWidth;
        this.lineHeight = lineHeight;
        this.offsetY = offsetY;
        this.easingType = easingType;
        this.fadeTotalTick = fadeTotalTick;
        this.showText = showText;
        this.totalTick = totalTick;
        this.onFinish = onFinish;
    }

    public void tick() {
        if (frozen) {
            tickFadeOut();
            return;
        }
        if (currentTick < totalTick) {
            currentTick += 1;
        }
        if (currentTick >= totalTick) {
            if (!finished) {
                finished = true;
                onFinish.run();
            }
            tickFadeOut();
        }
    }

    private void tickFadeOut() {
        if (fadeOutTick < fadeTotalTick) {
            fadeOutTick += 1;
        }
    }

    public void render(GuiGraphicsExtractor guiGraphics, float partialTick) {
        loadingBar.render(guiGraphics, this, getOpacity(partialTick), getProgress(partialTick));
    }

    public float getProgress(float partialTick) {
        if (totalTick <= 0) {
            return 1.0f;
        }
        if (frozen) {
            return frozenProgress;
        }
        float elapsedTick = currentTick < totalTick ? currentTick + partialTick : totalTick;
        return (float) Interpolation.applyEasing(easingType, Math.clamp(elapsedTick / totalTick, 0.0f, 1.0f));
    }

    public float getOpacity(float partialTick) {
        if (fadeTotalTick <= 0) {
            return isFadingOut() ? 0.0f : 1.0f;
        }
        float fadeInProgress = Math.clamp((currentTick + partialTick) / fadeTotalTick, 0.0f, 1.0f);
        float fadeOutElapsedTick = isFadingOut() ? Math.min(fadeOutTick + partialTick, fadeTotalTick) : 0.0f;
        float fadeInOpacity =
                (float) Math.clamp(Interpolation.applyEasing(easingType, fadeInProgress), MIN_OPACITY, 1.0f);
        float fadeOutOpacity = (float) Interpolation.applyEasing(easingType, 1.0f - fadeOutElapsedTick / fadeTotalTick);
        return Math.clamp(Math.min(fadeInOpacity, fadeOutOpacity), 0.0f, 1.0f);
    }

    public void forceFinish() {
        if (frozen) {
            return;
        }
        frozenProgress = getProgress(0.0f);
        frozen = true;
        fadeOutTick = 0;
    }

    public boolean isFrozen() {
        return frozen;
    }

    public boolean isFadingOut() {
        return frozen || currentTick >= totalTick;
    }

    public boolean isFadeOutFinished() {
        return isFadingOut() && fadeOutTick >= fadeTotalTick;
    }

    public int getRemainingSeconds() {
        return Math.max(totalTick - currentTick, 0) / 20;
    }

    public double getLineWidth() {
        return lineWidth;
    }

    public void setLineWidth(double lineWidth) {
        this.lineWidth = lineWidth;
    }

    public double getLineHeight() {
        return lineHeight;
    }

    public void setLineHeight(double lineHeight) {
        this.lineHeight = lineHeight;
    }

    public double getOffsetY() {
        return offsetY;
    }

    public void setOffsetY(double offsetY) {
        this.offsetY = offsetY;
    }

    public EasingType getEasingType() {
        return easingType;
    }

    public void setEasingType(EasingType easingType) {
        this.easingType = easingType;
    }

    public int getFadeTotalTick() {
        return fadeTotalTick;
    }

    public void setFadeTotalTick(int fadeTotalTick) {
        this.fadeTotalTick = fadeTotalTick;
    }

    public int getTotalTick() {
        return totalTick;
    }

    public void setTotalTick(int totalTick) {
        this.totalTick = totalTick;
    }

    public int getCurrentTick() {
        return currentTick;
    }

    public void setCurrentTick(int currentTick) {
        this.currentTick = currentTick;
    }

    public boolean isShowText() {
        return showText;
    }

    public void setShowText(boolean showText) {
        this.showText = showText;
    }
}

record LoadingBar(int color, Outline outline) {

    public void render(GuiGraphicsExtractor graphics, ChoiceTimeLineBar lineBar, float opacity, float progress) {
        float rightX = (float) (graphics.guiWidth() / 2.0 + lineBar.getLineWidth() / 2);
        float leftX = (float) (rightX - lineBar.getLineWidth());
        float bottomY = (float) (graphics.guiHeight() - lineBar.getOffsetY());
        float topY = (float) (bottomY - lineBar.getLineHeight());

        GuiGraphicsExtractorExtension guiGraphicsExtractorExtension = new GuiGraphicsExtractorExtension(graphics);
        guiGraphicsExtractorExtension.fill(
                rightX + outline.padding(),
                bottomY + outline.padding(),
                leftX - outline.padding(),
                topY - outline.padding(),
                ARGB.color(opacity, outline.color()));

        float loadingRightX = (float) Interpolation.lerp(leftX, rightX, progress);
        guiGraphicsExtractorExtension.fill(loadingRightX, bottomY, leftX, topY, ARGB.color(opacity, color));

        if (lineBar.isShowText()) {
            String secondsString = lineBar.getRemainingSeconds() + "s";
            int middleTextX =
                    graphics.guiWidth() / 2 - Minecraft.getInstance().font.width(secondsString) / 2;
            int textY = (int) bottomY + 5;
            graphics.text(Minecraft.getInstance().font, secondsString, middleTextX, textY, ARGB.color(opacity, color));
        }
    }
}

record Outline(int color, float padding) {}
