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
import fr.loudo.narrativecraft.api.editors.cutscene.layers.CutsceneLayer;
import fr.loudo.narrativecraft.api.signals.SignalArgument;
import fr.loudo.narrativecraft.commands.LocaleCommand;
import fr.loudo.narrativecraft.editors.EditorMaker;
import fr.loudo.narrativecraft.editors.cameraangle.CameraAngleMakerEditorMaker;
import fr.loudo.narrativecraft.editors.cutscene.CutsceneMakerEditorMaker;
import fr.loudo.narrativecraft.editors.dialog.DialogEditorMaker;
import fr.loudo.narrativecraft.editors.interaction.InteractionMakerEditorMaker;
import fr.loudo.narrativecraft.managers.PlayerSessionManager;
import fr.loudo.narrativecraft.narrative.NarrativeEntryEditor;
import fr.loudo.narrativecraft.narrative.NarrativeEntryEditorRegistry;
import fr.loudo.narrativecraft.narrative.NarrativeEntryResolver;
import fr.loudo.narrativecraft.narrative.NarrativeEnvironment;
import fr.loudo.narrativecraft.narrative.OperationResult;
import fr.loudo.narrativecraft.narrative.cameraangle.*;
import fr.loudo.narrativecraft.narrative.character.ICharacterStory;
import fr.loudo.narrativecraft.narrative.cutscene.Cutscene;
import fr.loudo.narrativecraft.narrative.cutscene.CutsceneEditor;
import fr.loudo.narrativecraft.narrative.cutscene.CutsceneSerializer;
import fr.loudo.narrativecraft.narrative.interaction.Interaction;
import fr.loudo.narrativecraft.narrative.interaction.InteractionEditor;
import fr.loudo.narrativecraft.narrative.interaction.InteractionSerializer;
import fr.loudo.narrativecraft.narrative.mainScreen.MainScreenMakerEditor;
import fr.loudo.narrativecraft.narrative.scene.Scene;
import fr.loudo.narrativecraft.narrative.story.StoryHandler;
import fr.loudo.narrativecraft.narrative.story.locale.StoryLocaleManager;
import fr.loudo.narrativecraft.network.BiEditorClose;
import fr.loudo.narrativecraft.network.BiSyncNarrativeEntryPacket;
import fr.loudo.narrativecraft.network.S2CNarrativeEntryRejected;
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
import fr.loudo.narrativecraft.network.signals.C2SEmitSignal;
import fr.loudo.narrativecraft.network.story.*;
import fr.loudo.narrativecraft.platform.Services;
import fr.loudo.narrativecraft.session.PlayerSession;
import fr.loudo.narrativecraft.utils.Translation;
import fr.loudo.narrativecraft.utils.Utils;
import fr.loudo.narrativecraft.utils.UtilsServer;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.permissions.Permissions;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

public class ServerPacketHandler {

    private static NarrativeEntryResolver resolver() {
        return NarrativeCraftMod.getInstance().getEntryResolver();
    }

    private static <E extends NarrativeEntryEditor<?, ?>> E editor(Class<E> editorClass) {
        return NarrativeEntryEditorRegistry.getInstance().getEditor(editorClass);
    }

    private static void sendSaveResult(Player player, String typeKey, OperationResult result) {
        Component message = result.isSuccess() ? Translation.message(typeKey + ".save.success") : result.getError();
        Services.PACKET.sendToPlayer((ServerPlayer) player, new S2CToastMessage(Translation.message(typeKey), message));
    }

    public static void narrativeEntry(BiSyncNarrativeEntryPacket packet, Player player) {
        if (!player.permissions().hasPermission(Permissions.COMMANDS_MODERATOR)) return;
        NarrativeEntryEditorRegistry registry = NarrativeEntryEditorRegistry.getInstance();
        OperationResult result =
                switch (packet.action()) {
                    case ADD -> registry.add(packet.entryId(), packet.entry());
                    case EDIT -> registry.edit(packet.entryId(), packet.entry());
                    case DELETE -> registry.delete(packet.entryId(), packet.entry());
                };
        if (result.isFailure()) {
            Services.PACKET.sendToPlayer(
                    (ServerPlayer) player,
                    new S2CNarrativeEntryRejected(packet.entryId(), packet.action(), result.getError()));
        }
    }

