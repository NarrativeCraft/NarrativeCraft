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

package fr.loudo.narrativecraft.network.handlers;

import fr.loudo.narrativecraft.NarrativeCraftMod;
import fr.loudo.narrativecraft.editors.EditorMaker;
import fr.loudo.narrativecraft.editors.cameraangle.CameraAngleMakerEditorMaker;
import fr.loudo.narrativecraft.editors.cutscene.CutsceneMakerEditorMaker;
import fr.loudo.narrativecraft.editors.dialog.DialogEditorMaker;
import fr.loudo.narrativecraft.editors.interaction.InteractionMakerEditorMaker;
import fr.loudo.narrativecraft.files.NarrativeCraftFileEditor;
import fr.loudo.narrativecraft.files.NarrativeCraftFileRegistry;
import fr.loudo.narrativecraft.managers.PlayerSessionManager;
import fr.loudo.narrativecraft.narrative.NarrativeEntryEditorRegistry;
import fr.loudo.narrativecraft.narrative.NarrativeEnvironment;
import fr.loudo.narrativecraft.narrative.cameraangle.*;
import fr.loudo.narrativecraft.narrative.chapter.Chapter;
import fr.loudo.narrativecraft.narrative.character.ICharacterStory;
import fr.loudo.narrativecraft.narrative.cutscene.Cutscene;
import fr.loudo.narrativecraft.narrative.cutscene.CutsceneDeserializer;
import fr.loudo.narrativecraft.narrative.interaction.Interaction;
import fr.loudo.narrativecraft.narrative.interaction.InteractionDeserializer;
import fr.loudo.narrativecraft.narrative.interaction.InteractionSerializer;
import fr.loudo.narrativecraft.narrative.mainScreen.MainScreenMakerEditor;
import fr.loudo.narrativecraft.narrative.npc.Npc;
import fr.loudo.narrativecraft.narrative.scene.Scene;
import fr.loudo.narrativecraft.narrative.story.StoryHandler;
import fr.loudo.narrativecraft.narrative.story.StoryLibrary;
import fr.loudo.narrativecraft.network.BiStopEditorMaker;
import fr.loudo.narrativecraft.network.BiSyncNarrativeEntryPacket;
import fr.loudo.narrativecraft.network.C2SChangeGamemodePacket;
import fr.loudo.narrativecraft.network.S2CToastMessage;
import fr.loudo.narrativecraft.network.cameraangle.*;
import fr.loudo.narrativecraft.network.cutscene.*;
import fr.loudo.narrativecraft.network.dialog.C2SEnterDialogEditor;
import fr.loudo.narrativecraft.network.inkAction.C2SInkActionFinished;
import fr.loudo.narrativecraft.network.interaction.BiInteractionEnter;
import fr.loudo.narrativecraft.network.interaction.C2SInteractionSave;
import fr.loudo.narrativecraft.network.interaction.S2CInteractionEditorData;
import fr.loudo.narrativecraft.network.mainScreen.BiMainScreenEnter;
import fr.loudo.narrativecraft.network.mainScreen.C2SMainScreenCaptureCharacter;
import fr.loudo.narrativecraft.network.mainScreen.C2SMainScreenRemovePlacement;
import fr.loudo.narrativecraft.network.mainScreen.C2SMainScreenSave;
import fr.loudo.narrativecraft.network.story.*;
import fr.loudo.narrativecraft.platform.Services;
import fr.loudo.narrativecraft.session.PlayerSession;
import fr.loudo.narrativecraft.utils.Translation;
import fr.loudo.narrativecraft.utils.UtilsServer;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

public class ServerPacketHandler {

    public static void narrativeEntry(BiSyncNarrativeEntryPacket packet, Player player) {
        if (!player.hasPermissions(2)) return;
        switch (packet.action()) {
            case ADD ->
                NarrativeEntryEditorRegistry.getInstance().add(packet.entryId(), packet.entry(), player.getUUID());
            case EDIT ->
                NarrativeEntryEditorRegistry.getInstance().edit(packet.entryId(), packet.entry(), player.getUUID());
            case DELETE ->
                NarrativeEntryEditorRegistry.getInstance().delete(packet.entryId(), packet.entry(), player.getUUID());
        }
    }

