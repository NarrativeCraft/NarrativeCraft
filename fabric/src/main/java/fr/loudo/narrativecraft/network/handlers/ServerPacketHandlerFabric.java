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

import fr.loudo.narrativecraft.network.BiStopEditorMaker;
import fr.loudo.narrativecraft.network.BiSyncNarrativeEntryPacket;
import fr.loudo.narrativecraft.network.cameraangle.BiCameraAngleEnter;
import fr.loudo.narrativecraft.network.cameraangle.C2SCameraAngleAddTemplateReference;
import fr.loudo.narrativecraft.network.cameraangle.C2SCameraAngleCaptureCharacter;
import fr.loudo.narrativecraft.network.cameraangle.C2SCameraAngleRemovePlacement;
import fr.loudo.narrativecraft.network.cameraangle.C2SCameraAngleSave;
import fr.loudo.narrativecraft.network.cameraangle.C2SCameraAngleSetEntityPose;
import fr.loudo.narrativecraft.network.cameraangle.C2SCameraAngleTeleportToTemplate;
import fr.loudo.narrativecraft.network.cutscene.BiCutsceneEnter;
import fr.loudo.narrativecraft.network.cutscene.BiCutscenePlayHeadPacket;
import fr.loudo.narrativecraft.network.cutscene.C2SCutsceneControl;
import fr.loudo.narrativecraft.network.cutscene.C2SCutsceneSave;
import fr.loudo.narrativecraft.network.dialog.C2SEnterDialogEditor;
import fr.loudo.narrativecraft.network.inkAction.C2SInkActionFinished;
import fr.loudo.narrativecraft.network.interaction.BiInteractionEnter;
import fr.loudo.narrativecraft.network.interaction.C2SInteractionSave;
import fr.loudo.narrativecraft.network.mainScreen.BiMainScreenEnter;
import fr.loudo.narrativecraft.network.mainScreen.C2SMainScreenCaptureCharacter;
import fr.loudo.narrativecraft.network.mainScreen.C2SMainScreenRemovePlacement;
import fr.loudo.narrativecraft.network.mainScreen.C2SMainScreenSave;
import fr.loudo.narrativecraft.network.story.C2SChoiceSelected;
import fr.loudo.narrativecraft.network.story.C2SDialogueFinished;
import fr.loudo.narrativecraft.network.story.C2SPlayStitchStory;
import fr.loudo.narrativecraft.network.story.C2SPlayStory;
import fr.loudo.narrativecraft.network.story.C2SStopStory;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;

public class ServerPacketHandlerFabric {

