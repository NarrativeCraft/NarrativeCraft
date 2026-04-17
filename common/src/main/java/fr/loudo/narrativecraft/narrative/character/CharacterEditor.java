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

package fr.loudo.narrativecraft.narrative.character;

import fr.loudo.narrativecraft.NarrativeCraftMod;
import fr.loudo.narrativecraft.files.NarrativeCraftFileEditor;
import fr.loudo.narrativecraft.files.NarrativeCraftFileRegistry;
import fr.loudo.narrativecraft.managers.CharacterManager;
import fr.loudo.narrativecraft.narrative.NarrativeEntryEditor;
import fr.loudo.narrativecraft.network.BiSyncNarrativeEntryPacket;
import fr.loudo.narrativecraft.utils.Translation;
import fr.loudo.narrativecraft.utils.UtilsServer;
import java.util.UUID;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.PlayerModelType;

public class CharacterEditor implements NarrativeEntryEditor<CharacterStoryPayload, CharacterStory> {

    private final CharacterManager characterManager =
            NarrativeCraftMod.getInstance().getCharacterManager();

    @Override
    public CharacterStory resolve(UUID entryId, CharacterStoryPayload payload) {
        return characterManager.getById(entryId);
    }

    @Override
    public void add(UUID entryId, CharacterStoryPayload payload, UUID playerId) {
        CharacterStory character = buildFromPayload(entryId, payload);

        ServerPlayer player = UtilsServer.getPlayerByUUID(playerId);

        int result = NarrativeCraftFileRegistry.getInstance().create(character);
        if (result == NarrativeCraftFileEditor.OPERATION_SUCCESS) {
            characterManager.add(character);
            UtilsServer.broadcastPacket(BiSyncNarrativeEntryPacket.add(entryId, payload));
        } else {
            UtilsServer.sendErrorClearScreen(Translation.message("error.crud.add", payload.getName()), player);
        }
    }

    @Override
    public void edit(UUID entryId, CharacterStoryPayload payload, UUID playerId) {
        CharacterStory oldCharacter = resolve(entryId, payload);
        if (oldCharacter == null) return;

        CharacterStory newCharacter = buildFromPayload(entryId, payload);
        int result = NarrativeCraftFileRegistry.getInstance().edit(newCharacter);

        if (result == NarrativeCraftFileEditor.OPERATION_FAILED) {
            ServerPlayer player = UtilsServer.getPlayerByUUID(playerId);
            UtilsServer.sendErrorClearScreen(Translation.message("error.crud.edit", payload.getName()), player);
            return;
        }

        oldCharacter.setName(payload.getName());
        oldCharacter.setDescription(payload.getDescription());
        oldCharacter.setDialogPresetName(
                payload.getDialogPresetName().isEmpty() ? null : payload.getDialogPresetName());
        if (!payload.getModelType().isEmpty()) {
            oldCharacter.setModelType(PlayerModelType.valueOf(payload.getModelType()));
        }
        oldCharacter.setEntityType(resolveEntityType(payload.getEntityTypeId()));

        UtilsServer.broadcastPacket(BiSyncNarrativeEntryPacket.edit(entryId, payload));
    }

    @Override
    public void delete(UUID entryId, CharacterStoryPayload payload, UUID playerId) {
        CharacterStory character = resolve(entryId, payload);

        ServerPlayer player = UtilsServer.getPlayerByUUID(playerId);

        int result = NarrativeCraftFileRegistry.getInstance().delete(character);
        if (result == NarrativeCraftFileEditor.OPERATION_SUCCESS) {
            characterManager.remove(character);
            UtilsServer.broadcastPacket(BiSyncNarrativeEntryPacket.delete(entryId, payload));
        } else {
            UtilsServer.sendErrorClearScreen(Translation.message("error.crud.delete", payload.getName()), player);
        }
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
