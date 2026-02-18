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

import fr.loudo.narrativecraft.client.NarrativeCraftClientMod;
import fr.loudo.narrativecraft.narrative.chapter.Chapter;
import fr.loudo.narrativecraft.screens.PaginationsItemsScreen;
import fr.loudo.narrativecraft.screens.components.Paginationitem;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

public class PressKeyListener {

    private static final Map<KeyMapping, Boolean> PREVIOUS_STATES_KEY_MAPPING = new HashMap<>();

    public static void onKeyPressed(Minecraft minecraft) {
        handleKeyPress(ModKeys.STORY_MANAGER, () -> {
            List<Paginationitem> items = new ArrayList<>();
            for (Chapter chapter :
                    NarrativeCraftClientMod.getInstance().getChapterManager().getList()) {
                items.add(new Paginationitem(chapter.getName(), null));
            }
            PaginationsItemsScreen screen = new PaginationsItemsScreen(Component.literal("Chapters"), items);
            minecraft.setScreen(screen);
        });
    }

    public static void handleKeyPress(KeyMapping key, Runnable action) {
        boolean isDown = key.isDown();
        boolean wasDown = PREVIOUS_STATES_KEY_MAPPING.getOrDefault(key, false);

        if (isDown && !wasDown) {
            action.run();
        }

        PREVIOUS_STATES_KEY_MAPPING.put(key, isDown);
    }
}
