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

import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

public class DialogAnimator {

    public enum State {
        STARTING,
        RUNNING,
        RESIZING,
        STOPPING,
        STOPPED
    }

    private static final float TRANSITION_TICKS = 8f;

    private State state = State.STOPPED;
    private float animationProgress = 0f;
    private float previousProgress = 0f;
    private float resizeProgress = 0f;
    private float previousResizeProgress = 0f;
    private Runnable onStoppedCallback;

    private float resizeFromWidth;
    private float resizeFromHeight;
    private float resizeToWidth;
    private float resizeToHeight;

    public void tick() {
        previousProgress = animationProgress;
        previousResizeProgress = resizeProgress;
        switch (state) {
            case STARTING -> {
                animationProgress += 1f / TRANSITION_TICKS;
                if (animationProgress >= 1f) {
                    animationProgress = 1f;
                    state = State.RUNNING;
                }
            }
            case STOPPING -> {
                animationProgress -= 1f / TRANSITION_TICKS;
                if (animationProgress <= 0f) {
                    animationProgress = 0f;
                    state = State.STOPPED;
                    if (onStoppedCallback != null) {
                        onStoppedCallback.run();
                    }
                }
            }
            case RESIZING -> {
                resizeProgress += 1f / TRANSITION_TICKS;
                if (resizeProgress >= 1f) {
                    resizeProgress = 1f;
                    state = State.RUNNING;
                }
            }
            default -> {}
        }
    }

    public void startAppear() {
        state = State.STARTING;
        animationProgress = 0f;
        previousProgress = 0f;
    }

    public void startDisappear() {
        if (state == State.STOPPED) return;
        state = State.STOPPING;
    }

    public void startResize(float fromWidth, float fromHeight, float toWidth, float toHeight) {
        this.resizeFromWidth = fromWidth;
        this.resizeFromHeight = fromHeight;
        this.resizeToWidth = toWidth;
        this.resizeToHeight = toHeight;
        resizeProgress = 0f;
        state = State.RESIZING;
    }

    public void onStopped(Runnable callback) {
        this.onStoppedCallback = callback;
    }

    public float getScale(float partialTick) {
        return smoothStep(getInterpolatedProgress(partialTick));
    }

    public float getOpacity(float partialTick) {
        return getInterpolatedProgress(partialTick);
    }

    public Vec3 getPosition(Vec3 fromPos, Vec3 toPos, float partialTick) {
        float t = smoothStep(getInterpolatedProgress(partialTick));
        return new Vec3(
                Mth.lerp(t, fromPos.x, toPos.x), Mth.lerp(t, fromPos.y, toPos.y), Mth.lerp(t, fromPos.z, toPos.z));
    }

    public float getResizeWidth(float partialTick) {
        if (state != State.RESIZING) return resizeToWidth;
        float t = smoothStep(getInterpolatedResize(partialTick));
        return resizeFromWidth + (resizeToWidth - resizeFromWidth) * t;
    }

    public float getResizeHeight(float partialTick) {
        if (state != State.RESIZING) return resizeToHeight;
        float t = smoothStep(getInterpolatedResize(partialTick));
        return resizeFromHeight + (resizeToHeight - resizeFromHeight) * t;
    }

    public boolean isStopped() {
        return state == State.STOPPED;
    }

    public boolean isRunning() {
        return state == State.RUNNING;
    }

    public boolean isTextScrollAllowed() {
        return state == State.RUNNING || state == State.RESIZING;
    }

    public State getState() {
        return state;
    }

    private float getInterpolatedProgress(float partialTick) {
        return Math.clamp(previousProgress + (animationProgress - previousProgress) * partialTick, 0f, 1f);
    }

    private float getInterpolatedResize(float partialTick) {
        return Math.clamp(previousResizeProgress + (resizeProgress - previousResizeProgress) * partialTick, 0f, 1f);
    }

    private float smoothStep(float t) {
        return t * t * (3f - 2f * t);
    }
}
