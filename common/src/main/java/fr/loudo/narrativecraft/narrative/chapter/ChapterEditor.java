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

package fr.loudo.narrativecraft.narrative.chapter;

import fr.loudo.narrativecraft.NarrativeCraftMod;
import fr.loudo.narrativecraft.file.NarrativeCraftFileEditor;
import fr.loudo.narrativecraft.file.NarrativeCraftFileRegistry;
import fr.loudo.narrativecraft.managers.ChapterManager;
import fr.loudo.narrativecraft.narrative.NarrativeEntryEditor;
import fr.loudo.narrativecraft.network.BiSyncNarrativeEntryPacket;
import fr.loudo.narrativecraft.network.NarrativeEntryAction;
import fr.loudo.narrativecraft.platform.Services;
import fr.loudo.narrativecraft.utils.UtilsServer;
import java.util.UUID;
import net.minecraft.server.level.ServerPlayer;

public class ChapterEditor implements NarrativeEntryEditor<ChapterPayload, Chapter> {

    final ChapterManager chapterManager = NarrativeCraftMod.getInstance().getChapterManager();

    @Override
    public Chapter resolve(ChapterPayload payload) {
        return chapterManager.getById(payload.getId());
    }

    @Override
    public void add(ChapterPayload payload, UUID playerId) {
        Chapter chapter =
                new Chapter(payload.getId(), payload.getName(), payload.getDescription(), payload.getChapterIndex());

        ServerPlayer player = UtilsServer.getPlayerByUUID(playerId);

        int result = NarrativeCraftFileRegistry.getInstance().create(chapter);
        if (result == NarrativeCraftFileEditor.OPERATION_SUCCESS) {
            chapterManager.add(chapter);
        } else {
            if (player == null) return;
            Services.PACKET.sendToPlayer(player, new BiSyncNarrativeEntryPacket(payload, NarrativeEntryAction.DELETE));
        }
    }

    @Override
    public void edit(ChapterPayload payload, UUID playerId) {}

    @Override
    public void delete(ChapterPayload payload, UUID playerId) {
        Chapter chapter = resolve(payload);

        ServerPlayer player = UtilsServer.getPlayerByUUID(playerId);

        int result = NarrativeCraftFileRegistry.getInstance().delete(chapter);
        if (result == NarrativeCraftFileEditor.OPERATION_SUCCESS) {
            chapterManager.remove(chapter);
        } else {
            if (player == null) return;
            Services.PACKET.sendToPlayer(player, new BiSyncNarrativeEntryPacket(payload, NarrativeEntryAction.ADD));
        }
    }
}
