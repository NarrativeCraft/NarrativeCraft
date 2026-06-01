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

package fr.loudo.narrativecraft.narrative.cutscene;

import fr.loudo.narrativecraft.NarrativeCraftMod;
import fr.loudo.narrativecraft.files.NarrativeCraftFileEditor;
import fr.loudo.narrativecraft.files.NarrativeCraftFileRegistry;
import fr.loudo.narrativecraft.managers.ChapterManager;
import fr.loudo.narrativecraft.narrative.NarrativeEntryEditor;
import fr.loudo.narrativecraft.narrative.animation.Animation;
import fr.loudo.narrativecraft.narrative.chapter.Chapter;
import fr.loudo.narrativecraft.narrative.scene.Scene;
import fr.loudo.narrativecraft.narrative.subscene.Subscene;
import fr.loudo.narrativecraft.network.BiSyncNarrativeEntryPacket;
import fr.loudo.narrativecraft.utils.Translation;
import fr.loudo.narrativecraft.utils.UtilsServer;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import net.minecraft.server.level.ServerPlayer;

public class CutsceneEditor implements NarrativeEntryEditor<CutscenePayload, Cutscene> {

    final ChapterManager chapterManager = NarrativeCraftMod.getInstance().getChapterManager();

    @Override
    public Cutscene resolve(UUID entryId, CutscenePayload payload) {
        Chapter chapter = chapterManager.getById(payload.getChapterId());
        if (chapter == null) return null;

        Scene scene = chapter.getSceneManager().getById(payload.getSceneId());
        if (scene == null) return null;

        return scene.getCutsceneManager().getById(entryId);
    }

    @Override
    public void add(UUID entryId, CutscenePayload payload, UUID playerId) {
        Chapter chapter = chapterManager.getById(payload.getChapterId());
        if (chapter == null) return;

        Scene scene = chapter.getSceneManager().getById(payload.getSceneId());
        if (scene == null) return;

        Cutscene cutscene = new Cutscene(entryId, payload.getName(), payload.getDescription(), scene);
        int result = NarrativeCraftFileRegistry.getInstance().create(cutscene);

        if (result == NarrativeCraftFileEditor.OPERATION_FAILED) {
            ServerPlayer player = UtilsServer.getPlayerByUUID(playerId);
            UtilsServer.sendErrorClearScreen(Translation.message("error.crud.add", payload.getName()), player);
            return;
        }

        scene.getCutsceneManager().add(cutscene);
        UtilsServer.broadcastPacket(BiSyncNarrativeEntryPacket.add(entryId, payload));
    }

    @Override
    public void edit(UUID entryId, CutscenePayload payload, UUID playerId) {
        Cutscene oldCutscene = resolve(entryId, payload);
        if (oldCutscene == null) return;

        List<Animation> animations = payload.getAnimationIds().stream()
                .map(id -> oldCutscene.getScene().getAnimationManager().getById(id))
                .filter(a -> a != null)
                .collect(Collectors.toList());

        List<Subscene> subscenes = payload.getSubsceneIds().stream()
                .map(id -> oldCutscene.getScene().getSubsceneManager().getById(id))
                .filter(s -> s != null)
                .collect(Collectors.toList());

        Cutscene newCutscene = new Cutscene(
                entryId, payload.getName(), payload.getDescription(), oldCutscene.getScene(), animations, subscenes);
        int result = NarrativeCraftFileRegistry.getInstance().edit(newCutscene);

        if (result == NarrativeCraftFileEditor.OPERATION_FAILED) {
            ServerPlayer player = UtilsServer.getPlayerByUUID(playerId);
            UtilsServer.sendErrorClearScreen(Translation.message("error.crud.edit", payload.getName()), player);
            return;
        }

        oldCutscene.setName(payload.getName());
        oldCutscene.setDescription(payload.getDescription());
        oldCutscene.setAnimations(animations);
        oldCutscene.setSubscenes(subscenes);

        UtilsServer.broadcastPacket(BiSyncNarrativeEntryPacket.edit(entryId, payload));
    }

    @Override
    public void delete(UUID entryId, CutscenePayload payload, UUID playerId) {
        Cutscene cutscene = resolve(entryId, payload);
        if (cutscene == null) return;

        int result = NarrativeCraftFileRegistry.getInstance().delete(cutscene);

        if (result == NarrativeCraftFileEditor.OPERATION_FAILED) {
            ServerPlayer player = UtilsServer.getPlayerByUUID(playerId);
            UtilsServer.sendErrorClearScreen(Translation.message("error.crud.delete", payload.getName()), player);
            return;
        }

        cutscene.getScene().getCutsceneManager().remove(cutscene);
        UtilsServer.broadcastPacket(BiSyncNarrativeEntryPacket.delete(entryId, payload));
    }
}
