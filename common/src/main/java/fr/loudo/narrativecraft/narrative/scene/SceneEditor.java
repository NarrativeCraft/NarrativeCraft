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

package fr.loudo.narrativecraft.narrative.scene;

import fr.loudo.narrativecraft.NarrativeCraftMod;
import fr.loudo.narrativecraft.files.NarrativeCraftFileEditor;
import fr.loudo.narrativecraft.files.NarrativeCraftFileRegistry;
import fr.loudo.narrativecraft.managers.ChapterManager;
import fr.loudo.narrativecraft.narrative.NarrativeEntryEditor;
import fr.loudo.narrativecraft.narrative.chapter.Chapter;
import fr.loudo.narrativecraft.network.BiSyncNarrativeEntryPacket;
import fr.loudo.narrativecraft.utils.Translation;
import fr.loudo.narrativecraft.utils.UtilsServer;
import java.util.UUID;
import net.minecraft.server.level.ServerPlayer;

public class SceneEditor implements NarrativeEntryEditor<ScenePayload, Scene> {

    final ChapterManager chapterManager = NarrativeCraftMod.getInstance().getChapterManager();

    @Override
    public Scene resolve(UUID entryId, ScenePayload payload) {
        Chapter chapter = chapterManager.getById(payload.getChapterId());
        if (chapter == null) return null;

        return chapter.getSceneManager().getById(entryId);
    }

    @Override
    public void add(UUID entryId, ScenePayload payload, UUID playerId) {

        Chapter chapter = chapterManager.getById(payload.getChapterId());
        if (chapter == null) return;

        Scene scene = new Scene(entryId, payload.getName(), payload.getDescription(), chapter, payload.getRank());
        int result = NarrativeCraftFileRegistry.getInstance().create(scene);

        if (result == NarrativeCraftFileEditor.OPERATION_FAILED) {
            ServerPlayer player = UtilsServer.getPlayerByUUID(playerId);
            UtilsServer.sendErrorClearScreen(Translation.message("error.crud.add", payload.getName()), player);
            return;
        }

        scene.getChapter().getSceneManager().add(scene);
        scene.setRank(chapter.getSceneManager().size());

        UtilsServer.broadcastPacket(BiSyncNarrativeEntryPacket.add(entryId, payload));
    }

    @Override
    public void edit(UUID entryId, ScenePayload payload, UUID playerId) {

        Scene oldScene = resolve(entryId, payload);
        if (oldScene == null) {
            return;
        }

        Scene newScene = new Scene(
                entryId, payload.getName(), payload.getDescription(), oldScene.getChapter(), payload.getRank());
        int result = NarrativeCraftFileRegistry.getInstance().edit(newScene);
        if (result == NarrativeCraftFileEditor.OPERATION_FAILED) {
            ServerPlayer player = UtilsServer.getPlayerByUUID(playerId);
            UtilsServer.sendErrorClearScreen(Translation.message("error.crud.edit", payload.getName()), player);
            return;
        }

        int oldRank = oldScene.getRank();
        oldScene.setName(payload.getName());
        oldScene.setDescription(payload.getDescription());
        oldScene.setRank(payload.getRank());
        if (oldRank != payload.getRank()) {
            oldScene.getChapter().getSceneManager().forceSort();
        }

        UtilsServer.broadcastPacket(BiSyncNarrativeEntryPacket.edit(entryId, payload));
    }

    @Override
    public void delete(UUID entryId, ScenePayload payload, UUID playerId) {

        Scene scene = resolve(entryId, payload);
        if (scene == null) return;

        int result = NarrativeCraftFileRegistry.getInstance().delete(scene);
        if (result == NarrativeCraftFileEditor.OPERATION_FAILED) {
            ServerPlayer player = UtilsServer.getPlayerByUUID(playerId);
            UtilsServer.sendErrorClearScreen(Translation.message("error.crud.delete", payload.getName()), player);
            return;
        }

        scene.getChapter().getSceneManager().remove(scene);
        UtilsServer.broadcastPacket(BiSyncNarrativeEntryPacket.delete(entryId, payload));
    }
}