    public static void cutsceneState(BiCutsceneEnter packet, Player player) {
        if (packet.getEnvironment() == NarrativeEnvironment.DEVELOPMENT
                && !player.permissions().hasPermission(Permissions.COMMANDS_MODERATOR)) return;
        PlayerSessionManager sessionManager = NarrativeCraftMod.getInstance().getPlayerSessionManager();
        PlayerSession session = sessionManager.getByPlayer(player);
        if (session == null) return;
        Cutscene cutscene = resolver().cutscene(packet.getChapterId(), packet.getSceneId(), packet.getCutsceneId());
        if (cutscene == null) return;

        CutsceneMakerEditorMaker editor = new CutsceneMakerEditorMaker(cutscene, session, packet.getEnvironment());
        session.openEditor(editor);
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
            case QUIT -> session.closeEditor();
        }
    }

    public static void cutsceneSave(C2SCutsceneSave packet, Player player) {
        if (!player.permissions().hasPermission(Permissions.COMMANDS_MODERATOR)) return;
        Cutscene cutscene = resolver().cutscene(packet.getChapterId(), packet.getSceneId(), packet.getCutsceneId());
        if (cutscene == null) return;

        OperationResult result =
                editor(CutsceneEditor.class).saveLayers(cutscene, packet.getLayersJson(), packet.getManualMaxTick());
        List<CutsceneLayer> layers = cutscene.getLayers() == null ? List.of() : cutscene.getLayers();
        Services.PACKET.sendToPlayer(
                (ServerPlayer) player,
                new S2CCutsceneEditorData(
                        cutscene.getId(), CutsceneSerializer.serializeLayers(layers), cutscene.getManualMaxTick()));
        sendSaveResult(player, "cutscene", result);
    }

    public static void playHeadUpdate(BiCutscenePlayHeadPacket packet, Player player) {
        CutsceneMakerEditorMaker editor = NarrativeCraftMod.getInstance()
                .getPlayerSessionManager()
                .getEditor(player, CutsceneMakerEditorMaker.class);
        if (editor == null) return;

        editor.moveTo(packet.tick());
    }

    public static void cameraAngleEnter(BiCameraAngleEnter packet, Player player) {
        if (packet.getEnvironment() == NarrativeEnvironment.DEVELOPMENT
                && !player.permissions().hasPermission(Permissions.COMMANDS_MODERATOR)) return;
        PlayerSessionManager sessionManager = NarrativeCraftMod.getInstance().getPlayerSessionManager();
        PlayerSession session = sessionManager.getByPlayer(player);
        if (session == null) return;
        CameraAngle cameraAngle =
                resolver().cameraAngle(packet.getChapterId(), packet.getSceneId(), packet.getCameraAngleId());
        if (cameraAngle == null) return;

        CameraAngleMakerEditorMaker editor = new CameraAngleMakerEditorMaker(cameraAngle, session);
        session.openEditor(editor);
    }

    public static void editorCloseRequest(BiEditorClose packet, Player player) {
        PlayerSession session =
                NarrativeCraftMod.getInstance().getPlayerSessionManager().getByPlayer(player);
        if (session == null || session.getEditor() == null) return;
        if (packet.editorSessionId() != BiEditorClose.UNIDENTIFIED_SESSION
                && packet.editorSessionId() != session.getEditorSessionId()) return;
        session.closeEditor();
    }

    public static void enterDialogEditor(C2SEnterDialogEditor packet, Player player) {
        if (!player.permissions().hasPermission(Permissions.COMMANDS_MODERATOR)) return;
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
                character = resolver().npc(targetId);
            }
        }

        DialogEditorMaker editor = new DialogEditorMaker(session, character);
        session.openEditor(editor);

        if (character != null) {
            UtilsServer.sendCharacterSkin((ServerPlayer) player, character);
            Services.PACKET.sendToPlayer(
                    (ServerPlayer) player,
                    new S2CCharacterStoryAction(
                            character.getId(),
                            Utils.resolveProfileId(editor.getFakePlayer()),
                            S2CCharacterStoryAction.Action.ADD));
        }
    }

    public static void cameraAngleSave(C2SCameraAngleSave packet, Player player) {
        if (!player.permissions().hasPermission(Permissions.COMMANDS_MODERATOR)) return;
        CameraAngle cameraAngle =
                resolver().cameraAngle(packet.getChapterId(), packet.getSceneId(), packet.getCameraAngleId());
        if (cameraAngle == null) return;

        OperationResult result = editor(CameraAngleEditor.class).saveData(cameraAngle, packet.getDataJson());
        sendSaveResult(player, "camera_angle", result);
    }

    public static void cameraAngleRemovePlacement(C2SCameraAngleRemovePlacement packet, Player player) {
        if (!player.permissions().hasPermission(Permissions.COMMANDS_MODERATOR)) return;
        CameraAngle cameraAngle = resolver().cameraAngle(packet.chapterId(), packet.sceneId(), packet.cameraAngleId());
        if (cameraAngle == null) return;

        CameraAngleMakerEditorMaker editor = NarrativeCraftMod.getInstance()
                .getPlayerSessionManager()
                .getEditor(player, CameraAngleMakerEditorMaker.class);
        if (editor == null) return;

        editor.removePlacement(packet.placementId());
    }

    public static void cameraAngleAddTemplateReference(C2SCameraAngleAddTemplateReference packet, Player player) {
        if (!player.permissions().hasPermission(Permissions.COMMANDS_MODERATOR)) return;
        CameraAngle cameraAngle = resolver().cameraAngle(packet.chapterId(), packet.sceneId(), packet.cameraAngleId());
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

        TemplateReference reference =
                new TemplateReference(packet.templateReferenceId(), sourceType, packet.refId(), packet.displayName());
        editor.getTemplateReferences().add(reference);
        editor.spawnTemplateReference(reference);
    }

    public static void cameraAngleTeleportToTemplate(C2SCameraAngleTeleportToTemplate packet, Player player) {
        if (!player.permissions().hasPermission(Permissions.COMMANDS_MODERATOR)) return;
        CameraAngleMakerEditorMaker editor = NarrativeCraftMod.getInstance()
                .getPlayerSessionManager()
                .getEditor(player, CameraAngleMakerEditorMaker.class);
        if (editor == null) return;
        editor.teleportPlayerToTemplate(packet.refId());
    }

    public static void cameraAngleRemoveTemplateReference(C2SCameraAngleRemoveTemplateReference packet, Player player) {
        if (!player.permissions().hasPermission(Permissions.COMMANDS_MODERATOR)) return;
        CameraAngle cameraAngle = resolver().cameraAngle(packet.chapterId(), packet.sceneId(), packet.cameraAngleId());
        if (cameraAngle == null) return;

        CameraAngleMakerEditorMaker editor = NarrativeCraftMod.getInstance()
                .getPlayerSessionManager()
                .getEditor(player, CameraAngleMakerEditorMaker.class);
        if (editor == null) return;

        editor.removeTemplateReference(packet.templateReferenceId());
    }

    public static void interactionEnter(BiInteractionEnter packet, Player player) {
        if (packet.getEnvironment() == NarrativeEnvironment.DEVELOPMENT
                && !player.permissions().hasPermission(Permissions.COMMANDS_MODERATOR)) return;
        PlayerSessionManager sessionManager = NarrativeCraftMod.getInstance().getPlayerSessionManager();
        PlayerSession session = sessionManager.getByPlayer(player);
        if (session == null) return;
        Interaction interaction =
                resolver().interaction(packet.getChapterId(), packet.getSceneId(), packet.getInteractionId());
        if (interaction == null) return;

        InteractionMakerEditorMaker editor = new InteractionMakerEditorMaker(interaction, session);
        session.openEditor(editor);

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

    public static void emitSignal(C2SEmitSignal packet, Player player) {
        PlayerSession session =
                NarrativeCraftMod.getInstance().getPlayerSessionManager().getByPlayer(player);
        if (session == null) return;
        StoryHandler storyHandler = session.getStoryHandler();
        if (storyHandler == null) return;

        List<SignalArgument> signalArguments = packet.arguments();
        Object[] arguments = new Object[signalArguments.size()];
        for (int index = 0; index < arguments.length; index++) {
            SignalArgument signalArgument = signalArguments.get(index);
            if (!signalArgument.isValid()) {
                NarrativeCraftMod.LOGGER.warn(
                        "Player {} emitted signal '{}' with an invalid argument: {}",
                        player.getName().getString(),
                        packet.eventName(),
                        signalArgument.errorMessage());
                return;
            }
            arguments[index] = signalArgument.toInkValue();
        }
        storyHandler.playSignal(packet.eventName(), arguments);
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
        if (!player.permissions().hasPermission(Permissions.COMMANDS_MODERATOR)) return;
        Interaction interaction =
                resolver().interaction(packet.getChapterId(), packet.getSceneId(), packet.getInteractionId());
        if (interaction == null) return;

        OperationResult result = editor(InteractionEditor.class).saveData(interaction, packet.getDataJson());
        sendSaveResult(player, "interaction", result);
    }

    public static void cameraAngleSetEntityPose(C2SCameraAngleSetEntityPose packet, Player player) {
        if (!player.permissions().hasPermission(Permissions.COMMANDS_MODERATOR)) return;
        PlayerSession session =
                NarrativeCraftMod.getInstance().getPlayerSessionManager().getByPlayer(player);
        if (session == null) return;
        EditorMaker editor = session.getEditor();
        if (!(editor instanceof CameraAngleMakerEditorMaker cameraAngleMakerEditorMaker)) return;
        cameraAngleMakerEditorMaker.setEntityPose(packet.placementId(), packet.pose());
    }

    public static void cameraAngleCaptureCharacter(C2SCameraAngleCaptureCharacter packet, Player player) {
        if (!player.permissions().hasPermission(Permissions.COMMANDS_MODERATOR)) return;
        CameraAngle cameraAngle = resolver().cameraAngle(packet.chapterId(), packet.sceneId(), packet.cameraAngleId());
        if (cameraAngle == null) return;
        Scene scene = cameraAngle.getScene();

        PlayerSession playerSession =
                NarrativeCraftMod.getInstance().getPlayerSessionManager().getByPlayer(player);
        if (playerSession == null) return;
        EditorMaker editorMaker = playerSession.getEditor();
        if (!(editorMaker instanceof CameraAngleMakerEditorMaker cameraAngleMakerEditor)) return;

        UUID characterId = packet.characterId();
        Vec3 position = player.position();
        Vec3 rotation = new Vec3(player.getXRot(), player.getYRot(), 0.0);

        Map<EquipmentSlot, ItemStack> itemsBySlot = captureEquipment(player);
        ICharacterStory characterStory =
                NarrativeCraftMod.getInstance().getCharacterManager().resolveCharacter(characterId, scene);
        if (characterStory == null) return;

        CharacterPlacement placement =
                new CharacterPlacement(characterStory, position, rotation, itemsBySlot, player.onGround());
        String placementJson = CameraAngleSerializer.serializeSingleCharacterPlacement(placement);
        Services.PACKET.sendToPlayer(
                (ServerPlayer) player, new S2CCameraAngleCharacterCaptured(cameraAngle.getId(), placementJson));

        cameraAngleMakerEditor.spawnEntity(placement);
    }

    public static void mainScreenCaptureCharacter(C2SMainScreenCaptureCharacter packet, Player player) {
        if (!player.permissions().hasPermission(Permissions.COMMANDS_MODERATOR)) return;
        CameraAngle mainScreenAngle = NarrativeCraftMod.getInstance().getMainScreenData();
        if (mainScreenAngle == null) return;

        PlayerSession playerSession =
                NarrativeCraftMod.getInstance().getPlayerSessionManager().getByPlayer(player);
        if (playerSession == null) return;
        EditorMaker editorMaker = playerSession.getEditor();
        if (!(editorMaker instanceof MainScreenMakerEditor editor)) return;

        ICharacterStory characterStory =
                NarrativeCraftMod.getInstance().getCharacterManager().resolveCharacter(packet.characterId(), null);
        if (characterStory == null) return;

        Vec3 position = player.position();
        Vec3 rotation = new Vec3(player.getXRot(), player.getYRot(), 0.0);

        Map<EquipmentSlot, ItemStack> itemsBySlot = captureEquipment(player);

        CharacterPlacement placement =
                new CharacterPlacement(characterStory, position, rotation, itemsBySlot, player.onGround());
        String placementJson = CameraAngleSerializer.serializeSingleCharacterPlacement(placement);
        Services.PACKET.sendToPlayer(
                (ServerPlayer) player, new S2CCameraAngleCharacterCaptured(mainScreenAngle.getId(), placementJson));

        editor.spawnEntity(placement);
    }

    private static Map<EquipmentSlot, ItemStack> captureEquipment(Player player) {
        Map<EquipmentSlot, ItemStack> itemsBySlot = new EnumMap<>(EquipmentSlot.class);
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            ItemStack stack = player.getItemBySlot(slot);
            if (!stack.isEmpty()) itemsBySlot.put(slot, stack.copy());
        }
        return itemsBySlot;
    }

    public static void mainScreenRemovePlacement(C2SMainScreenRemovePlacement packet, Player player) {
        if (!player.permissions().hasPermission(Permissions.COMMANDS_MODERATOR)) return;
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
        if (!player.permissions().hasPermission(Permissions.COMMANDS_MODERATOR)) return;
        CameraAngle mainScreenData = NarrativeCraftMod.getInstance().getMainScreenData();
        if (mainScreenData == null) return;
        CameraAngle updated = new CameraAngle(mainScreenData.getId(), mainScreenData.getName(), null);
        OperationResult result;
        try {
            CameraAngleDeserializer.deserializeInto(packet.dataJson(), updated);
            NarrativeCraftMod.getInstance().getFile().saveMainScreenData(updated);
            mainScreenData.copyDataFrom(updated);
            result = OperationResult.success();
        } catch (Exception e) {
            NarrativeCraftMod.LOGGER.error("Failed to save main screen data!", e);
            result = OperationResult.failure("camera_angle.save.failed");
        }
        sendSaveResult(player, "camera_angle", result);
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

        String locale = packet.locale();
        playerSession.setStoryLocale(StoryLocaleManager.exists(locale) ? locale : null);
        LocaleCommand.sendTranslations(playerSession.getPlayer());

        StoryHandler storyHandler = playerSession.getStoryHandler();
        if (storyHandler != null) {
            storyHandler.refreshLocalizedContent();
        }
    }

    public static void enterMainScreen(BiMainScreenEnter packet, Player player) {
        CameraAngle mainScreenAngle = NarrativeCraftMod.getInstance().getFile().getMainScreenData();
        PlayerSession playerSession =
                NarrativeCraftMod.getInstance().getPlayerSessionManager().getByPlayer(player);
        if (playerSession == null) return;
        MainScreenMakerEditor editor = new MainScreenMakerEditor(mainScreenAngle, playerSession, packet.environment());
        playerSession.openEditor(editor);
    }
}
