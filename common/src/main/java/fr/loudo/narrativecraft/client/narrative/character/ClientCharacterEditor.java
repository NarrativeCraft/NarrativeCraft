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

package fr.loudo.narrativecraft.client.narrative.character;

import fr.loudo.narrativecraft.client.ClientNarrativeCraftMod;
import fr.loudo.narrativecraft.client.narrative.ClientNarrativeEntryEditor;
import fr.loudo.narrativecraft.managers.CharacterManager;
import fr.loudo.narrativecraft.narrative.character.CharacterStory;
import fr.loudo.narrativecraft.narrative.character.CharacterStoryPayload;
import fr.loudo.narrativecraft.utils.UtilsClient;
import java.util.UUID;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.PlayerModelType;

public class ClientCharacterEditor implements ClientNarrativeEntryEditor<CharacterStoryPayload, CharacterStory> {

    private final CharacterManager characterManager =
            ClientNarrativeCraftMod.getInstance().getCharacterManager();

    @Override
    public CharacterStory resolve(UUID entryId, CharacterStoryPayload payload) {
        return characterManager.getById(entryId);
    }

    @Override
    public void add(UUID entryId, CharacterStoryPayload payload) {
        CharacterStory character = buildFromPayload(entryId, payload);
        characterManager.add(character);
        UtilsClient.reloadListScreen();
    }

    @Override
    public void edit(UUID entryId, CharacterStoryPayload payload) {
        CharacterStory oldCharacter = resolve(entryId, payload);
        if (oldCharacter == null) return;

        oldCharacter.setName(payload.getName());
        oldCharacter.setDescription(payload.getDescription());
        oldCharacter.setDialogPresetName(
                payload.getDialogPresetName().isEmpty() ? null : payload.getDialogPresetName());
        if (!payload.getModelType().isEmpty()) {
            oldCharacter.setModelType(PlayerModelType.valueOf(payload.getModelType()));
        }
        oldCharacter.setEntityType(resolveEntityType(payload.getEntityTypeId()));
        UtilsClient.reloadListScreen();
    }

    @Override
    public void delete(UUID entryId, CharacterStoryPayload payload) {
        CharacterStory character = resolve(entryId, payload);
        characterManager.remove(character);
        UtilsClient.reloadListScreen();
    }

    private CharacterStory buildFromPayload(UUID entryId, CharacterStoryPayload payload) {
        CharacterStory character = new CharacterStory(entryId, payload.getName(), payload.getDescription());
        character.setDialogPresetName(payload.getDialogPresetName().isEmpty() ? null : payload.getDialogPresetName());
        if (!payload.getModelType().isEmpty()) {
            character.setModelType(PlayerModelType.valueOf(payload.getModelType()));
        }
        character.setEntityType(resolveEntityType(payload.getEntityTypeId()));
        return character;
    }

    private EntityType<?> resolveEntityType(String entityTypeId) {
        return BuiltInRegistries.ENTITY_TYPE
                .getOptional(Identifier.parse(entityTypeId))
                .orElse(EntityType.PLAYER);
    }
}
