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
import fr.loudo.narrativecraft.network.BiEditorClose;
import fr.loudo.narrativecraft.network.S2CEditorOpened;
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
import fr.loudo.narrativecraft.network.story.*;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class ClientPacketHandlerNeoForge {

    public static void syncNarrativeEntry(BiSyncNarrativeEntryPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            ClientPacketHandler.narrativeEntry(packet);
        });
    }

    public static void clearNarrativeData(S2CNarrativeDataClear packet, IPayloadContext context) {
        context.enqueueWork(ClientPacketHandler::clearNarrativeData);
    }

    public static void clearScreen(S2CScreenClear packet, IPayloadContext context) {
        context.enqueueWork(ClientPacketHandler::clearScreen);
    }

    public static void setSession(S2CPlayerSession packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            ClientPacketHandler.setSession(packet);
        });
    }

    public static void showToast(S2CToastMessage packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            ClientPacketHandler.showToast(packet);
        });
    }

    public static void loadCutsceneEditorData(S2CCutsceneEditorData packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            ClientPacketHandler.loadCutsceneEditorData(packet);
        });
    }

    public static void updatePlayHeadCutscene(BiCutscenePlayHeadPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            ClientPacketHandler.updatePlayHeadCutscene(packet);
        });
    }

    public static void handleDialogTest(S2CDialogTest packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            ClientPacketHandler.handleDialogTest(packet);
        });
    }

    public static void loadCameraAngleEditorData(S2CCameraAngleEditorData packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            ClientPacketHandler.loadCameraAngleEditorData(packet);
        });
    }

    public static void addCameraAngleCharacter(S2CCameraAngleCharacterCaptured packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            ClientPacketHandler.addCameraAngleCharacter(packet);
        });
    }

    public static void onPlacementEntitySpawned(S2CCameraAnglePlacementEntitySpawned packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            ClientPacketHandler.onPlacementEntitySpawned(packet);
        });
    }

    public static void onDialogEditorEntitySpawned(S2CDialogEditorEntitySpawned packet, IPayloadContext context) {
        context.enqueueWork(() -> ClientPacketHandler.onDialogEditorEntitySpawned(packet));
    }

    public static void enterCameraView(S2CEnterCameraView packet, IPayloadContext context) {
        context.enqueueWork(() -> ClientPacketHandler.enterCameraView(packet));
    }

    public static void loadInteractionEditorData(S2CInteractionEditorData packet, IPayloadContext context) {
        context.enqueueWork(() -> ClientPacketHandler.loadInteractionEditorData(packet));
    }

    public static void runInkAction(S2CRunInkAction packet, IPayloadContext context) {
        context.enqueueWork(() -> ClientPacketHandler.runInkAction(packet));
    }

    public static void stopAllInkActions(S2CStopAllInkActions packet, IPayloadContext context) {
        context.enqueueWork(ClientPacketHandler::stopAllInkActions);
    }

    public static void showDialogue(S2CShowDialogue packet, IPayloadContext context) {
        context.enqueueWork(() -> ClientPacketHandler.showDialogue(packet));
    }

    public static void showChoices(S2CShowChoices packet, IPayloadContext context) {
        context.enqueueWork(() -> ClientPacketHandler.showChoices(packet));
    }

    public static void stopStory(S2CStopStory packet, IPayloadContext context) {
        context.enqueueWork(ClientPacketHandler::stopStory);
    }

    public static void storyLocales(S2CStoryLocales packet, IPayloadContext context) {
        context.enqueueWork(() -> ClientPacketHandler.storyLocales(packet));
    }

    public static void ensureLocaleExists(S2CEnsureLocalExists packet, IPayloadContext context) {
        context.enqueueWork(() -> ClientPacketHandler.ensureLocalExists(packet));
    }

    public static void applyStoryLocale(S2CSetStoryLocale packet, IPayloadContext context) {
        context.enqueueWork(() -> ClientPacketHandler.applyStoryLocale(packet));
    }

    public static void storyTranslations(S2CStoryTranslations packet, IPayloadContext context) {
        context.enqueueWork(() -> ClientPacketHandler.storyTranslations(packet));
    }

    public static void storyVariables(S2CStoryVariables packet, IPayloadContext context) {
        context.enqueueWork(() -> ClientPacketHandler.storyVariables(packet));
    }

    public static void dialogStop(S2CDialogStop packet, IPayloadContext context) {
        context.enqueueWork(ClientPacketHandler::dialogStop);
    }

    public static void cutsceneState(BiCutsceneEnter packet, IPayloadContext context) {
        context.enqueueWork(() -> ClientPacketHandler.cutsceneState(packet));
    }

    public static void cameraAngleEnter(BiCameraAngleEnter packet, IPayloadContext context) {
        context.enqueueWork(() -> ClientPacketHandler.cameraAngleEnter(packet));
    }

    public static void editorClose(BiEditorClose packet, IPayloadContext context) {
        context.enqueueWork(() -> ClientPacketHandler.editorClose(packet));
    }

    public static void editorOpened(S2CEditorOpened packet, IPayloadContext context) {
        context.enqueueWork(() -> ClientPacketHandler.editorOpened(packet));
    }

    public static void interactionEnter(BiInteractionEnter packet, IPayloadContext context) {
        context.enqueueWork(() -> ClientPacketHandler.interactionEnter(packet));
    }

    public static void characterSkin(S2CCharacterSkin packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            ClientPacketHandler.characterSkin(packet);
        });
    }

    public static void characterStoryAction(S2CCharacterStoryAction packet, IPayloadContext context) {
        context.enqueueWork(() -> ClientPacketHandler.characterStoryAction(packet));
    }

    public static void clearLoadedSkins(S2CClearLoadedSkins packet, IPayloadContext context) {
        context.enqueueWork(() -> ClientPacketHandler.clearLoadedSkins(packet));
    }

    public static void renderSaveIcon(S2CRenderSaveIcon packet, IPayloadContext context) {
        context.enqueueWork(() -> ClientPacketHandler.renderSaveIcon(packet));
    }

    public static void enterMainScreen(BiMainScreenEnter packet, IPayloadContext context) {
        context.enqueueWork(() -> ClientPacketHandler.enterMainScreen(packet));
    }

    public static void receiveMainScreenData(S2CMainScreenData packet, IPayloadContext context) {
        context.enqueueWork(() -> ClientPacketHandler.receiveMainScreenData(packet));
    }

    public static void openMainScreen(S2COpenMainScreen packet, IPayloadContext context) {
        context.enqueueWork(() -> ClientPacketHandler.openMainScreen(packet));
    }

    public static void notifyClientPlayStory(S2CNotifyClientPlayStory packet, IPayloadContext context) {
        context.enqueueWork(ClientPacketHandler::notifyClientPlayStory);
    }

    public static void sessionClear(S2CSessionClear packet, IPayloadContext context) {
        context.enqueueWork(ClientPacketHandler::sessionClear);
    }
}
