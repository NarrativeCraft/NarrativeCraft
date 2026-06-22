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

import fr.loudo.narrativecraft.network.*;
import fr.loudo.narrativecraft.network.cameraangle.*;
import fr.loudo.narrativecraft.network.cutscene.BiCutsceneEnter;
import fr.loudo.narrativecraft.network.cutscene.BiCutscenePlayHeadPacket;
import fr.loudo.narrativecraft.network.cutscene.S2CCutsceneEditorData;
import fr.loudo.narrativecraft.network.dialog.S2CDialogEditorEntitySpawned;
import fr.loudo.narrativecraft.network.dialog.S2CDialogTest;
import fr.loudo.narrativecraft.network.inkAction.S2CRunInkAction;
import fr.loudo.narrativecraft.network.inkAction.S2CStopAllInkActions;
import fr.loudo.narrativecraft.network.interaction.BiInteractionEnter;
import fr.loudo.narrativecraft.network.interaction.S2CInteractionEditorData;
import fr.loudo.narrativecraft.network.mainScreen.BiMainScreenEnter;
import fr.loudo.narrativecraft.network.mainScreen.S2CMainScreenData;
import fr.loudo.narrativecraft.network.mainScreen.S2COpenMainScreen;
import fr.loudo.narrativecraft.network.story.S2CCharacterStoryAction;
import fr.loudo.narrativecraft.network.story.S2CDialogStop;
import fr.loudo.narrativecraft.network.story.S2CNotifyClientPlayStory;
import fr.loudo.narrativecraft.network.story.S2CShowChoices;
import fr.loudo.narrativecraft.network.story.S2CShowDialogue;
import fr.loudo.narrativecraft.network.story.S2CStopStory;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

public class ClientPacketHandlerFabric {

