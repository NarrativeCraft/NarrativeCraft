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

package fr.loudo.narrativecraft.network.handlers;

import fr.loudo.narrativecraft.NarrativeCraftMod;
import fr.loudo.narrativecraft.editors.cutscene.CutsceneMakerEditor;
import fr.loudo.narrativecraft.managers.PlayerSessionManager;
import fr.loudo.narrativecraft.narrative.NarrativeEntryEditorRegistry;
import fr.loudo.narrativecraft.narrative.chapter.Chapter;
import fr.loudo.narrativecraft.narrative.cutscene.Cutscene;
import fr.loudo.narrativecraft.narrative.scene.Scene;
import fr.loudo.narrativecraft.network.BiSyncNarrativeEntryPacket;
import fr.loudo.narrativecraft.network.cutscene.C2SCutsceneState;
import fr.loudo.narrativecraft.session.PlayerSession;
import net.minecraft.world.entity.player.Player;

public class ServerPacketHandler {

    public static void narrativeEntry(BiSyncNarrativeEntryPacket packet, Player player) {
        switch (packet.action()) {
            case ADD ->
                NarrativeEntryEditorRegistry.getInstance().add(packet.entryId(), packet.entry(), player.getUUID());
            case EDIT ->
                NarrativeEntryEditorRegistry.getInstance().edit(packet.entryId(), packet.entry(), player.getUUID());
            case DELETE ->
                NarrativeEntryEditorRegistry.getInstance().delete(packet.entryId(), packet.entry(), player.getUUID());
        }
    }

    public static void cutsceneState(C2SCutsceneState packet, Player player) {
        PlayerSessionManager sessionManager = NarrativeCraftMod.getInstance().getPlayerSessionManager();
        PlayerSession session = sessionManager.getByPlayer(player);
        Chapter chapter = NarrativeCraftMod.getInstance().getChapterManager().getById(packet.getChapterId());
        if (chapter == null) return;
        Scene scene = chapter.getSceneManager().getById(packet.getSceneId());
        if (scene == null) return;
        Cutscene cutscene = scene.getCutsceneManager().getById(packet.getCutsceneId());
        if (cutscene == null) return;

        switch (packet.getState()) {
            case ENTER -> {
                CutsceneMakerEditor editor = new CutsceneMakerEditor(cutscene, session);
                session.setEditor(editor);
                editor.init();
            }
            case QUIT -> {
                CutsceneMakerEditor editor = sessionManager.getEditor(player, CutsceneMakerEditor.class);
                if (editor != null) {
                    editor.stop();
                    session.setEditor(null);
                }
            }
        }
    }
}