    public static void cutsceneState(BiCutsceneEnter packet, Player player) {
        if (packet.getEnvironment() == NarrativeEnvironment.DEVELOPMENT && !player.hasPermissions(2)) return;
        PlayerSessionManager sessionManager = NarrativeCraftMod.getInstance().getPlayerSessionManager();
        PlayerSession session = sessionManager.getByPlayer(player);
        if (session == null) return;
        Chapter chapter = NarrativeCraftMod.getInstance().getChapterManager().getById(packet.getChapterId());
        if (chapter == null) return;
        Scene scene = chapter.getSceneManager().getById(packet.getSceneId());
        if (scene == null) return;
        Cutscene cutscene = scene.getCutsceneManager().getById(packet.getCutsceneId());
        if (cutscene == null) return;

        CutsceneMakerEditorMaker editor = new CutsceneMakerEditorMaker(cutscene, session, packet.getEnvironment());
        session.setEditor(editor);
        editor.init();
        editor.start();
    }

    public static void cutsceneControl(C2SCutsceneControl packet, Player player) {
        PlayerSessionManager sessionManager = NarrativeCraftMod.getInstance().getPlayerSessionManager();
        PlayerSession session = sessionManager.getByPlayer(player);
        if (session == null) return;
        CutsceneMakerEditorMaker editor = sessionManager.getEditor(player, CutsceneMakerEditorMaker.class);
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
        if (!player.hasPermissions(2)) return;
        Chapter chapter = NarrativeCraftMod.getInstance().getChapterManager().getById(packet.getChapterId());
        if (chapter == null) return;
        Scene scene = chapter.getSceneManager().getById(packet.getSceneId());
        if (scene == null) return;
        Cutscene cutscene = scene.getCutsceneManager().getById(packet.getCutsceneId());
        if (cutscene == null) return;

        CutsceneDeserializer.deserializeLayers(packet.getLayersJson(), cutscene);
        cutscene.setManualMaxTick(packet.getManualMaxTick());
        int result = NarrativeCraftFileRegistry.getInstance().edit(cutscene);
        Services.PACKET.sendToPlayer(
                (ServerPlayer) player,
                new S2CCutsceneEditorData(cutscene.getId(), packet.getLayersJson(), cutscene.getManualMaxTick()));

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
        CutsceneMakerEditorMaker editor = NarrativeCraftMod.getInstance()
                .getPlayerSessionManager()
                .getEditor(player, CutsceneMakerEditorMaker.class);
        if (editor == null) return;

        editor.moveTo(packet.tick());
    }

    public static void cameraAngleEnter(BiCameraAngleEnter packet, Player player) {
        if (packet.getEnvironment() == NarrativeEnvironment.DEVELOPMENT && !player.hasPermissions(2)) return;
        PlayerSessionManager sessionManager = NarrativeCraftMod.getInstance().getPlayerSessionManager();
        PlayerSession session = sessionManager.getByPlayer(player);
        if (session == null) return;
        Chapter chapter = NarrativeCraftMod.getInstance().getChapterManager().getById(packet.getChapterId());
        if (chapter == null) return;
        Scene scene = chapter.getSceneManager().getById(packet.getSceneId());
        if (scene == null) return;
        CameraAngle cameraAngle = scene.getCameraAngleManager().getById(packet.getCameraAngleId());
        if (cameraAngle == null) return;

        CameraAngleMakerEditorMaker editor = new CameraAngleMakerEditorMaker(cameraAngle, session);
        session.setEditor(editor);
        editor.init();
    }

    public static void stopEditorMaker(BiStopEditorMaker packet, Player player) {
        PlayerSession session =
                NarrativeCraftMod.getInstance().getPlayerSessionManager().getByPlayer(player);
        if (session == null) return;
        EditorMaker editor = session.getEditor();
        if (editor == null) return;
        editor.stop();
        session.setEditor(null);
    }