    public static void handle() {
        ClientPlayNetworking.registerGlobalReceiver(BiSyncNarrativeEntryPacket.TYPE, (packet, context) -> {
            ClientPacketHandler.narrativeEntry(packet);
        });
        ClientPlayNetworking.registerGlobalReceiver(BiCutsceneEnter.TYPE, (packet, context) -> {
            ClientPacketHandler.cutsceneState(packet);
        });
        ClientPlayNetworking.registerGlobalReceiver(S2CNarrativeDataClear.TYPE, (packet, context) -> {
            ClientPacketHandler.clearNarrativeData();
        });
        ClientPlayNetworking.registerGlobalReceiver(S2CScreenClear.TYPE, (packet, context) -> {
            ClientPacketHandler.clearScreen();
        });
        ClientPlayNetworking.registerGlobalReceiver(S2CPlayerSession.TYPE, (packet, context) -> {
            ClientPacketHandler.setSession(packet);
        });
        ClientPlayNetworking.registerGlobalReceiver(S2CToastMessage.TYPE, (packet, context) -> {
            ClientPacketHandler.showToast(packet);
        });
        ClientPlayNetworking.registerGlobalReceiver(S2CCutsceneEditorData.TYPE, (packet, context) -> {
            ClientPacketHandler.loadCutsceneEditorData(packet);
        });
        ClientPlayNetworking.registerGlobalReceiver(BiCutscenePlayHeadPacket.TYPE, (packet, context) -> {
            ClientPacketHandler.updatePlayHeadCutscene(packet);
        });
        ClientPlayNetworking.registerGlobalReceiver(S2CDialogTest.TYPE, (packet, context) -> {
            ClientPacketHandler.handleDialogTest(packet);
        });
        ClientPlayNetworking.registerGlobalReceiver(S2CCameraAngleEditorData.TYPE, (packet, context) -> {
            ClientPacketHandler.loadCameraAngleEditorData(packet);
        });
        ClientPlayNetworking.registerGlobalReceiver(S2CCameraAngleCharacterCaptured.TYPE, (packet, context) -> {
            ClientPacketHandler.addCameraAngleCharacter(packet);
        });
        ClientPlayNetworking.registerGlobalReceiver(S2CCameraAnglePlacementEntitySpawned.TYPE, (packet, context) -> {
            ClientPacketHandler.onPlacementEntitySpawned(packet);
        });
        ClientPlayNetworking.registerGlobalReceiver(S2CDialogEditorEntitySpawned.TYPE, (packet, context) -> {
            ClientPacketHandler.onDialogEditorEntitySpawned(packet);
        });
        ClientPlayNetworking.registerGlobalReceiver(S2CEnterCameraView.TYPE, (packet, context) -> {
            ClientPacketHandler.enterCameraView(packet);
        });
        ClientPlayNetworking.registerGlobalReceiver(S2CInteractionEditorData.TYPE, (packet, context) -> {
            ClientPacketHandler.loadInteractionEditorData(packet);
        });
        ClientPlayNetworking.registerGlobalReceiver(S2CRunInkAction.TYPE, (packet, context) -> {
            ClientPacketHandler.runInkAction(packet);
        });
        ClientPlayNetworking.registerGlobalReceiver(S2CStopAllInkActions.TYPE, (packet, context) -> {
            ClientPacketHandler.stopAllInkActions();
        });
        ClientPlayNetworking.registerGlobalReceiver(S2CShowDialogue.TYPE, (packet, context) -> {
            ClientPacketHandler.showDialogue(packet);
        });
        ClientPlayNetworking.registerGlobalReceiver(S2CShowChoices.TYPE, (packet, context) -> {
            ClientPacketHandler.showChoices(packet);
        });
        ClientPlayNetworking.registerGlobalReceiver(S2CStopStory.TYPE, (packet, context) -> {
            ClientPacketHandler.stopStory();
        });
        ClientPlayNetworking.registerGlobalReceiver(S2CDialogStop.TYPE, (packet, context) -> {
            ClientPacketHandler.dialogStop();
        });
        ClientPlayNetworking.registerGlobalReceiver(BiCameraAngleEnter.TYPE, (packet, context) -> {
            ClientPacketHandler.cameraAngleEnter(packet);
        });
        ClientPlayNetworking.registerGlobalReceiver(BiStopEditorMaker.TYPE, (packet, context) -> {
            ClientPacketHandler.stopEditorMaker(packet);
        });
        ClientPlayNetworking.registerGlobalReceiver(BiInteractionEnter.TYPE, (packet, context) -> {
            ClientPacketHandler.interactionEnter(packet);
        });
        ClientPlayNetworking.registerGlobalReceiver(S2CCharacterSkin.TYPE, (packet, context) -> {
            ClientPacketHandler.characterSkin(packet);
        });
        ClientPlayNetworking.registerGlobalReceiver(S2CCharacterStoryAction.TYPE, (packet, context) -> {
            ClientPacketHandler.characterStoryAction(packet);
        });
        ClientPlayNetworking.registerGlobalReceiver(S2CClearLoadedSkins.TYPE, (packet, context) -> {
            ClientPacketHandler.clearLoadedSkins(packet);
        });
        ClientPlayNetworking.registerGlobalReceiver(S2CRenderSaveIcon.TYPE, (packet, context) -> {
            ClientPacketHandler.renderSaveIcon(packet);
        });
        ClientPlayNetworking.registerGlobalReceiver(BiMainScreenEnter.TYPE, (packet, context) -> {
            ClientPacketHandler.enterMainScreen(packet);
        });
        ClientPlayNetworking.registerGlobalReceiver(S2CMainScreenData.TYPE, (packet, context) -> {
            ClientPacketHandler.receiveMainScreenData(packet);
        });
        ClientPlayNetworking.registerGlobalReceiver(S2COpenMainScreen.TYPE, (packet, context) -> {
            ClientPacketHandler.openMainScreen(packet);
        });
        ClientPlayNetworking.registerGlobalReceiver(S2CNotifyClientPlayStory.TYPE, (packet, context) -> {
            ClientPacketHandler.notifyClientPlayStory();
        });
        ClientPlayNetworking.registerGlobalReceiver(S2CSessionClear.TYPE, (packet, context) -> {
            ClientPacketHandler.sessionClear();
        });
    }
}
