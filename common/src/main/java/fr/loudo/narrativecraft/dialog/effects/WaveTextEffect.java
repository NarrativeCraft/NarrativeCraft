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

package fr.loudo.narrativecraft.dialog.effects;

import fr.loudo.narrativecraft.api.dialog.ITextEffect;
import java.util.Map;
import net.minecraft.world.phys.Vec2;

public class WaveTextEffect implements ITextEffect {

    private static final float AMPLITUDE = 1.5f;
    private static final float SPEED = 4f;
    private static final float PHASE_OFFSET = 0.4f;

    @Override
    public Vec2 apply(int letterIndex, long tick, float partialTick, Map<String, String> params) {
        double time = (tick + partialTick) / 20.0 * SPEED;
        float offsetY = (float) Math.sin(time + letterIndex * PHASE_OFFSET) * AMPLITUDE;
        return new Vec2(0f, offsetY);
    }
}