    public static void enterDialogEditor(C2SEnterDialogEditor packet, Player player) {
        if (!player.hasPermissions(2)) return;
        PlayerSessionManager sessionManager = NarrativeCraftMod.getInstance().getPlayerSessionManager();
        PlayerSession session = sessionManager.getByPlayer(player);
        if (session == null) return;

        ICharacterStory character = null;
        if (!packet.targetId().isEmpty()) {
            UUID targetId;
            try {
                targetId = UUID.fromString(packet.targetId());
            } catch (IllegalArgumentException e) {
                return;
            }
            if ("character".equals(packet.editorType())) {
                character =
                        NarrativeCraftMod.getInstance().getCharacterManager().getById(targetId);
            } else if ("npc".equals(packet.editorType())) {
                character = findNpcById(targetId);
            }
        }

        DialogEditorMaker editor = new DialogEditorMaker(session, character);
        session.setEditor(editor);
        editor.init();

        UtilsServer.sendCharacterSkin((ServerPlayer) player, character);
        Services.PACKET.sendToPlayer(
                (ServerPlayer) player,
                new S2CCharacterStoryAction(character.getId(), S2CCharacterStoryAction.Action.ADD));
    }

    private static Npc findNpcById(UUID id) {
        for (Chapter chapter :
                NarrativeCraftMod.getInstance().getChapterManager().getList()) {
            for (Scene scene : chapter.getSceneManager().getList()) {
                Npc npc = scene.getNpcManager().getById(id);
                if (npc != null) return npc;
            }
        }
        return null;
    }

    public static void cameraAngleSave(C2SCameraAngleSave packet, Player player) {
        if (!player.hasPermissions(2)) return;
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
        if (!player.hasPermissions(2)) return;
        Chapter chapter = NarrativeCraftMod.getInstance().getChapterManager().getById(packet.chapterId());
        if (chapter == null) return;
        Scene scene = chapter.getSceneManager().getById(packet.sceneId());
        if (scene == null) return;
        CameraAngle cameraAngle = scene.getCameraAngleManager().getById(packet.cameraAngleId());
        if (cameraAngle == null) return;

        CameraAngleMakerEditorMaker editor = NarrativeCraftMod.getInstance()
                .getPlayerSessionManager()
                .getEditor(player, CameraAngleMakerEditorMaker.class);
        if (editor == null) return;

        editor.removePlacement(packet.placementId());
    }

    public static void cameraAngleAddTemplateReference(C2SCameraAngleAddTemplateReference packet, Player player) {
        if (!player.hasPermissions(2)) return;
        Chapter chapter = NarrativeCraftMod.getInstance().getChapterManager().getById(packet.chapterId());
        if (chapter == null) return;
        Scene scene = chapter.getSceneManager().getById(packet.sceneId());
        if (scene == null) return;
        CameraAngle cameraAngle = scene.getCameraAngleManager().getById(packet.cameraAngleId());
        if (cameraAngle == null) return;

        CameraAngleMakerEditorMaker editor = NarrativeCraftMod.getInstance()
                .getPlayerSessionManager()
                .getEditor(player, CameraAngleMakerEditorMaker.class);
        if (editor == null) return;

        TemplateSourceType sourceType;
        try {
            sourceType = TemplateSourceType.valueOf(packet.sourceType());
        } catch (IllegalArgumentException e) {
            return;
        }

        TemplateReference reference = new TemplateReference(packet.templateReferenceId(), sourceType, packet.refId());
        editor.getTemplateReferences().add(reference);
        editor.spawnTemplateReference(reference);
    }

    public static void cameraAngleTeleportToTemplate(C2SCameraAngleTeleportToTemplate packet, Player player) {
        if (!player.hasPermissions(2)) return;
        CameraAngleMakerEditorMaker editor = NarrativeCraftMod.getInstance()
                .getPlayerSessionManager()
                .getEditor(player, CameraAngleMakerEditorMaker.class);
        if (editor == null) return;
        editor.teleportPlayerToTemplate(packet.refId());
    }

    public static void cameraAngleRemoveTemplateReference(C2SCameraAngleRemoveTemplateReference packet, Player player) {
        if (!player.hasPermissions(2)) return;
        Chapter chapter = NarrativeCraftMod.getInstance().getChapterManager().getById(packet.chapterId());
        if (chapter == null) return;
        Scene scene = chapter.getSceneManager().getById(packet.sceneId());
        if (scene == null) return;
        CameraAngle cameraAngle = scene.getCameraAngleManager().getById(packet.cameraAngleId());
        if (cameraAngle == null) return;

        CameraAngleMakerEditorMaker editor = NarrativeCraftMod.getInstance()
                .getPlayerSessionManager()
                .getEditor(player, CameraAngleMakerEditorMaker.class);
        if (editor == null) return;

        editor.removeTemplateReference(packet.templateReferenceId());
    }

