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

package fr.loudo.narrativecraft.client.inkTag.actions;

import com.mojang.blaze3d.vertex.PoseStack;
import fr.loudo.narrativecraft.api.inkAction.InkActionResult;
import fr.loudo.narrativecraft.api.session.IPlayerSession;
import fr.loudo.narrativecraft.narrative.inkTag.actions.ShakeScreenInkAction;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.levelgen.synth.SimplexNoise;

public class ClientShakeScreenInkAction extends ShakeScreenInkAction {

    private static final float PIXEL = 0.025f;

    private SimplexNoise noise;
    private float noiseIndex = 0.0f;
    private float shakeStrength = 0.0f;

    private float lastOffsetX = 0.0f;
    private float lastOffsetY = 0.0f;
    private float currentOffsetX = 0.0f;
    private float currentOffsetY = 0.0f;

    @Override
    public void stop() {
        super.stop();
        shakeStrength = 0.0f;
        noiseIndex = 0.0f;
        currentOffsetX = currentOffsetY = lastOffsetX = lastOffsetY = 0.0f;
    }

    @Override
    public void tick() {
        if (!isRunning) return;

        noiseIndex += (1.0f / 20.0f) * noiseShakeSpeed;

        float decayFactor = Mth.clamp(shakeDecayRate * (1.0f / 20.0f), 0.0f, 1.0f);
        shakeStrength = Mth.lerp(decayFactor, shakeStrength, 0.0f);

        lastOffsetX = currentOffsetX;
        lastOffsetY = currentOffsetY;
        currentOffsetX = (float) noise.getValue(1, noiseIndex) * shakeStrength;
        currentOffsetY = (float) noise.getValue(100, noiseIndex) * shakeStrength;

        if (Math.abs(shakeStrength) < 0.001f) {
            isRunning = false;
            currentOffsetX = currentOffsetY = lastOffsetX = lastOffsetY = 0.0f;
        }
    }

    /**
     * Empty to not be called twice.
     *
     * @see #shakeScreen(PoseStack, float)
     */
    @Override
    public void render(PoseStack poseStack, float partialTick) {}

    public void shakeScreen(PoseStack poseStack, float partialTick) {
        float interpolatedX = Mth.lerp(partialTick, lastOffsetX, currentOffsetX);
        float interpolatedY = Mth.lerp(partialTick, lastOffsetY, currentOffsetY);
        poseStack.translate(interpolatedX, interpolatedY, 0);
    }

    @Override
    protected InkActionResult doExecute(IPlayerSession playerSession) {
        if (noiseShakeStrength == 0 && shakeDecayRate == 0 && noiseShakeSpeed == 0) {
            isRunning = false;
            return InkActionResult.ok();
        }
        noise = new SimplexNoise(RandomSource.create());
        shakeStrength = noiseShakeStrength * PIXEL;
        noiseIndex = 0.0f;
        lastOffsetX = lastOffsetY = currentOffsetX = currentOffsetY = 0.0f;
        return InkActionResult.ok();
    }
}
