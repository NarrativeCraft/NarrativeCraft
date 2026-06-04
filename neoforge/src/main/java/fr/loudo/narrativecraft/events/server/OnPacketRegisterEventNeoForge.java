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

package fr.loudo.narrativecraft.events.server;

import fr.loudo.narrativecraft.NarrativeCraftMod;
import fr.loudo.narrativecraft.network.*;
import fr.loudo.narrativecraft.network.cameraangle.*;
import fr.loudo.narrativecraft.network.cutscene.*;
import fr.loudo.narrativecraft.network.dialog.C2SEnterDialogEditor;
import fr.loudo.narrativecraft.network.dialog.S2CDialogEditorEntitySpawned;
import fr.loudo.narrativecraft.network.dialog.S2CDialogTest;
import fr.loudo.narrativecraft.network.handlers.ClientPacketHandlerNeoForge;
import fr.loudo.narrativecraft.network.handlers.ServerPacketHandlerNeoForge;
import fr.loudo.narrativecraft.network.inkAction.C2SInkActionFinished;
import fr.loudo.narrativecraft.network.inkAction.S2CRunInkAction;
import fr.loudo.narrativecraft.network.inkAction.S2CStopAllInkActions;
import fr.loudo.narrativecraft.network.interaction.BiInteractionEnter;
import fr.loudo.narrativecraft.network.interaction.C2SInteractionSave;
import fr.loudo.narrativecraft.network.interaction.S2CInteractionEditorData;
import fr.loudo.narrativecraft.network.mainScreen.BiMainScreenEnter;
import fr.loudo.narrativecraft.network.mainScreen.C2SMainScreenCaptureCharacter;
import fr.loudo.narrativecraft.network.mainScreen.C2SMainScreenSave;
import fr.loudo.narrativecraft.network.mainScreen.S2CMainScreenData;
import fr.loudo.narrativecraft.network.mainScreen.S2COpenMainScreen;
import fr.loudo.narrativecraft.network.story.*;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

@Mod(NarrativeCraftMod.MOD_ID)
public class OnPacketRegisterEventNeoForge {

    public OnPacketRegisterEventNeoForge(IEventBus modBus) {
        modBus.addListener(OnPacketRegisterEventNeoForge::onPacketRegister);
    }

    private static void onPacketRegister(RegisterPayloadHandlersEvent event) {
        final PayloadRegistrar registrar = event.registrar("1");
        registerBiPackets(registrar);
        registerS2CPackets(registrar);
        registerC2SPackets(registrar);
    }