    public static void interactionEnter(BiInteractionEnter packet, Player player) {
        if (packet.getEnvironment() == NarrativeEnvironment.DEVELOPMENT && !player.hasPermissions(2)) return;
        PlayerSessionManager sessionManager = NarrativeCraftMod.getInstance().getPlayerSessionManager();
        PlayerSession session = sessionManager.getByPlayer(player);
        if (session == null) return;
        Chapter chapter = NarrativeCraftMod.getInstance().getChapterManager().getById(packet.getChapterId());
        if (chapter == null) return;
        Scene scene = chapter.getSceneManager().getById(packet.getSceneId());
        if (scene == null) return;
        Interaction interaction = scene.getInteractionManager().getById(packet.getInteractionId());
        if (interaction == null) return;

        InteractionMakerEditorMaker editor = new InteractionMakerEditorMaker(interaction, session);
        session.setEditor(editor);
        editor.init();

        String dataJson = InteractionSerializer.serializeData(interaction);
        Services.PACKET.sendToPlayer(
                (ServerPlayer) player, new S2CInteractionEditorData(interaction.getId(), dataJson));
    }

    public static void inkActionFinished(C2SInkActionFinished packet, Player player) {
        PlayerSession session =
                NarrativeCraftMod.getInstance().getPlayerSessionManager().getByPlayer(player);
        if (session == null) return;
        StoryHandler storyHandler = session.getStoryHandler();
        if (storyHandler != null) {
            storyHandler.getInkTagHandler().onClientActionFinished(packet.instanceId());
        }
    }

    public static void stopStory(C2SStopStory packet, Player player) {
        PlayerSession session =
                NarrativeCraftMod.getInstance().getPlayerSessionManager().getByPlayer(player);
        if (session == null) return;
        StoryHandler storyHandler = session.getStoryHandler();
        if (storyHandler != null) {
            storyHandler.stop();
            if (packet.showMainScreen()) {
                UtilsServer.openMainScreenToPlayer((ServerPlayer) player);
            }
        }
    }

    public static void dialogueFinished(C2SDialogueFinished packet, Player player) {
        PlayerSession session =
                NarrativeCraftMod.getInstance().getPlayerSessionManager().getByPlayer(player);
        if (session == null) return;
        StoryHandler storyHandler = session.getStoryHandler();
        if (storyHandler != null) {
            storyHandler.onDialogueAck();
        }
    }

    public static void choiceSelected(C2SChoiceSelected packet, Player player) {
        PlayerSession session =
                NarrativeCraftMod.getInstance().getPlayerSessionManager().getByPlayer(player);
        if (session == null) return;
        StoryHandler storyHandler = session.getStoryHandler();
        if (storyHandler != null) {
            storyHandler.onChoiceSelected(packet.index());
        }
    }

    public static void playStitch(C2SPlayStitchStory packet, Player player) {
        PlayerSession session =
                NarrativeCraftMod.getInstance().getPlayerSessionManager().getByPlayer(player);
        if (session == null) return;
        StoryHandler storyHandler = session.getStoryHandler();
        if (storyHandler != null && session.isGameplayMode()) {
            if (packet.oneTime() && storyHandler.hasAlreadyInteracted(packet.interactionId())) return;
            if (packet.oneTime()) storyHandler.addInteractionId(packet.interactionId());
            storyHandler.playStitch(packet.stitchName());
        }
    }

    public static void interactionSave(C2SInteractionSave packet, Player player) {
        if (!player.hasPermissions(2)) return;
        Chapter chapter = NarrativeCraftMod.getInstance().getChapterManager().getById(packet.getChapterId());
        if (chapter == null) return;
        Scene scene = chapter.getSceneManager().getById(packet.getSceneId());
        if (scene == null) return;
        Interaction interaction = scene.getInteractionManager().getById(packet.getInteractionId());
        if (interaction == null) return;

        InteractionDeserializer.deserializeInto(packet.getDataJson(), interaction);
        int result = NarrativeCraftFileRegistry.getInstance().edit(interaction);

        if (result == NarrativeCraftFileEditor.OPERATION_SUCCESS) {
            Services.PACKET.sendToPlayer(
                    (ServerPlayer) player,
                    new S2CToastMessage(
                            Translation.message("interaction"), Translation.message("interaction.save.success")));
        } else {
            Services.PACKET.sendToPlayer(
                    (ServerPlayer) player,
                    new S2CToastMessage(
                            Translation.message("interaction"), Translation.message("interaction.save.failed")));
        }
    }

