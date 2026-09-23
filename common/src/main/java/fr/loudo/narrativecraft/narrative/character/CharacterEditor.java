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

package fr.loudo.narrativecraft.narrative.character;

import fr.loudo.narrativecraft.NarrativeCraftMod;
import fr.loudo.narrativecraft.files.NarrativeCraftFileRegistry;
import fr.loudo.narrativecraft.managers.CharacterManager;
import fr.loudo.narrativecraft.narrative.AbstractNarrativeEntryEditor;
import fr.loudo.narrativecraft.narrative.NarrativeManager;
import fr.loudo.narrativecraft.narrative.OperationResult;
import fr.loudo.narrativecraft.network.BiSyncNarrativeEntryPacket;
import fr.loudo.narrativecraft.utils.UtilsServer;
import java.util.UUID;

public class CharacterEditor extends AbstractNarrativeEntryEditor<CharacterStoryPayload, CharacterStory> {

    private final CharacterManager characterManager =
            NarrativeCraftMod.getInstance().getCharacterManager();

    @Override
    protected String getTypeKey() {
        return "character";
    }

    @Override
    protected NarrativeManager<CharacterStory> getSiblings(CharacterStoryPayload payload) {
        return characterManager;
    }

    @Override
    protected CharacterStory build(UUID entryId, CharacterStoryPayload payload, CharacterStory existing) {
        return CharacterStory.fromPayload(entryId, payload);
    }

    @Override
    protected void copyAttributes(CharacterStory target, CharacterStory source) {
        target.copyAttributesFrom(source);
    }

    @Override
    public OperationResult add(UUID entryId, CharacterStoryPayload payload) {
        OperationResult result = super.add(entryId, payload);
        CharacterStory character = characterManager.getById(entryId);
        if (result.isSuccess() && character.isMainCharacter()) {
            demoteOtherMainCharacters(character);
        }
        return result;
    }

    @Override
    public OperationResult edit(UUID entryId, CharacterStoryPayload payload) {
        CharacterStory existing = characterManager.getById(entryId);
        boolean wasMainCharacter = existing != null && existing.isMainCharacter();
        OperationResult result = super.edit(entryId, payload);
        if (result.isSuccess() && existing.isMainCharacter() && !wasMainCharacter) {
            demoteOtherMainCharacters(existing);
        }
        return result;
    }

    private void demoteOtherMainCharacters(CharacterStory mainCharacter) {
        for (CharacterStory character : characterManager.getList()) {
            if (character == mainCharacter || !character.isMainCharacter()) continue;

            CharacterStory demoted = new CharacterStory(character.getId(), character.getName());
            demoted.copyAttributesFrom(character);
            MainCharacterAttribute attribute = new MainCharacterAttribute(character.getMainCharacterAttribute());
            attribute.setMainCharacter(false);
            demoted.setMainCharacterAttribute(attribute);

            OperationResult storage = NarrativeCraftFileRegistry.getInstance().edit(character, demoted);
            if (storage.isFailure()) {
                NarrativeCraftMod.LOGGER.warn("Failed to demote previous main character {}", character.getName());
                continue;
            }
            character.copyAttributesFrom(demoted);
            UtilsServer.broadcastPacket(BiSyncNarrativeEntryPacket.edit(character.getId(), character.toPayload()));
        }
    }
}
