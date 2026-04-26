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
import fr.loudo.narrativecraft.network.BiSyncNarrativeEntryPacket;
import fr.loudo.narrativecraft.network.S2CNarrativeDataClear;
import fr.loudo.narrativecraft.network.S2CPlayerSession;
import fr.loudo.narrativecraft.network.S2CScreenClear;
import fr.loudo.narrativecraft.network.S2CToastMessage;
import fr.loudo.narrativecraft.network.cameraangle.C2SCameraAngleAddTemplateReference;
import fr.loudo.narrativecraft.network.cameraangle.C2SCameraAngleCaptureCharacter;
import fr.loudo.narrativecraft.network.cameraangle.C2SCameraAngleControl;
import fr.loudo.narrativecraft.network.cameraangle.C2SCameraAngleEnter;
import fr.loudo.narrativecraft.network.cameraangle.C2SCameraAngleRemovePlacement;
import fr.loudo.narrativecraft.network.cameraangle.C2SCameraAngleRemoveTemplateReference;
import fr.loudo.narrativecraft.network.cameraangle.C2SCameraAngleSave;
import fr.loudo.narrativecraft.network.cameraangle.C2SCameraAngleTeleportToTemplate;
import fr.loudo.narrativecraft.network.cameraangle.S2CCameraAngleCharacterCaptured;
import fr.loudo.narrativecraft.network.cameraangle.S2CCameraAngleEditorData;
import fr.loudo.narrativecraft.network.cameraangle.S2CCameraAnglePlacementEntitySpawned;
import fr.loudo.narrativecraft.network.cutscene.BiCutscenePlayHeadPacket;
import fr.loudo.narrativecraft.network.cutscene.C2SCutsceneControl;
import fr.loudo.narrativecraft.network.cutscene.C2SCutsceneEnter;
import fr.loudo.narrativecraft.network.cutscene.C2SCutsceneSave;
import fr.loudo.narrativecraft.network.cutscene.S2CCutsceneEditorData;
import fr.loudo.narrativecraft.network.dialog.S2CDialogTest;
import fr.loudo.narrativecraft.network.handlers.ClientPacketHandlerNeoForge;
import fr.loudo.narrativecraft.network.handlers.ServerPacketHandlerNeoForge;
import fr.loudo.narrativecraft.network.interaction.C2SInteractionEnter;
import fr.loudo.narrativecraft.network.interaction.C2SInteractionSave;
import fr.loudo.narrativecraft.network.interaction.S2CInteractionEditorData;
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
        registrar.playBidirectional(
                BiSyncNarrativeEntryPacket.TYPE,
                BiSyncNarrativeEntryPacket.STREAM_CODEC,
                ServerPacketHandlerNeoForge::syncNarrativeEntry);
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
                S2CInteractionEditorData.TYPE,
                S2CInteractionEditorData.STREAM_CODEC,
                ClientPacketHandlerNeoForge::loadInteractionEditorData);
    }

    private static void registerC2SPackets(PayloadRegistrar registrar) {
        registrar.playToServer(
                C2SCutsceneEnter.TYPE, C2SCutsceneEnter.STREAM_CODEC, ServerPacketHandlerNeoForge::cutsceneState);
        registrar.playToServer(
                C2SCutsceneControl.TYPE, C2SCutsceneControl.STREAM_CODEC, ServerPacketHandlerNeoForge::cutsceneControl);
        registrar.playToServer(
                C2SCutsceneSave.TYPE, C2SCutsceneSave.STREAM_CODEC, ServerPacketHandlerNeoForge::cutsceneSave);
        registrar.playBidirectional(
                BiCutscenePlayHeadPacket.TYPE,
                BiCutscenePlayHeadPacket.STREAM_CODEC,
                ServerPacketHandlerNeoForge::playHeadUpdate,
                ClientPacketHandlerNeoForge::updatePlayHeadCutscene);
        registrar.playToServer(
                C2SCameraAngleEnter.TYPE,
                C2SCameraAngleEnter.STREAM_CODEC,
                ServerPacketHandlerNeoForge::cameraAngleEnter);
        registrar.playToServer(
                C2SCameraAngleControl.TYPE,
                C2SCameraAngleControl.STREAM_CODEC,
                ServerPacketHandlerNeoForge::cameraAngleControl);
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
                C2SInteractionEnter.TYPE,
                C2SInteractionEnter.STREAM_CODEC,
                ServerPacketHandlerNeoForge::interactionEnter);
        registrar.playToServer(
                C2SInteractionSave.TYPE, C2SInteractionSave.STREAM_CODEC, ServerPacketHandlerNeoForge::interactionSave);
    }
}
