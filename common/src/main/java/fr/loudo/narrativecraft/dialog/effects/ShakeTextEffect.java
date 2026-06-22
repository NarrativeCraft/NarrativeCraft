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
import java.util.Random;
import net.minecraft.world.phys.Vec2;

public class ShakeTextEffect implements ITextEffect {

    @Override
    public Vec2 apply(int letterIndex, long tick, float partialTick, Map<String, String> params) {
        float force = parseFloat(params, "force", 1.0f);
        float time = parseFloat(params, "time", 0.05f);

        int shakeCycle = (int) ((tick + partialTick) / Math.max(0.001f, time * 20f));
        long baseHash = shakeCycle * 1000003L + letterIndex * 999983L;
        float offsetX = (new Random(baseHash ^ 0xDEADBEEFL).nextFloat() * 2f - 1f) * force;
        float offsetY = (new Random(baseHash ^ 0xCAFEBABEL).nextFloat() * 2f - 1f) * force;
        return new Vec2(offsetX, offsetY);
    }

    private float parseFloat(Map<String, String> params, String key, float defaultValue) {
        String value = params.get(key);
        if (value == null) return defaultValue;
        try {
            return Float.parseFloat(value);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }
}
