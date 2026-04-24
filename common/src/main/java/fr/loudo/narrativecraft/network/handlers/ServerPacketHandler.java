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
import fr.loudo.narrativecraft.editors.Editor;
import fr.loudo.narrativecraft.editors.cameraangle.CameraAngleMakerEditor;
import fr.loudo.narrativecraft.editors.cutscene.CutsceneMakerEditor;
import fr.loudo.narrativecraft.files.NarrativeCraftFileEditor;
import fr.loudo.narrativecraft.files.NarrativeCraftFileRegistry;
import fr.loudo.narrativecraft.managers.PlayerSessionManager;
import fr.loudo.narrativecraft.narrative.NarrativeEntryEditorRegistry;
import fr.loudo.narrativecraft.narrative.cameraangle.CameraAngle;
import fr.loudo.narrativecraft.narrative.cameraangle.CameraAngleDeserializer;
import fr.loudo.narrativecraft.narrative.cameraangle.CameraAngleSerializer;
import fr.loudo.narrativecraft.narrative.cameraangle.CharacterPlacement;
import fr.loudo.narrativecraft.narrative.cameraangle.TemplateReference;
import fr.loudo.narrativecraft.narrative.cameraangle.TemplateSourceType;
import fr.loudo.narrativecraft.narrative.chapter.Chapter;
import fr.loudo.narrativecraft.narrative.character.ICharacterStory;
import fr.loudo.narrativecraft.narrative.cutscene.Cutscene;
import fr.loudo.narrativecraft.narrative.cutscene.CutsceneDeserializer;
import fr.loudo.narrativecraft.narrative.cutscene.CutsceneSerializer;
import fr.loudo.narrativecraft.narrative.scene.Scene;
import fr.loudo.narrativecraft.network.BiSyncNarrativeEntryPacket;
import fr.loudo.narrativecraft.network.S2CToastMessage;
import fr.loudo.narrativecraft.network.cameraangle.*;
import fr.loudo.narrativecraft.network.cameraangle.C2SCameraAngleAddTemplateReference;
import fr.loudo.narrativecraft.network.cameraangle.C2SCameraAngleRemoveTemplateReference;
import fr.loudo.narrativecraft.network.cutscene.*;
import fr.loudo.narrativecraft.platform.Services;
import fr.loudo.narrativecraft.session.PlayerSession;
import fr.loudo.narrativecraft.utils.Translation;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

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

    public static void cutsceneState(C2SCutsceneEnter packet, Player player) {
        PlayerSessionManager sessionManager = NarrativeCraftMod.getInstance().getPlayerSessionManager();
        PlayerSession session = sessionManager.getByPlayer(player);
        Chapter chapter = NarrativeCraftMod.getInstance().getChapterManager().getById(packet.getChapterId());
        if (chapter == null) return;
        Scene scene = chapter.getSceneManager().getById(packet.getSceneId());
        if (scene == null) return;
        Cutscene cutscene = scene.getCutsceneManager().getById(packet.getCutsceneId());
        if (cutscene == null) return;

        CutsceneMakerEditor editor = new CutsceneMakerEditor(cutscene, session);
        session.setEditor(editor);
        editor.init();
        editor.start();

        if (cutscene.getEditorLayers() != null && !cutscene.getEditorLayers().isEmpty()) {
            String layersJson = CutsceneSerializer.serializeLayers(cutscene.getEditorLayers());
            Services.PACKET.sendToPlayer(
                    (ServerPlayer) player, new S2CCutsceneEditorData(cutscene.getId(), layersJson));
        }
    }

    public static void cutsceneControl(C2SCutsceneControl packet, Player player) {
        PlayerSessionManager sessionManager = NarrativeCraftMod.getInstance().getPlayerSessionManager();
        PlayerSession session = sessionManager.getByPlayer(player);
        CutsceneMakerEditor editor = sessionManager.getEditor(player, CutsceneMakerEditor.class);
        if (editor == null) return;

        switch (packet.state()) {
            case PLAY -> editor.play();
            case PAUSE -> editor.pause();
            case QUIT -> {
                editor.stop();
                session.setEditor(null);
            }
        }
    }

    public static void cutsceneSave(C2SCutsceneSave packet, Player player) {
        Chapter chapter = NarrativeCraftMod.getInstance().getChapterManager().getById(packet.getChapterId());
        if (chapter == null) return;
        Scene scene = chapter.getSceneManager().getById(packet.getSceneId());
        if (scene == null) return;
        Cutscene cutscene = scene.getCutsceneManager().getById(packet.getCutsceneId());
        if (cutscene == null) return;

        CutsceneDeserializer.deserializeLayers(packet.getLayersJson(), cutscene);
        int result = NarrativeCraftFileRegistry.getInstance().edit(cutscene);
        Services.PACKET.sendToPlayer(
                (ServerPlayer) player, new S2CCutsceneEditorData(cutscene.getId(), packet.getLayersJson()));

        if (result == NarrativeCraftFileEditor.OPERATION_SUCCESS) {
            Services.PACKET.sendToPlayer(
                    (ServerPlayer) player,
                    new S2CToastMessage(Translation.message("cutscene"), Translation.message("cutscene.save.success")));
        } else {
            Services.PACKET.sendToPlayer(
                    (ServerPlayer) player,
                    new S2CToastMessage(Translation.message("cutscene"), Translation.message("cutscene.save.failed")));
        }
    }

    public static void playHeadUpdate(BiCutscenePlayHeadPacket packet, Player player) {
        CutsceneMakerEditor editor =
                NarrativeCraftMod.getInstance().getPlayerSessionManager().getEditor(player, CutsceneMakerEditor.class);
        if (editor == null) return;

        editor.moveTo(packet.tick());
    }

    public static void cameraAngleEnter(C2SCameraAngleEnter packet, Player player) {
        PlayerSessionManager sessionManager = NarrativeCraftMod.getInstance().getPlayerSessionManager();
        PlayerSession session = sessionManager.getByPlayer(player);
        Chapter chapter = NarrativeCraftMod.getInstance().getChapterManager().getById(packet.getChapterId());
        if (chapter == null) return;
        Scene scene = chapter.getSceneManager().getById(packet.getSceneId());
        if (scene == null) return;
        CameraAngle cameraAngle = scene.getCameraAngleManager().getById(packet.getCameraAngleId());
        if (cameraAngle == null) return;

        CameraAngleMakerEditor editor = new CameraAngleMakerEditor(cameraAngle, session);
        session.setEditor(editor);
        editor.init();

        String dataJson = CameraAngleSerializer.serializeData(cameraAngle);
        Services.PACKET.sendToPlayer(
                (ServerPlayer) player, new S2CCameraAngleEditorData(cameraAngle.getId(), dataJson));
    }

    public static void cameraAngleControl(C2SCameraAngleControl packet, Player player) {
        PlayerSessionManager sessionManager = NarrativeCraftMod.getInstance().getPlayerSessionManager();
        PlayerSession session = sessionManager.getByPlayer(player);
        CameraAngleMakerEditor editor = sessionManager.getEditor(player, CameraAngleMakerEditor.class);
        if (editor == null) return;

        if (packet.state() == C2SCameraAngleControl.State.QUIT) {
            editor.stop();
            session.setEditor(null);
        }
    }

    public static void cameraAngleSave(C2SCameraAngleSave packet, Player player) {
        Chapter chapter = NarrativeCraftMod.getInstance().getChapterManager().getById(packet.getChapterId());
        if (chapter == null) return;
        Scene scene = chapter.getSceneManager().getById(packet.getSceneId());
        if (scene == null) return;
        CameraAngle cameraAngle = scene.getCameraAngleManager().getById(packet.getCameraAngleId());
        if (cameraAngle == null) return;

        CameraAngleDeserializer.deserializeInto(packet.getDataJson(), cameraAngle);
        int result = NarrativeCraftFileRegistry.getInstance().edit(cameraAngle);

        if (result == NarrativeCraftFileEditor.OPERATION_SUCCESS) {
            Services.PACKET.sendToPlayer(
                    (ServerPlayer) player,
                    new S2CToastMessage(
                            Translation.message("camera_angle"), Translation.message("camera_angle.save.success")));
        } else {
            Services.PACKET.sendToPlayer(
                    (ServerPlayer) player,
                    new S2CToastMessage(
                            Translation.message("camera_angle"), Translation.message("camera_angle.save.failed")));
        }
    }

    public static void cameraAngleRemovePlacement(C2SCameraAngleRemovePlacement packet, Player player) {
        Chapter chapter = NarrativeCraftMod.getInstance().getChapterManager().getById(packet.chapterId());
        if (chapter == null) return;
        Scene scene = chapter.getSceneManager().getById(packet.sceneId());
        if (scene == null) return;
        CameraAngle cameraAngle = scene.getCameraAngleManager().getById(packet.cameraAngleId());
        if (cameraAngle == null) return;

        CameraAngleMakerEditor editor = NarrativeCraftMod.getInstance()
                .getPlayerSessionManager()
                .getEditor(player, CameraAngleMakerEditor.class);
        if (editor == null) return;

        editor.removePlacement(packet.placementId());
    }

    public static void cameraAngleAddTemplateReference(C2SCameraAngleAddTemplateReference packet, Player player) {
        Chapter chapter = NarrativeCraftMod.getInstance().getChapterManager().getById(packet.chapterId());
        if (chapter == null) return;
        Scene scene = chapter.getSceneManager().getById(packet.sceneId());
        if (scene == null) return;
        CameraAngle cameraAngle = scene.getCameraAngleManager().getById(packet.cameraAngleId());
        if (cameraAngle == null) return;

        CameraAngleMakerEditor editor = NarrativeCraftMod.getInstance()
                .getPlayerSessionManager()
                .getEditor(player, CameraAngleMakerEditor.class);
        if (editor == null) return;

        TemplateSourceType sourceType;
        try {
            sourceType = TemplateSourceType.valueOf(packet.sourceType());
        } catch (IllegalArgumentException e) {
            return;
        }

        TemplateReference reference = new TemplateReference(packet.templateReferenceId(), sourceType, packet.refId());
        editor.spawnTemplateReference(reference);
    }

    public static void cameraAngleRemoveTemplateReference(C2SCameraAngleRemoveTemplateReference packet, Player player) {
        Chapter chapter = NarrativeCraftMod.getInstance().getChapterManager().getById(packet.chapterId());
        if (chapter == null) return;
        Scene scene = chapter.getSceneManager().getById(packet.sceneId());
        if (scene == null) return;
        CameraAngle cameraAngle = scene.getCameraAngleManager().getById(packet.cameraAngleId());
        if (cameraAngle == null) return;

        CameraAngleMakerEditor editor = NarrativeCraftMod.getInstance()
                .getPlayerSessionManager()
                .getEditor(player, CameraAngleMakerEditor.class);
        if (editor == null) return;

        editor.removeTemplateReference(packet.templateReferenceId());
    }

    public static void cameraAngleCaptureCharacter(C2SCameraAngleCaptureCharacter packet, Player player) {
        Chapter chapter = NarrativeCraftMod.getInstance().getChapterManager().getById(packet.chapterId());
        if (chapter == null) return;
        Scene scene = chapter.getSceneManager().getById(packet.sceneId());
        if (scene == null) return;
        CameraAngle cameraAngle = scene.getCameraAngleManager().getById(packet.cameraAngleId());
        if (cameraAngle == null) return;

        PlayerSession playerSession =
                NarrativeCraftMod.getInstance().getPlayerSessionManager().getByPlayer(player);
        Editor editor = playerSession.getEditor();
        if (!(editor instanceof CameraAngleMakerEditor cameraAngleMakerEditor)) return;

        UUID characterId = packet.characterId();
        Vec3 position = player.position();
        Vec3 rotation = new Vec3(player.getXRot(), player.getYRot(), 0.0);

        List<ItemStack> items = new ArrayList<>();
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            ItemStack stack = player.getItemBySlot(slot);
            if (!stack.isEmpty()) items.add(stack.copy());
        }
        ICharacterStory characterStory =
                NarrativeCraftMod.getInstance().getCharacterManager().resolveCharacter(characterId, scene);
        if (characterStory == null) return;

        CharacterPlacement placement = new CharacterPlacement(characterStory, position, rotation, items);
        String placementJson = CameraAngleSerializer.serializeSingleCharacterPlacement(placement);
        Services.PACKET.sendToPlayer(
                (ServerPlayer) player, new S2CCameraAngleCharacterCaptured(cameraAngle.getId(), placementJson));

        cameraAngleMakerEditor.spawnEntity(placement);
    }
}