    public static void cameraAngleSetEntityPose(C2SCameraAngleSetEntityPose packet, Player player) {
        if (!player.hasPermissions(2)) return;
        PlayerSession session =
                NarrativeCraftMod.getInstance().getPlayerSessionManager().getByPlayer(player);
        if (session == null) return;
        EditorMaker editor = session.getEditor();
        if (!(editor instanceof CameraAngleMakerEditorMaker cameraAngleMakerEditorMaker)) return;
        cameraAngleMakerEditorMaker.setEntityPose(packet.placementId(), packet.pose());
    }

    public static void cameraAngleCaptureCharacter(C2SCameraAngleCaptureCharacter packet, Player player) {
        if (!player.hasPermissions(2)) return;
        Chapter chapter = NarrativeCraftMod.getInstance().getChapterManager().getById(packet.chapterId());
        if (chapter == null) return;
        Scene scene = chapter.getSceneManager().getById(packet.sceneId());
        if (scene == null) return;
        CameraAngle cameraAngle = scene.getCameraAngleManager().getById(packet.cameraAngleId());
        if (cameraAngle == null) return;

        PlayerSession playerSession =
                NarrativeCraftMod.getInstance().getPlayerSessionManager().getByPlayer(player);
        if (playerSession == null) return;
        EditorMaker editorMaker = playerSession.getEditor();
        if (!(editorMaker instanceof CameraAngleMakerEditorMaker cameraAngleMakerEditor)) return;

        Services.PACKET.sendToPlayer(
                playerSession.getPlayer(),
                new S2CCharacterStoryAction(packet.characterId(), S2CCharacterStoryAction.Action.ADD));

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

    public static void mainScreenCaptureCharacter(C2SMainScreenCaptureCharacter packet, Player player) {
        if (!player.hasPermissions(2)) return;
        CameraAngle mainScreenAngle = NarrativeCraftMod.getInstance().getMainScreenData();
        if (mainScreenAngle == null) return;

        PlayerSession playerSession =
                NarrativeCraftMod.getInstance().getPlayerSessionManager().getByPlayer(player);
        if (playerSession == null) return;
        EditorMaker editorMaker = playerSession.getEditor();
        if (!(editorMaker instanceof MainScreenMakerEditor editor)) return;

        Services.PACKET.sendToPlayer(
                playerSession.getPlayer(),
                new S2CCharacterStoryAction(packet.characterId(), S2CCharacterStoryAction.Action.ADD));

        ICharacterStory characterStory =
                NarrativeCraftMod.getInstance().getCharacterManager().resolveCharacter(packet.characterId(), null);
        if (characterStory == null) return;

        Vec3 position = player.position();
        Vec3 rotation = new Vec3(player.getXRot(), player.getYRot(), 0.0);

        List<ItemStack> items = new ArrayList<>();
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            ItemStack stack = player.getItemBySlot(slot);
            if (!stack.isEmpty()) items.add(stack.copy());
        }

        CharacterPlacement placement = new CharacterPlacement(characterStory, position, rotation, items);
        String placementJson = CameraAngleSerializer.serializeSingleCharacterPlacement(placement);
        Services.PACKET.sendToPlayer(
                (ServerPlayer) player, new S2CCameraAngleCharacterCaptured(mainScreenAngle.getId(), placementJson));

        editor.spawnEntity(placement);
    }

    public static void mainScreenRemovePlacement(C2SMainScreenRemovePlacement packet, Player player) {
        if (!player.hasPermissions(2)) return;
        CameraAngle mainScreenAngle = NarrativeCraftMod.getInstance().getMainScreenData();
        if (mainScreenAngle == null) return;

        PlayerSession playerSession =
                NarrativeCraftMod.getInstance().getPlayerSessionManager().getByPlayer(player);
        if (playerSession == null) return;
        EditorMaker editorMaker = playerSession.getEditor();
        if (!(editorMaker instanceof MainScreenMakerEditor editor)) return;

        editor.removePlacement(packet.placementId());
    }

