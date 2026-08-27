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

import fr.loudo.narrativecraft.utils.FakePlayer;
import net.minecraft.network.protocol.game.ClientboundRotateHeadPacket;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.levelgen.synth.SimplexNoise;

public class DialogEntityBobbing {

    private final Entity entity;

    private float noiseShakeSpeed;
    private float noiseShakeStrength;

    private final SimplexNoise noise;
    private float noiseI = 0.0f;

    private float appliedOffsetXRot;
    private float appliedOffsetYRot;

    public DialogEntityBobbing(Entity entity, float noiseShakeSpeed, float noiseShakeStrength) {
        this.entity = entity;
        noise = new SimplexNoise(RandomSource.create());
        this.noiseShakeSpeed = noiseShakeSpeed;
        this.noiseShakeStrength = noiseShakeStrength;
    }

    public void reset() {
        if (entity == null) return;
        applyOffset(0.0f, 0.0f);
    }

    public void tick() {
        if (entity == null) return;
        if (noiseShakeSpeed == 0.0f && noiseShakeStrength == 0.0f) {
            reset();
            return;
        }
        noiseI += (1.0f / 20.0f) * noiseShakeSpeed;

        float currentOffsetXRot = (float) noise.getValue(1, noiseI) * noiseShakeStrength;
        float currentOffsetYRot = (float) noise.getValue(100, noiseI) * noiseShakeStrength;

        applyOffset(currentOffsetXRot, currentOffsetYRot);
    }

    private void applyOffset(float offsetXRot, float offsetYRot) {
        if (offsetXRot == appliedOffsetXRot && offsetYRot == appliedOffsetYRot) return;

        float deltaXRot = offsetXRot - appliedOffsetXRot;
        float deltaYRot = offsetYRot - appliedOffsetYRot;

        entity.setYRot(entity.getYRot() + deltaYRot);
        entity.setYHeadRot(entity.getYHeadRot() + deltaYRot);
        entity.setXRot(entity.getXRot() + deltaXRot);
        if (entity instanceof FakePlayer fakePlayer) {
            fakePlayer.connection.send(
                    new ClientboundRotateHeadPacket(entity, (byte) (entity.getYHeadRot() * 256 / 360)));
        }

        appliedOffsetXRot = offsetXRot;
        appliedOffsetYRot = offsetYRot;
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
