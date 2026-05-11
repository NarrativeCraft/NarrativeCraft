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

package fr.loudo.narrativecraft.managers;

import fr.loudo.narrativecraft.narrative.NarrativeManager;
import fr.loudo.narrativecraft.narrative.character.CharacterStory;
import fr.loudo.narrativecraft.narrative.character.ICharacterStory;
import fr.loudo.narrativecraft.narrative.scene.Scene;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import javax.annotation.Nullable;

public class CharacterManager extends NarrativeManager<CharacterStory> {

    @Override
    public List<CharacterStory> getList() {
        List<CharacterStory> sorted = new ArrayList<>(list);
        sorted.sort(Comparator.comparingInt(c -> c.getMainCharacterAttribute().isMainCharacter() ? 0 : 1));
        return sorted;
    }

    /**
     * Retrieve a global character or a npc if you have a scene by his id.
     * @param characterId id of the character
     * @param scene scene of the npc if it is set
     * @return instance of {@link ICharacterStory}
     */
    public ICharacterStory resolveCharacter(UUID characterId, @Nullable Scene scene) {
        ICharacterStory characterStory = getById(characterId);
        if (characterStory == null && scene != null) {
            return scene.getNpcManager().getById(characterId);
        }
        return characterStory;
    }
}
