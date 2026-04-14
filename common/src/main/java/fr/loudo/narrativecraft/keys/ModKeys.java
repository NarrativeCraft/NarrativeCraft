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

package fr.loudo.narrativecraft.keys;

import com.mojang.blaze3d.platform.InputConstants;
import fr.loudo.narrativecraft.NarrativeCraftMod;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.KeyMapping;
import net.minecraft.resources.Identifier;

public class ModKeys {

    private static final KeyMapping.Category CATEGORY =
            new KeyMapping.Category(Identifier.fromNamespaceAndPath(NarrativeCraftMod.MOD_ID, "main"));

    public static final List<KeyMapping> ALL_KEYS = new ArrayList<>();
    public static final KeyMapping STORY_MANAGER =
            registerKey("narrativecraft.key.story_manager", InputConstants.KEY_N);
    public static final KeyMapping HIDE_CUTSCENE_MAKER_HUD =
            registerKey("narrativecraft.key.hide_cutscene_maker_hud", InputConstants.KEY_H);
    public static final KeyMapping TOGGLE_CAMERA_ROLL =
            registerKey("narrativecraft.key.toggle_camera_roll", InputConstants.KEY_R);

    private static KeyMapping registerKey(String translationKey, int code) {
        KeyMapping key = new KeyMapping(translationKey, InputConstants.Type.KEYSYM, code, CATEGORY);
        ALL_KEYS.add(key);
        return key;
    }
}
