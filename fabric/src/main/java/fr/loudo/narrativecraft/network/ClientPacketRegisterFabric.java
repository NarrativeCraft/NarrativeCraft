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

package fr.loudo.narrativecraft.network;

import fr.loudo.narrativecraft.network.cameraangle.*;
import fr.loudo.narrativecraft.network.cutscene.BiCutsceneEnter;
import fr.loudo.narrativecraft.network.cutscene.BiCutscenePlayHeadPacket;
import fr.loudo.narrativecraft.network.cutscene.S2CCutsceneEditorData;
import fr.loudo.narrativecraft.network.dialog.S2CDialogTest;
import fr.loudo.narrativecraft.network.inkAction.S2CRunInkAction;
import fr.loudo.narrativecraft.network.inkAction.S2CStopAllInkActions;
import fr.loudo.narrativecraft.network.interaction.BiInteractionEnter;
import fr.loudo.narrativecraft.network.interaction.S2CInteractionEditorData;
import fr.loudo.narrativecraft.network.mainScreen.BiMainScreenEnter;
import fr.loudo.narrativecraft.network.mainScreen.S2CMainScreenData;
import fr.loudo.narrativecraft.network.story.S2CCharacterStoryAction;
import fr.loudo.narrativecraft.network.story.S2CDialogStop;
import fr.loudo.narrativecraft.network.story.S2CShowChoices;
import fr.loudo.narrativecraft.network.story.S2CShowDialogue;
import fr.loudo.narrativecraft.network.story.S2CStopStory;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;

public class ClientPacketRegisterFabric {

    public static void register() {
        PayloadTypeRegistry.clientboundPlay().register(BiCutsceneEnter.TYPE, BiCutsceneEnter.STREAM_CODEC);
        PayloadTypeRegistry.clientboundPlay().register(BiCameraAngleEnter.TYPE, BiCameraAngleEnter.STREAM_CODEC);
        PayloadTypeRegistry.clientboundPlay().register(S2CNarrativeDataClear.TYPE, S2CNarrativeDataClear.STREAM_CODEC);
        PayloadTypeRegistry.clientboundPlay().register(S2CScreenClear.TYPE, S2CScreenClear.STREAM_CODEC);
        PayloadTypeRegistry.clientboundPlay().register(S2CPlayerSession.TYPE, S2CPlayerSession.STREAM_CODEC);
        PayloadTypeRegistry.clientboundPlay().register(S2CToastMessage.TYPE, S2CToastMessage.STREAM_CODEC);
        PayloadTypeRegistry.clientboundPlay().register(S2CCutsceneEditorData.TYPE, S2CCutsceneEditorData.STREAM_CODEC);
        PayloadTypeRegistry.clientboundPlay()
                .register(BiCutscenePlayHeadPacket.TYPE, BiCutscenePlayHeadPacket.STREAM_CODEC);
        PayloadTypeRegistry.clientboundPlay().register(S2CDialogTest.TYPE, S2CDialogTest.STREAM_CODEC);
        PayloadTypeRegistry.clientboundPlay()
                .register(S2CCameraAngleEditorData.TYPE, S2CCameraAngleEditorData.STREAM_CODEC);
        PayloadTypeRegistry.clientboundPlay()
                .register(S2CCameraAngleCharacterCaptured.TYPE, S2CCameraAngleCharacterCaptured.STREAM_CODEC);
        PayloadTypeRegistry.clientboundPlay()
                .register(S2CCameraAnglePlacementEntitySpawned.TYPE, S2CCameraAnglePlacementEntitySpawned.STREAM_CODEC);
        PayloadTypeRegistry.clientboundPlay().register(S2CEnterCameraView.TYPE, S2CEnterCameraView.STREAM_CODEC);
        PayloadTypeRegistry.clientboundPlay()
                .register(S2CInteractionEditorData.TYPE, S2CInteractionEditorData.STREAM_CODEC);
        PayloadTypeRegistry.clientboundPlay().register(S2CRunInkAction.TYPE, S2CRunInkAction.STREAM_CODEC);
        PayloadTypeRegistry.clientboundPlay().register(S2CStopAllInkActions.TYPE, S2CStopAllInkActions.STREAM_CODEC);
        PayloadTypeRegistry.clientboundPlay().register(S2CShowDialogue.TYPE, S2CShowDialogue.STREAM_CODEC);
        PayloadTypeRegistry.clientboundPlay().register(S2CShowChoices.TYPE, S2CShowChoices.STREAM_CODEC);
        PayloadTypeRegistry.clientboundPlay().register(S2CStopStory.TYPE, S2CStopStory.STREAM_CODEC);
        PayloadTypeRegistry.clientboundPlay().register(S2CDialogStop.TYPE, S2CDialogStop.STREAM_CODEC);
        PayloadTypeRegistry.clientboundPlay().register(S2CStopEditorMaker.TYPE, S2CStopEditorMaker.STREAM_CODEC);
        PayloadTypeRegistry.clientboundPlay().register(BiInteractionEnter.TYPE, BiInteractionEnter.STREAM_CODEC);
        PayloadTypeRegistry.clientboundPlay().register(S2CCharacterSkin.TYPE, S2CCharacterSkin.STREAM_CODEC);
        PayloadTypeRegistry.clientboundPlay()
                .register(S2CCharacterStoryAction.TYPE, S2CCharacterStoryAction.STREAM_CODEC);
        PayloadTypeRegistry.clientboundPlay().register(S2CClearLoadedSkins.TYPE, S2CClearLoadedSkins.STREAM_CODEC);
        PayloadTypeRegistry.clientboundPlay().register(S2CRenderSaveIcon.TYPE, S2CRenderSaveIcon.STREAM_CODEC);
        PayloadTypeRegistry.clientboundPlay().register(BiMainScreenEnter.TYPE, BiMainScreenEnter.STREAM_CODEC);
        PayloadTypeRegistry.clientboundPlay().register(S2CMainScreenData.TYPE, S2CMainScreenData.STREAM_CODEC);
    }
}
