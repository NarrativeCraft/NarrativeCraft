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

package fr.loudo.narrativecraft.api.managers;

import fr.loudo.narrativecraft.api.narrative.character.ICharacter;
import fr.loudo.narrativecraft.api.narrative.scene.IScene;

import javax.annotation.Nullable;
import java.util.List;
import java.util.UUID;

public interface ICharacterManager extends INarrativeManager<ICharacter> {

    List<? extends ICharacter> getSortedList();

    ICharacter getMainCharacter();

    /**
     * Retrieve a global character or a npc if you have a scene by his id.
     * @param characterId id of the character
     * @param scene scene of the npc if it is set
     * @return instance of {@link ICharacter}
     */
    ICharacter resolveCharacter(UUID characterId, @Nullable IScene scene);

    /**
     * Retrieve a global character or a npc if you have a scene by his name.
     * @param characterName name of the character
     * @param scene scene of the npc if it is set
     * @return instance of {@link ICharacter}
     */
    ICharacter resolveCharacter(String characterName, @Nullable IScene scene);
}