    public static void handle() {
        ServerPlayNetworking.registerGlobalReceiver(BiSyncNarrativeEntryPacket.TYPE, (packet, context) -> {
            ServerPacketHandler.narrativeEntry(packet, context.player());
        });
        ServerPlayNetworking.registerGlobalReceiver(BiCutsceneEnter.TYPE, (packet, context) -> {
            ServerPacketHandler.cutsceneState(packet, context.player());
        });
        ServerPlayNetworking.registerGlobalReceiver(C2SCutsceneControl.TYPE, (packet, context) -> {
            ServerPacketHandler.cutsceneControl(packet, context.player());
        });
        ServerPlayNetworking.registerGlobalReceiver(C2SCutsceneSave.TYPE, (packet, context) -> {
            ServerPacketHandler.cutsceneSave(packet, context.player());
        });
        ServerPlayNetworking.registerGlobalReceiver(BiCutscenePlayHeadPacket.TYPE, (packet, context) -> {
            ServerPacketHandler.playHeadUpdate(packet, context.player());
        });
        ServerPlayNetworking.registerGlobalReceiver(BiCameraAngleEnter.TYPE, (packet, context) -> {
            ServerPacketHandler.cameraAngleEnter(packet, context.player());
        });
        ServerPlayNetworking.registerGlobalReceiver(BiStopEditorMaker.TYPE, (packet, context) -> {
            ServerPacketHandler.stopEditorMaker(packet, context.player());
        });
        ServerPlayNetworking.registerGlobalReceiver(C2SCameraAngleSave.TYPE, (packet, context) -> {
            ServerPacketHandler.cameraAngleSave(packet, context.player());
        });
        ServerPlayNetworking.registerGlobalReceiver(C2SCameraAngleCaptureCharacter.TYPE, (packet, context) -> {
            ServerPacketHandler.cameraAngleCaptureCharacter(packet, context.player());
        });
        ServerPlayNetworking.registerGlobalReceiver(C2SCameraAngleRemovePlacement.TYPE, (packet, context) -> {
            ServerPacketHandler.cameraAngleRemovePlacement(packet, context.player());
        });
        ServerPlayNetworking.registerGlobalReceiver(C2SCameraAngleTeleportToTemplate.TYPE, (packet, context) -> {
            ServerPacketHandler.cameraAngleTeleportToTemplate(packet, context.player());
        });
        ServerPlayNetworking.registerGlobalReceiver(C2SCameraAngleSetEntityPose.TYPE, (packet, context) -> {
            ServerPacketHandler.cameraAngleSetEntityPose(packet, context.player());
        });
        ServerPlayNetworking.registerGlobalReceiver(C2SCameraAngleAddTemplateReference.TYPE, (packet, context) -> {
            ServerPacketHandler.cameraAngleAddTemplateReference(packet, context.player());
        });
        ServerPlayNetworking.registerGlobalReceiver(BiInteractionEnter.TYPE, (packet, context) -> {
            ServerPacketHandler.interactionEnter(packet, context.player());
        });
        ServerPlayNetworking.registerGlobalReceiver(C2SInteractionSave.TYPE, (packet, context) -> {
            ServerPacketHandler.interactionSave(packet, context.player());
        });
        ServerPlayNetworking.registerGlobalReceiver(C2SInkActionFinished.TYPE, (packet, context) -> {
            ServerPacketHandler.inkActionFinished(packet, context.player());
        });
        ServerPlayNetworking.registerGlobalReceiver(C2SDialogueFinished.TYPE, (packet, context) -> {
            ServerPacketHandler.dialogueFinished(packet, context.player());
        });
        ServerPlayNetworking.registerGlobalReceiver(C2SChoiceSelected.TYPE, (packet, context) -> {
            ServerPacketHandler.choiceSelected(packet, context.player());
        });
        ServerPlayNetworking.registerGlobalReceiver(C2SPlayStitchStory.TYPE, (packet, context) -> {
            ServerPacketHandler.playStitch(packet, context.player());
        });
        ServerPlayNetworking.registerGlobalReceiver(BiMainScreenEnter.TYPE, (packet, context) -> {
            ServerPacketHandler.enterMainScreen(packet, context.player());
        });
        ServerPlayNetworking.registerGlobalReceiver(C2SMainScreenCaptureCharacter.TYPE, (packet, context) -> {
            ServerPacketHandler.mainScreenCaptureCharacter(packet, context.player());
        });
        ServerPlayNetworking.registerGlobalReceiver(C2SMainScreenSave.TYPE, (packet, context) -> {
            ServerPacketHandler.mainScreenSave(packet, context.player());
        });
        ServerPlayNetworking.registerGlobalReceiver(C2SMainScreenRemovePlacement.TYPE, (packet, context) -> {
            ServerPacketHandler.mainScreenRemovePlacement(packet, context.player());
        });
        ServerPlayNetworking.registerGlobalReceiver(C2SPlayStory.TYPE, (packet, context) -> {
            ServerPacketHandler.playStory(packet, context.player());
        });
        ServerPlayNetworking.registerGlobalReceiver(C2SEnterDialogEditor.TYPE, (packet, context) -> {
            ServerPacketHandler.enterDialogEditor(packet, context.player());
        });
        ServerPlayNetworking.registerGlobalReceiver(C2SStopStory.TYPE, (packet, context) -> {
            ServerPacketHandler.stopStory(packet, context.player());
        });
    }
}
