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

package fr.loudo.narrativecraft.utils;

import fr.loudo.narrativecraft.screens.AbstractNarrativeEntryEditScreen;
import fr.loudo.narrativecraft.screens.NarrativeEntryListScreen;
import net.minecraft.client.Minecraft;

public class UtilsClient {

    private static final Minecraft minecraft = Minecraft.getInstance();

    public static void reloadListScreen() {
        // If player was on edit screen and has sent a payload on clicking "Send" button, bring back to last screen
        if (minecraft.screen instanceof AbstractNarrativeEntryEditScreen<?> screen && screen.payloadSent()) {
            minecraft.setScreen(screen.getLastScreen());
        }

        // Reload list for player if a new NarrativeEntry element was added
        if (minecraft.screen instanceof NarrativeEntryListScreen<?> screen) {
            screen.reload();
        }
    }
}
