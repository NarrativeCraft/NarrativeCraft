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

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import fr.loudo.narrativecraft.client.ClientNarrativeCraftMod;
import fr.loudo.narrativecraft.client.narrative.ClientNarrativeEntryEditor;
import fr.loudo.narrativecraft.dialog.DialogDataIO;
import fr.loudo.narrativecraft.managers.ChapterManager;
import fr.loudo.narrativecraft.narrative.chapter.Chapter;
import fr.loudo.narrativecraft.narrative.npc.Npc;
import fr.loudo.narrativecraft.narrative.npc.NpcPayload;
import fr.loudo.narrativecraft.narrative.scene.Scene;
import fr.loudo.narrativecraft.utils.UtilsClient;
import java.util.UUID;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.PlayerModelType;

public class ClientNpcEditor implements ClientNarrativeEntryEditor<NpcPayload, Npc> {

    private final ChapterManager chapterManager =
            ClientNarrativeCraftMod.getInstance().getChapterManager();

    @Override
    public void add(UUID entryId, NpcPayload payload) {
        Chapter chapter = chapterManager.getById(payload.getChapterId());
        if (chapter == null) return;

        Scene scene = chapter.getSceneManager().getById(payload.getSceneId());
        if (scene == null) return;

        Npc npc = buildFromPayload(entryId, payload, scene);
        scene.getNpcManager().add(npc);

        UtilsClient.reloadListScreen();
    }

    @Override
    public void edit(UUID entryId, NpcPayload payload) {
        Npc npc = resolve(entryId, payload);
        if (npc == null) return;

        npc.setName(payload.getName());
        if (!payload.getModelType().isEmpty()) {
            npc.setModelType(PlayerModelType.valueOf(payload.getModelType()));
        }
        npc.setEntityType(resolveEntityType(payload.getEntityTypeId()));
        String dialogDataJson = payload.getDialogDataJson();
        if (dialogDataJson != null && !dialogDataJson.isEmpty() && !dialogDataJson.equals("{}")) {
            try {
                JsonObject json = JsonParser.parseString(dialogDataJson).getAsJsonObject();
                npc.setDialogData(DialogDataIO.deserialize(json));
            } catch (Exception ignored) {
            }
        }

        UtilsClient.reloadListScreen();
    }

    @Override
    public void delete(UUID entryId, NpcPayload payload) {
        Chapter chapter = chapterManager.getById(payload.getChapterId());
        if (chapter == null) return;

        Scene scene = chapter.getSceneManager().getById(payload.getSceneId());
        if (scene == null) return;

        Npc npc = scene.getNpcManager().getById(entryId);
        if (npc == null) return;

        scene.getNpcManager().remove(npc);

        UtilsClient.reloadListScreen();
    }

    @Override
    public Npc resolve(UUID entryId, NpcPayload payload) {
        Chapter chapter = chapterManager.getById(payload.getChapterId());
        if (chapter == null) return null;

        Scene scene = chapter.getSceneManager().getById(payload.getSceneId());
        if (scene == null) return null;

        return scene.getNpcManager().getById(entryId);
    }

    private Npc buildFromPayload(UUID entryId, NpcPayload payload, Scene scene) {
        Npc npc = new Npc(entryId, payload.getName(), scene);
        if (!payload.getModelType().isEmpty()) {
            npc.setModelType(PlayerModelType.valueOf(payload.getModelType()));
        }
        npc.setEntityType(resolveEntityType(payload.getEntityTypeId()));
        String dialogDataJson = payload.getDialogDataJson();
        if (dialogDataJson != null && !dialogDataJson.isEmpty() && !dialogDataJson.equals("{}")) {
            try {
                JsonObject json = JsonParser.parseString(dialogDataJson).getAsJsonObject();
                npc.setDialogData(DialogDataIO.deserialize(json));
            } catch (Exception ignored) {
            }
        }
        return npc;
    }

    private EntityType<?> resolveEntityType(String entityTypeId) {
        return BuiltInRegistries.ENTITY_TYPE
                .getOptional(Identifier.parse(entityTypeId))
                .orElse(EntityType.PLAYER);
    }
}
