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

package fr.loudo.narrativecraft.narrative.interaction;

import fr.loudo.narrativecraft.NarrativeCraftMod;
import fr.loudo.narrativecraft.files.NarrativeCraftFileEditor;
import fr.loudo.narrativecraft.files.NarrativeCraftFileRegistry;
import fr.loudo.narrativecraft.managers.ChapterManager;
import fr.loudo.narrativecraft.narrative.NarrativeEntryEditor;
import fr.loudo.narrativecraft.narrative.chapter.Chapter;
import fr.loudo.narrativecraft.narrative.scene.Scene;
import fr.loudo.narrativecraft.network.BiSyncNarrativeEntryPacket;
import fr.loudo.narrativecraft.utils.Translation;
import fr.loudo.narrativecraft.utils.UtilsServer;
import java.util.UUID;
import net.minecraft.server.level.ServerPlayer;

public class InteractionEditor implements NarrativeEntryEditor<InteractionPayload, Interaction> {

    final ChapterManager chapterManager = NarrativeCraftMod.getInstance().getChapterManager();

    @Override
    public Interaction resolve(UUID entryId, InteractionPayload payload) {
        Chapter chapter = chapterManager.getById(payload.getChapterId());
        if (chapter == null) return null;

        Scene scene = chapter.getSceneManager().getById(payload.getSceneId());
        if (scene == null) return null;

        return scene.getInteractionManager().getById(entryId);
    }

    @Override
    public void add(UUID entryId, InteractionPayload payload, UUID playerId) {
        Chapter chapter = chapterManager.getById(payload.getChapterId());
        if (chapter == null) return;

        Scene scene = chapter.getSceneManager().getById(payload.getSceneId());
        if (scene == null) return;

        Interaction interaction = new Interaction(entryId, payload.getName(), payload.getDescription(), scene);
        int result = NarrativeCraftFileRegistry.getInstance().create(interaction);

        if (result == NarrativeCraftFileEditor.OPERATION_FAILED) {
            ServerPlayer player = UtilsServer.getPlayerByUUID(playerId);
            UtilsServer.sendErrorClearScreen(Translation.message("error.crud.add", payload.getName()), player);
            return;
        }

        scene.getInteractionManager().add(interaction);
        UtilsServer.broadcastPacket(BiSyncNarrativeEntryPacket.add(entryId, payload));
    }

    @Override
    public void edit(UUID entryId, InteractionPayload payload, UUID playerId) {
        Interaction interaction = resolve(entryId, payload);
        if (interaction == null) return;

        String oldName = interaction.getName();
        String oldDescription = interaction.getDescription();

        interaction.setName(payload.getName());
        interaction.setDescription(payload.getDescription());

        int result = NarrativeCraftFileRegistry.getInstance().edit(interaction);

        if (result == NarrativeCraftFileEditor.OPERATION_FAILED) {
            interaction.setName(oldName);
            interaction.setDescription(oldDescription);
            ServerPlayer player = UtilsServer.getPlayerByUUID(playerId);
            UtilsServer.sendErrorClearScreen(Translation.message("error.crud.edit", payload.getName()), player);
            return;
        }

        UtilsServer.broadcastPacket(BiSyncNarrativeEntryPacket.edit(entryId, payload));
    }

    @Override
    public void delete(UUID entryId, InteractionPayload payload, UUID playerId) {
        Interaction interaction = resolve(entryId, payload);
        if (interaction == null) return;

        int result = NarrativeCraftFileRegistry.getInstance().delete(interaction);

        if (result == NarrativeCraftFileEditor.OPERATION_FAILED) {
            ServerPlayer player = UtilsServer.getPlayerByUUID(playerId);
            UtilsServer.sendErrorClearScreen(Translation.message("error.crud.delete", payload.getName()), player);
            return;
        }

        interaction.getScene().getInteractionManager().remove(interaction);
        UtilsServer.broadcastPacket(BiSyncNarrativeEntryPacket.delete(entryId, payload));
    }
}
