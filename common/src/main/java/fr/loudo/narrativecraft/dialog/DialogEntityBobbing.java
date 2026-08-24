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

import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.levelgen.synth.SimplexNoise;

public class DialogEntityBobbing {

    private final Entity entity;

    private float noiseShakeSpeed;
    private float noiseShakeStrength;

    private final SimplexNoise noise;
    private float noiseI = 0.0f;

    private float lastXRot;
    private float lastYRot;

    public DialogEntityBobbing(Entity entity, float noiseShakeSpeed, float noiseShakeStrength) {
        if (entity != null) {
            lastXRot = entity.getXRot();
            lastYRot = entity.getYRot();
        }
        this.entity = entity;
        noise = new SimplexNoise(RandomSource.create());
        this.noiseShakeSpeed = noiseShakeSpeed;
        this.noiseShakeStrength = noiseShakeStrength;
    }

    public void reset() {
        if (entity == null) return;
        entity.setYRot(lastYRot);
        entity.setYHeadRot(lastYRot);
        entity.setXRot(lastXRot);
    }

    public void tick() {
        if (entity == null) return;
        noiseI += (1.0f / 20.0f) * noiseShakeSpeed;

        float currentOffsetX = (float) noise.getValue(1, noiseI) * noiseShakeStrength;
        float currentOffsetY = (float) noise.getValue(100, noiseI) * noiseShakeStrength;

        entity.setYRot(lastYRot + currentOffsetY);
        entity.setYHeadRot(lastYRot + currentOffsetY);
        entity.setXRot(lastXRot + currentOffsetX);
    }

    public float getNoiseShakeSpeed() {
        return noiseShakeSpeed;
    }

    public void setNoiseShakeSpeed(float noiseShakeSpeed) {
        this.noiseShakeSpeed = noiseShakeSpeed;
    }

    public float getNoiseShakeStrength() {
        return noiseShakeStrength;
    }

    public void setNoiseShakeStrength(float noiseShakeStrength) {
        this.noiseShakeStrength = noiseShakeStrength;
    }
}