    public static void mainScreenSave(C2SMainScreenSave packet, Player player) {
        if (!player.hasPermissions(2)) return;
        CameraAngle mainScreenData = NarrativeCraftMod.getInstance().getMainScreenData();
        if (mainScreenData == null) return;
        CameraAngleDeserializer.deserializeInto(packet.dataJson(), mainScreenData);
        try {
            NarrativeCraftMod.getInstance().getFile().saveMainScreenData(mainScreenData);
            Services.PACKET.sendToPlayer(
                    (ServerPlayer) player,
                    new S2CToastMessage(
                            Translation.message("camera_angle"), Translation.message("camera_angle.save.success")));
        } catch (Exception e) {
            NarrativeCraftMod.LOGGER.error("Failed to save main screen data!", e);
            Services.PACKET.sendToPlayer(
                    (ServerPlayer) player,
                    new S2CToastMessage(
                            Translation.message("camera_angle"), Translation.message("camera_angle.save.failed")));
        }
    }

    public static void playStory(C2SPlayStory packet, Player player) {
        PlayerSession playerSession =
                NarrativeCraftMod.getInstance().getPlayerSessionManager().getByPlayer(player);
        if (playerSession == null) return;
        StoryHandler storyHandler = playerSession.getStoryHandler();
        if (storyHandler != null) {
            storyHandler.stop();
        }

        if (packet.newGame()) {
            NarrativeCraftMod.getInstance().getSaveFileManager().removeSaveFile((ServerPlayer) player);
        }

        try {
            storyHandler = null;
            if (packet.fromSave()) {
                storyHandler =
                        NarrativeCraftMod.getInstance().getSaveFileManager().loadSave(playerSession);
            }
            if (storyHandler == null) {
                storyHandler = new StoryHandler(playerSession);
            }
            playerSession.setStoryHandler(storyHandler);
            warnIfSaveKeepsItsLocale(playerSession, storyHandler);
            if (packet.stitchName().isEmpty()) {
                storyHandler.start();
            } else {
                storyHandler.start(packet.stitchName().get());
            }
        } catch (Exception e) {
            NarrativeCraftMod.LOGGER.error("Failed to start story!", e);
            player.sendSystemMessage(Translation.message("error.start_story"));
        }
    }

    public static void setStoryLocale(C2SSetStoryLocale packet, Player player) {
        PlayerSession playerSession =
                NarrativeCraftMod.getInstance().getPlayerSessionManager().getByPlayer(player);
        if (playerSession == null) return;

        StoryLibrary storyLibrary = NarrativeCraftMod.getInstance().getStoryLibrary();
        String locale = packet.locale();
        boolean known = storyLibrary != null && storyLibrary.getLocales().contains(locale);
        playerSession.setStoryLocale(known ? locale : null);

        StoryHandler storyHandler = playerSession.getStoryHandler();
        if (storyHandler == null || !localeDiffers(playerSession, storyHandler)) return;

        sendLocaleToast(playerSession, Translation.message("locale.applies_next_load"));
    }

    private static void warnIfSaveKeepsItsLocale(PlayerSession playerSession, StoryHandler storyHandler) {
        if (!localeDiffers(playerSession, storyHandler)) return;

        sendLocaleToast(
                playerSession,
                Translation.message(
                        "locale.save_incompatible", playerSession.getStoryLocale(), storyHandler.getStoryLocale()));
    }

    private static boolean localeDiffers(PlayerSession playerSession, StoryHandler storyHandler) {
        String requestedLocale = playerSession.getStoryLocale();
        return requestedLocale != null && !requestedLocale.equals(storyHandler.getStoryLocale());
    }

    private static void sendLocaleToast(PlayerSession playerSession, Component message) {
        Services.PACKET.sendToPlayer(
                playerSession.getPlayer(), new S2CToastMessage(Translation.message("locale"), message));
    }

    public static void enterMainScreen(BiMainScreenEnter packet, Player player) {
        CameraAngle mainScreenAngle = NarrativeCraftMod.getInstance().getFile().getMainScreenData();
        PlayerSession playerSession =
                NarrativeCraftMod.getInstance().getPlayerSessionManager().getByPlayer(player);
        if (playerSession == null) return;
        MainScreenMakerEditor editor = new MainScreenMakerEditor(mainScreenAngle, playerSession, packet.environment());
        playerSession.setEditor(editor);
        editor.init();
    }

    public static void changeGamemode(C2SChangeGamemodePacket packet, Player player) {
        if (!player.hasPermissions(2)) return;
        ((ServerPlayer) player).setGameMode(packet.gameType());
    }
}