    private static void registerS2CPackets(PayloadRegistrar registrar) {
        registrar.playToClient(
                S2CDialogTest.TYPE, S2CDialogTest.STREAM_CODEC, ClientPacketHandlerNeoForge::handleDialogTest);
        registrar.playToClient(
                S2CToastMessage.TYPE, S2CToastMessage.STREAM_CODEC, ClientPacketHandlerNeoForge::showToast);
        registrar.playToClient(
                S2CCutsceneEditorData.TYPE,
                S2CCutsceneEditorData.STREAM_CODEC,
                ClientPacketHandlerNeoForge::loadCutsceneEditorData);
        registrar.playToClient(
                S2CNarrativeDataClear.TYPE,
                S2CNarrativeDataClear.STREAM_CODEC,
                ClientPacketHandlerNeoForge::clearNarrativeData);
        registrar.playToClient(
                S2CScreenClear.TYPE, S2CScreenClear.STREAM_CODEC, ClientPacketHandlerNeoForge::clearScreen);
        registrar.playToClient(
                S2CPlayerSession.TYPE, S2CPlayerSession.STREAM_CODEC, ClientPacketHandlerNeoForge::setSession);
        registrar.playToClient(
                S2CCameraAngleEditorData.TYPE,
                S2CCameraAngleEditorData.STREAM_CODEC,
                ClientPacketHandlerNeoForge::loadCameraAngleEditorData);
        registrar.playToClient(
                S2CCameraAngleCharacterCaptured.TYPE,
                S2CCameraAngleCharacterCaptured.STREAM_CODEC,
                ClientPacketHandlerNeoForge::addCameraAngleCharacter);
        registrar.playToClient(
                S2CCameraAnglePlacementEntitySpawned.TYPE,
                S2CCameraAnglePlacementEntitySpawned.STREAM_CODEC,
                ClientPacketHandlerNeoForge::onPlacementEntitySpawned);
        registrar.playToClient(
                S2CEnterCameraView.TYPE, S2CEnterCameraView.STREAM_CODEC, ClientPacketHandlerNeoForge::enterCameraView);
        registrar.playToClient(
                S2CInteractionEditorData.TYPE,
                S2CInteractionEditorData.STREAM_CODEC,
                ClientPacketHandlerNeoForge::loadInteractionEditorData);
        registrar.playToClient(
                S2CRunInkAction.TYPE, S2CRunInkAction.STREAM_CODEC, ClientPacketHandlerNeoForge::runInkAction);
        registrar.playToClient(
                S2CStopAllInkActions.TYPE,
                S2CStopAllInkActions.STREAM_CODEC,
                ClientPacketHandlerNeoForge::stopAllInkActions);
        registrar.playToClient(
                S2CShowDialogue.TYPE, S2CShowDialogue.STREAM_CODEC, ClientPacketHandlerNeoForge::showDialogue);
        registrar.playToClient(
                S2CShowChoices.TYPE, S2CShowChoices.STREAM_CODEC, ClientPacketHandlerNeoForge::showChoices);
        registrar.playToClient(S2CStopStory.TYPE, S2CStopStory.STREAM_CODEC, ClientPacketHandlerNeoForge::stopStory);
        registrar.playToClient(S2CDialogStop.TYPE, S2CDialogStop.STREAM_CODEC, ClientPacketHandlerNeoForge::dialogStop);
        registrar.playToClient(
                S2CCharacterStoryAction.TYPE,
                S2CCharacterStoryAction.STREAM_CODEC,
                ClientPacketHandlerNeoForge::characterStoryAction);
        registrar.playToClient(
                S2CCharacterSkin.TYPE, S2CCharacterSkin.STREAM_CODEC, ClientPacketHandlerNeoForge::characterSkin);
        registrar.playToClient(
                S2CClearLoadedSkins.TYPE,
                S2CClearLoadedSkins.STREAM_CODEC,
                ClientPacketHandlerNeoForge::clearLoadedSkins);
        registrar.playToClient(
                S2CRenderSaveIcon.TYPE, S2CRenderSaveIcon.STREAM_CODEC, ClientPacketHandlerNeoForge::renderSaveIcon);
        registrar.playToClient(
                S2CMainScreenData.TYPE,
                S2CMainScreenData.STREAM_CODEC,
                ClientPacketHandlerNeoForge::receiveMainScreenData);
        registrar.playToClient(
                S2COpenMainScreen.TYPE, S2COpenMainScreen.STREAM_CODEC, ClientPacketHandlerNeoForge::openMainScreen);
        registrar.playToClient(
                S2CDialogEditorEntitySpawned.TYPE,
                S2CDialogEditorEntitySpawned.STREAM_CODEC,
                ClientPacketHandlerNeoForge::onDialogEditorEntitySpawned);
        registrar.playToClient(
                S2CNotifyClientPlayStory.TYPE,
                S2CNotifyClientPlayStory.STREAM_CODEC,
                ClientPacketHandlerNeoForge::notifyClientPlayStory);
        registrar.playToClient(
                S2CSessionClear.TYPE, S2CSessionClear.STREAM_CODEC, ClientPacketHandlerNeoForge::sessionClear);
    }

