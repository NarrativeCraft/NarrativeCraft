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

import fr.loudo.narrativecraft.api.editors.cutscene.keyframes.EasingType;
import fr.loudo.narrativecraft.api.editors.cutscene.keyframes.Interpolation;
import fr.loudo.narrativecraft.client.settings.NarrativeClientSettings;

public abstract class DialogRenderer {

    protected final DialogLayout layout = new DialogLayout();
    protected final DialogAnimator animator = new DialogAnimator();
    protected final DialogScrollText scrollText;
    protected DialogData data;

    private Runnable onStoppedCallback;
    private Runnable onAutoSkippedCallback;
    private int autoSkipTicks = -1;
    private int currentTick = 0;

    private boolean pendingResize = false;
    private float resizeFromWidth;
    private float resizeFromHeight;

    private static final float SKIP_APPEAR_TICKS = 10f;
    private float skipProgress = 0f;
    private float previousSkipProgress = 0f;

    protected DialogRenderer(DialogData data) {
        this.data = data;
        animator.onStopped(() -> {
            if (onStoppedCallback != null) onStoppedCallback.run();
        });
        scrollText = new DialogScrollText(data.getLetterSound(), data.isSoundMuted());
    }

    public void start(String text) {
        scrollText.setText(text);
        animator.startAppear();
        autoSkipTicks = data.isAutoSkipEnabled() || NarrativeClientSettings.autoSkip
                ? 60
                : -1; // TODO: calculate auto skip seconds based on text length
        currentTick = 0;
        skipProgress = 0f;
        previousSkipProgress = 0f;
    }

    public void update(String newText) {
        resizeFromWidth = layout.getTotalWidth();
        resizeFromHeight = layout.getTotalHeight();
        scrollText.setText(newText);
        skipProgress = 0f;
        previousSkipProgress = 0f;
        pendingResize = true;
    }

    protected void checkAndApplyPendingResize() {
        if (!pendingResize) return;
        pendingResize = false;
        animator.startResize(resizeFromWidth, resizeFromHeight, layout.getTotalWidth(), layout.getTotalHeight());
    }

    public void stop() {
        animator.startDisappear();
    }

    public void autoSkipAt(float seconds) {
        autoSkipTicks = (int) (seconds * 20f);
    }

    public void tick() {
        animator.tick();

        if (animator.isTextScrollAllowed()) {
            scrollText.tick(data.getScrollSpeed() > 0 ? data.getScrollSpeed() : NarrativeClientSettings.textSpeed);
        }

        previousSkipProgress = skipProgress;
        if (scrollText.isFinished() && skipProgress < 1f) {
            skipProgress = Math.min(1f, skipProgress + 1f / SKIP_APPEAR_TICKS);
        }

        if (autoSkipTicks > 0 && scrollText.isFinished()) {
            currentTick++;
            if (currentTick >= autoSkipTicks) {
                if (onAutoSkippedCallback != null) onAutoSkippedCallback.run();
                stop();
                autoSkipTicks = -1;
            }
        }
    }

    protected float getSkipProgress(float partialTick) {
        float t = previousSkipProgress + (skipProgress - previousSkipProgress) * partialTick;
        t = Math.clamp(t, 0f, 1f);
        return (float) Interpolation.applyEasing(EasingType.EASE_OUT, t);
    }

    public boolean isTextFinished() {
        return scrollText.isFinished();
    }

    public void forceFinishText() {
        scrollText.forceFinish();
    }

    public boolean isAnimating() {
        return animator.isResizing() || animator.isStopping() || animator.isStarting();
    }

    public void onStopped(Runnable callback) {
        this.onStoppedCallback = callback;
    }

    public void onAutoSkipped(Runnable callback) {
        this.onAutoSkippedCallback = callback;
    }

    public boolean isStopped() {
        return animator.isStopped();
    }

    public DialogData getData() {
        return data;
    }

    public void setData(DialogData data) {
        this.data = data;
    }

    public DialogLayout getLayout() {
        return layout;
    }

    public DialogAnimator getAnimator() {
        return animator;
    }

    public DialogScrollText getScrollText() {
        return scrollText;
    }
}
