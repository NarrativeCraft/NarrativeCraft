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

package fr.loudo.narrativecraft.narrative.npc;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import fr.loudo.narrativecraft.NarrativeCraftMod;
import fr.loudo.narrativecraft.client.editors.widgets.DialogFieldSet;
import fr.loudo.narrativecraft.dialog.DialogDataIO;
import fr.loudo.narrativecraft.files.NarrativeCraftFileEditor;
import fr.loudo.narrativecraft.files.NarrativeCraftFileRegistry;
import fr.loudo.narrativecraft.managers.ChapterManager;
import fr.loudo.narrativecraft.narrative.NarrativeEntryEditor;
import fr.loudo.narrativecraft.narrative.chapter.Chapter;
import fr.loudo.narrativecraft.narrative.scene.Scene;
import fr.loudo.narrativecraft.network.BiSyncNarrativeEntryPacket;
import fr.loudo.narrativecraft.utils.Translation;
import fr.loudo.narrativecraft.utils.Utils;
import fr.loudo.narrativecraft.utils.UtilsServer;
import java.util.UUID;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;

public class NpcEditor implements NarrativeEntryEditor<NpcPayload, Npc> {

    private final ChapterManager chapterManager =
            NarrativeCraftMod.getInstance().getChapterManager();

    @Override
    public Npc resolve(UUID entryId, NpcPayload payload) {
        Chapter chapter = chapterManager.getById(payload.getChapterId());
        if (chapter == null) return null;

        Scene scene = chapter.getSceneManager().getById(payload.getSceneId());
        if (scene == null) return null;

        return scene.getNpcManager().getById(entryId);
    }

    @Override
    public void add(UUID entryId, NpcPayload payload, UUID playerId) {
        Chapter chapter = chapterManager.getById(payload.getChapterId());
        if (chapter == null) return;

        Scene scene = chapter.getSceneManager().getById(payload.getSceneId());
        if (scene == null) return;

        Npc npc = buildFromPayload(entryId, payload, scene);
        int result = NarrativeCraftFileRegistry.getInstance().create(npc);

        if (result == NarrativeCraftFileEditor.OPERATION_FAILED) {
            ServerPlayer player = UtilsServer.getPlayerByUUID(playerId);
            UtilsServer.sendErrorClearScreen(Translation.message("error.crud.add", payload.getName()), player);
            return;
        }

        scene.getNpcManager().add(npc);
        UtilsServer.broadcastPacket(BiSyncNarrativeEntryPacket.add(entryId, payload));
    }

    @Override
    public void edit(UUID entryId, NpcPayload payload, UUID playerId) {
        Npc oldNpc = resolve(entryId, payload);
        if (oldNpc == null) return;

        Npc newNpc = buildFromPayload(entryId, payload, oldNpc.getScene());
        int result = NarrativeCraftFileRegistry.getInstance().edit(newNpc);

        if (result == NarrativeCraftFileEditor.OPERATION_FAILED) {
            ServerPlayer player = UtilsServer.getPlayerByUUID(playerId);
            UtilsServer.sendErrorClearScreen(Translation.message("error.crud.edit", payload.getName()), player);
            return;
        }

        oldNpc.setName(payload.getName());
        if (!payload.getModelType().isEmpty()) {
            oldNpc.setModelType(Utils.parsePlayerModelType(payload.getModelType()));
        }
        oldNpc.setEntityType(resolveEntityType(payload.getEntityTypeId()));
        oldNpc.setDialogData(newNpc.getDialogData());

        UtilsServer.broadcastPacket(BiSyncNarrativeEntryPacket.edit(entryId, payload));
    }

    @Override
    public void delete(UUID entryId, NpcPayload payload, UUID playerId) {
        Npc npc = resolve(entryId, payload);
        if (npc == null) return;

        int result = NarrativeCraftFileRegistry.getInstance().delete(npc);

        if (result == NarrativeCraftFileEditor.OPERATION_FAILED) {
            ServerPlayer player = UtilsServer.getPlayerByUUID(playerId);
            UtilsServer.sendErrorClearScreen(Translation.message("error.crud.delete", payload.getName()), player);
            return;
        }

        npc.getScene().getNpcManager().remove(npc);
        UtilsServer.broadcastPacket(BiSyncNarrativeEntryPacket.delete(entryId, payload));
    }

    private Npc buildFromPayload(UUID entryId, NpcPayload payload, Scene scene) {
        Npc npc = new Npc(entryId, payload.getName(), scene);
        if (!payload.getModelType().isEmpty()) {
            npc.setModelType(Utils.parsePlayerModelType(payload.getModelType()));
        }
        npc.setEntityType(resolveEntityType(payload.getEntityTypeId()));
        String dialogDataJson = payload.getDialogDataJson();
        if (dialogDataJson != null && !dialogDataJson.isEmpty() && !dialogDataJson.equals("{}")) {
            try {
                JsonObject json = JsonParser.parseString(dialogDataJson).getAsJsonObject();
                npc.setDialogData(DialogDataIO.deserialize(json, DialogFieldSet.CHARACTER));
            } catch (Exception ignored) {
            }
        }
        return npc;
    }

    private EntityType<?> resolveEntityType(String entityTypeId) {
        return BuiltInRegistries.ENTITY_TYPE
                .getOptional(Identifier.parse(entityTypeId))
                .orElse(EntityTypes.PLAYER);
    }
}