    private static void registerC2SPackets(PayloadRegistrar registrar) {
        registrar.playToServer(
                C2SCutsceneControl.TYPE, C2SCutsceneControl.STREAM_CODEC, ServerPacketHandlerNeoForge::cutsceneControl);
        registrar.playToServer(
                C2SCutsceneSave.TYPE, C2SCutsceneSave.STREAM_CODEC, ServerPacketHandlerNeoForge::cutsceneSave);
        registrar.playToServer(
                C2SCameraAngleSave.TYPE, C2SCameraAngleSave.STREAM_CODEC, ServerPacketHandlerNeoForge::cameraAngleSave);
        registrar.playToServer(
                C2SCameraAngleCaptureCharacter.TYPE,
                C2SCameraAngleCaptureCharacter.STREAM_CODEC,
                ServerPacketHandlerNeoForge::cameraAngleCaptureCharacter);
        registrar.playToServer(
                C2SCameraAngleRemovePlacement.TYPE,
                C2SCameraAngleRemovePlacement.STREAM_CODEC,
                ServerPacketHandlerNeoForge::cameraAngleRemovePlacement);
        registrar.playToServer(
                C2SCameraAngleAddTemplateReference.TYPE,
                C2SCameraAngleAddTemplateReference.STREAM_CODEC,
                ServerPacketHandlerNeoForge::cameraAngleAddTemplateReference);
        registrar.playToServer(
                C2SCameraAngleRemoveTemplateReference.TYPE,
                C2SCameraAngleRemoveTemplateReference.STREAM_CODEC,
                ServerPacketHandlerNeoForge::cameraAngleRemoveTemplateReference);
        registrar.playToServer(
                C2SCameraAngleTeleportToTemplate.TYPE,
                C2SCameraAngleTeleportToTemplate.STREAM_CODEC,
                ServerPacketHandlerNeoForge::cameraAngleTeleportToTemplate);
        registrar.playToServer(
                C2SCameraAngleSetEntityPose.TYPE,
                C2SCameraAngleSetEntityPose.STREAM_CODEC,
                ServerPacketHandlerNeoForge::cameraAngleSetEntityPose);
        registrar.playToServer(
                C2SInteractionSave.TYPE, C2SInteractionSave.STREAM_CODEC, ServerPacketHandlerNeoForge::interactionSave);
        registrar.playToServer(
                C2SInkActionFinished.TYPE,
                C2SInkActionFinished.STREAM_CODEC,
                ServerPacketHandlerNeoForge::inkActionFinished);
        registrar.playToServer(
                C2SDialogueFinished.TYPE,
                C2SDialogueFinished.STREAM_CODEC,
                ServerPacketHandlerNeoForge::dialogueFinished);
        registrar.playToServer(
                C2SChoiceSelected.TYPE, C2SChoiceSelected.STREAM_CODEC, ServerPacketHandlerNeoForge::choiceSelected);
        registrar.playToServer(
                C2SPlayStitchStory.TYPE, C2SPlayStitchStory.STREAM_CODEC, ServerPacketHandlerNeoForge::playStitch);
        registrar.playToServer(
                C2SMainScreenCaptureCharacter.TYPE,
                C2SMainScreenCaptureCharacter.STREAM_CODEC,
                ServerPacketHandlerNeoForge::mainScreenCaptureCharacter);
        registrar.playToServer(
                C2SMainScreenSave.TYPE, C2SMainScreenSave.STREAM_CODEC, ServerPacketHandlerNeoForge::mainScreenSave);
        registrar.playToServer(
                C2SEnterDialogEditor.TYPE,
                C2SEnterDialogEditor.STREAM_CODEC,
                ServerPacketHandlerNeoForge::enterDialogEditor);
    }

    private static void registerBiPackets(PayloadRegistrar registrar) {
        registrar.playBidirectional(
                BiSyncNarrativeEntryPacket.TYPE,
                BiSyncNarrativeEntryPacket.STREAM_CODEC,
                ServerPacketHandlerNeoForge::syncNarrativeEntry,
                ClientPacketHandlerNeoForge::syncNarrativeEntry);
        registrar.playBidirectional(
                BiCutscenePlayHeadPacket.TYPE,
                BiCutscenePlayHeadPacket.STREAM_CODEC,
                ServerPacketHandlerNeoForge::playHeadUpdate,
                ClientPacketHandlerNeoForge::updatePlayHeadCutscene);
        registrar.playBidirectional(
                BiCameraAngleEnter.TYPE,
                BiCameraAngleEnter.STREAM_CODEC,
                ServerPacketHandlerNeoForge::cameraAngleEnter,
                ClientPacketHandlerNeoForge::cameraAngleEnter);
        registrar.playBidirectional(
                BiCutsceneEnter.TYPE,
                BiCutsceneEnter.STREAM_CODEC,
                ServerPacketHandlerNeoForge::cutsceneState,
                ClientPacketHandlerNeoForge::cutsceneState);
        registrar.playBidirectional(
                BiMainScreenEnter.TYPE,
                BiMainScreenEnter.STREAM_CODEC,
                ServerPacketHandlerNeoForge::enterMainScreen,
                ClientPacketHandlerNeoForge::enterMainScreen);
        registrar.playBidirectional(
                BiInteractionEnter.TYPE,
                BiInteractionEnter.STREAM_CODEC,
                ServerPacketHandlerNeoForge::interactionEnter,
                ClientPacketHandlerNeoForge::interactionEnter);
        registrar.playBidirectional(
                BiStopEditorMaker.TYPE,
                BiStopEditorMaker.STREAM_CODEC,
                ServerPacketHandlerNeoForge::stopEditorMaker,
                ClientPacketHandlerNeoForge::stopEditorMaker);
        registrar.playToServer(C2SPlayStory.TYPE, C2SPlayStory.STREAM_CODEC, ServerPacketHandlerNeoForge::playStory);
    }
}
