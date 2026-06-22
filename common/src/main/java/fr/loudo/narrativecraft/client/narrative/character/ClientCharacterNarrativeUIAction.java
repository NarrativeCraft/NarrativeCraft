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

package fr.loudo.narrativecraft.client.narrative.character;

import fr.loudo.narrativecraft.client.narrative.ui.ClientNarrativeUIAction;
import fr.loudo.narrativecraft.client.screens.AbstractNarrativeEntryEditScreen;
import fr.loudo.narrativecraft.client.screens.narrative.character.CharacterEntryEditScreen;
import fr.loudo.narrativecraft.narrative.character.CharacterStory;
import net.minecraft.client.gui.screens.Screen;

public class ClientCharacterNarrativeUIAction implements ClientNarrativeUIAction<CharacterStory> {

    @Override
    public Screen subListSubScreen(CharacterStory entry, Screen parent) {
        return null;
    }

    @Override
    public boolean isClickable() {
        return false;
    }

    @Override
    public void customClickAction(CharacterStory entry) {}

    @Override
    public AbstractNarrativeEntryEditScreen<CharacterStory> showEditScreen(CharacterStory entry, Screen lastScreen) {
        return entry == null
                ? new CharacterEntryEditScreen(lastScreen)
                : new CharacterEntryEditScreen(entry, lastScreen);
    }
}
