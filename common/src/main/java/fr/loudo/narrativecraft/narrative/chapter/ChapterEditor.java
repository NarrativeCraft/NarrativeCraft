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

package fr.loudo.narrativecraft.narrative.chapter;

import fr.loudo.narrativecraft.NarrativeCraftMod;
import fr.loudo.narrativecraft.files.NarrativeCraftFileEditor;
import fr.loudo.narrativecraft.files.NarrativeCraftFileRegistry;
import fr.loudo.narrativecraft.managers.ChapterManager;
import fr.loudo.narrativecraft.narrative.NarrativeEntryEditor;
import fr.loudo.narrativecraft.network.BiSyncNarrativeEntryPacket;
import fr.loudo.narrativecraft.utils.Translation;
import fr.loudo.narrativecraft.utils.UtilsServer;
import java.util.UUID;
import net.minecraft.server.level.ServerPlayer;

public class ChapterEditor implements NarrativeEntryEditor<ChapterPayload, Chapter> {

    final ChapterManager chapterManager = NarrativeCraftMod.getInstance().getChapterManager();

    @Override
    public Chapter resolve(UUID entryId, ChapterPayload payload) {
        return chapterManager.getById(entryId);
    }

    @Override
    public void add(UUID entryId, ChapterPayload payload, UUID playerId) {
        Chapter chapter = new Chapter(entryId, payload.getName(), payload.getDescription(), payload.getChapterIndex());

        ServerPlayer player = UtilsServer.getPlayerByUUID(playerId);

        int result = NarrativeCraftFileRegistry.getInstance().create(chapter);
        if (result == NarrativeCraftFileEditor.OPERATION_SUCCESS) {
            chapterManager.add(chapter);
            UtilsServer.broadcastPacket(BiSyncNarrativeEntryPacket.add(entryId, payload));
        } else {
            UtilsServer.sendErrorClearScreen(Translation.message("error.crud.add", payload.getName()), player);
        }
    }

    @Override
    public void edit(UUID entryId, ChapterPayload payload, UUID playerId) {

        Chapter oldChapter = resolve(entryId, payload);

        if (oldChapter == null) {
            return;
        }

        Chapter newChapter =
                new Chapter(entryId, payload.getName(), payload.getDescription(), payload.getChapterIndex());
        int result = NarrativeCraftFileRegistry.getInstance().edit(newChapter);

        if (result == NarrativeCraftFileEditor.OPERATION_FAILED) {
            ServerPlayer player = UtilsServer.getPlayerByUUID(playerId);
            UtilsServer.sendErrorClearScreen(Translation.message("error.crud.edit", payload.getName()), player);
            return;
        }

        int oldIndex = oldChapter.getChapterIndex();
        oldChapter.setName(payload.getName());
        oldChapter.setDescription(payload.getDescription());
        oldChapter.setChapterIndex(payload.getChapterIndex());
        if (oldIndex != payload.getChapterIndex()) {
            chapterManager.forceSort();
        }

        UtilsServer.broadcastPacket(BiSyncNarrativeEntryPacket.edit(entryId, payload));
    }

    @Override
    public void delete(UUID entryId, ChapterPayload payload, UUID playerId) {
        Chapter chapter = resolve(entryId, payload);

        ServerPlayer player = UtilsServer.getPlayerByUUID(playerId);

        int result = NarrativeCraftFileRegistry.getInstance().delete(chapter);
        if (result == NarrativeCraftFileEditor.OPERATION_SUCCESS) {
            chapterManager.remove(chapter);
            UtilsServer.broadcastPacket(BiSyncNarrativeEntryPacket.delete(entryId, payload));
        } else {
            UtilsServer.sendErrorClearScreen(Translation.message("error.crud.delete", payload.getName()), player);
        }
    }
}
