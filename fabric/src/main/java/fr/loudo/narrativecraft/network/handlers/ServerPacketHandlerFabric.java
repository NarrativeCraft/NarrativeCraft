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

import fr.loudo.narrativecraft.network.BiStopEditorMaker;
import fr.loudo.narrativecraft.network.BiSyncNarrativeEntryPacket;
import fr.loudo.narrativecraft.network.C2SChangeGamemodePacket;
import fr.loudo.narrativecraft.network.cameraangle.BiCameraAngleEnter;
import fr.loudo.narrativecraft.network.cameraangle.C2SCameraAngleAddTemplateReference;
import fr.loudo.narrativecraft.network.cameraangle.C2SCameraAngleCaptureCharacter;
import fr.loudo.narrativecraft.network.cameraangle.C2SCameraAngleRemovePlacement;
import fr.loudo.narrativecraft.network.cameraangle.C2SCameraAngleRemoveTemplateReference;
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
import fr.loudo.narrativecraft.network.story.C2SSetStoryLocale;
import fr.loudo.narrativecraft.network.story.C2SStopStory;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;

public class ServerPacketHandlerFabric {

    public static void handle() {
        ServerPlayNetworking.registerGlobalReceiver(
                BiSyncNarrativeEntryPacket.TYPE, (server, player, handler, buf, responseSender) -> {
                    BiSyncNarrativeEntryPacket packet = BiSyncNarrativeEntryPacket.read(buf);
                    server.execute(() -> {
                        ServerPacketHandler.narrativeEntry(packet, player);
                    });
                });
        ServerPlayNetworking.registerGlobalReceiver(
                BiCutsceneEnter.TYPE, (server, player, handler, buf, responseSender) -> {
                    BiCutsceneEnter packet = BiCutsceneEnter.read(buf);
                    server.execute(() -> {
                        ServerPacketHandler.cutsceneState(packet, player);
                    });
                });
        ServerPlayNetworking.registerGlobalReceiver(
                C2SCutsceneControl.TYPE, (server, player, handler, buf, responseSender) -> {
                    C2SCutsceneControl packet = C2SCutsceneControl.read(buf);
                    server.execute(() -> {
                        ServerPacketHandler.cutsceneControl(packet, player);
                    });
                });
        ServerPlayNetworking.registerGlobalReceiver(
                C2SCutsceneSave.TYPE, (server, player, handler, buf, responseSender) -> {
                    C2SCutsceneSave packet = C2SCutsceneSave.read(buf);
                    server.execute(() -> {
                        ServerPacketHandler.cutsceneSave(packet, player);
                    });
                });
        ServerPlayNetworking.registerGlobalReceiver(
                BiCutscenePlayHeadPacket.TYPE, (server, player, handler, buf, responseSender) -> {
                    BiCutscenePlayHeadPacket packet = BiCutscenePlayHeadPacket.read(buf);
                    server.execute(() -> {
                        ServerPacketHandler.playHeadUpdate(packet, player);
                    });
                });
        ServerPlayNetworking.registerGlobalReceiver(
                BiCameraAngleEnter.TYPE, (server, player, handler, buf, responseSender) -> {
                    BiCameraAngleEnter packet = BiCameraAngleEnter.read(buf);
                    server.execute(() -> {
                        ServerPacketHandler.cameraAngleEnter(packet, player);
                    });
                });
        ServerPlayNetworking.registerGlobalReceiver(
                BiStopEditorMaker.TYPE, (server, player, handler, buf, responseSender) -> {
                    BiStopEditorMaker packet = BiStopEditorMaker.read(buf);
                    server.execute(() -> {
                        ServerPacketHandler.stopEditorMaker(packet, player);
                    });
                });
        ServerPlayNetworking.registerGlobalReceiver(
                C2SCameraAngleSave.TYPE, (server, player, handler, buf, responseSender) -> {
                    C2SCameraAngleSave packet = C2SCameraAngleSave.read(buf);
                    server.execute(() -> {
                        ServerPacketHandler.cameraAngleSave(packet, player);
                    });
                });
        ServerPlayNetworking.registerGlobalReceiver(
                C2SCameraAngleCaptureCharacter.TYPE, (server, player, handler, buf, responseSender) -> {
                    C2SCameraAngleCaptureCharacter packet = C2SCameraAngleCaptureCharacter.read(buf);
                    server.execute(() -> {
                        ServerPacketHandler.cameraAngleCaptureCharacter(packet, player);
                    });
                });
        ServerPlayNetworking.registerGlobalReceiver(
                C2SCameraAngleRemovePlacement.TYPE, (server, player, handler, buf, responseSender) -> {
                    C2SCameraAngleRemovePlacement packet = C2SCameraAngleRemovePlacement.read(buf);
                    server.execute(() -> {
                        ServerPacketHandler.cameraAngleRemovePlacement(packet, player);
                    });
                });
        ServerPlayNetworking.registerGlobalReceiver(
                C2SCameraAngleTeleportToTemplate.TYPE, (server, player, handler, buf, responseSender) -> {
                    C2SCameraAngleTeleportToTemplate packet = C2SCameraAngleTeleportToTemplate.read(buf);
                    server.execute(() -> {
                        ServerPacketHandler.cameraAngleTeleportToTemplate(packet, player);
                    });
                });
        ServerPlayNetworking.registerGlobalReceiver(
                C2SCameraAngleSetEntityPose.TYPE, (server, player, handler, buf, responseSender) -> {
                    C2SCameraAngleSetEntityPose packet = C2SCameraAngleSetEntityPose.read(buf);
                    server.execute(() -> {
                        ServerPacketHandler.cameraAngleSetEntityPose(packet, player);
                    });
                });
        ServerPlayNetworking.registerGlobalReceiver(
                C2SCameraAngleAddTemplateReference.TYPE, (server, player, handler, buf, responseSender) -> {
                    C2SCameraAngleAddTemplateReference packet = C2SCameraAngleAddTemplateReference.read(buf);
                    server.execute(() -> {
                        ServerPacketHandler.cameraAngleAddTemplateReference(packet, player);
                    });
                });
        ServerPlayNetworking.registerGlobalReceiver(
                C2SCameraAngleRemoveTemplateReference.TYPE, (server, player, handler, buf, responseSender) -> {
                    C2SCameraAngleRemoveTemplateReference packet = C2SCameraAngleRemoveTemplateReference.read(buf);
                    server.execute(() -> {
                        ServerPacketHandler.cameraAngleRemoveTemplateReference(packet, player);
                    });
                });
        ServerPlayNetworking.registerGlobalReceiver(
                BiInteractionEnter.TYPE, (server, player, handler, buf, responseSender) -> {
                    BiInteractionEnter packet = BiInteractionEnter.read(buf);
                    server.execute(() -> {
                        ServerPacketHandler.interactionEnter(packet, player);
                    });
                });
        ServerPlayNetworking.registerGlobalReceiver(
                C2SInteractionSave.TYPE, (server, player, handler, buf, responseSender) -> {
                    C2SInteractionSave packet = C2SInteractionSave.read(buf);
                    server.execute(() -> {
                        ServerPacketHandler.interactionSave(packet, player);
                    });
                });
        ServerPlayNetworking.registerGlobalReceiver(
                C2SInkActionFinished.TYPE, (server, player, handler, buf, responseSender) -> {
                    C2SInkActionFinished packet = C2SInkActionFinished.read(buf);
                    server.execute(() -> {
                        ServerPacketHandler.inkActionFinished(packet, player);
                    });
                });
        ServerPlayNetworking.registerGlobalReceiver(
                C2SDialogueFinished.TYPE, (server, player, handler, buf, responseSender) -> {
                    C2SDialogueFinished packet = C2SDialogueFinished.read(buf);
                    server.execute(() -> {
                        ServerPacketHandler.dialogueFinished(packet, player);
                    });
                });
        ServerPlayNetworking.registerGlobalReceiver(
                C2SChoiceSelected.TYPE, (server, player, handler, buf, responseSender) -> {
                    C2SChoiceSelected packet = C2SChoiceSelected.read(buf);
                    server.execute(() -> {
                        ServerPacketHandler.choiceSelected(packet, player);
                    });
                });
        ServerPlayNetworking.registerGlobalReceiver(
                C2SPlayStitchStory.TYPE, (server, player, handler, buf, responseSender) -> {
                    C2SPlayStitchStory packet = C2SPlayStitchStory.read(buf);
                    server.execute(() -> {
                        ServerPacketHandler.playStitch(packet, player);
                    });
                });
        ServerPlayNetworking.registerGlobalReceiver(
                BiMainScreenEnter.TYPE, (server, player, handler, buf, responseSender) -> {
                    BiMainScreenEnter packet = BiMainScreenEnter.read(buf);
                    server.execute(() -> {
                        ServerPacketHandler.enterMainScreen(packet, player);
                    });
                });
        ServerPlayNetworking.registerGlobalReceiver(
                C2SMainScreenCaptureCharacter.TYPE, (server, player, handler, buf, responseSender) -> {
                    C2SMainScreenCaptureCharacter packet = C2SMainScreenCaptureCharacter.read(buf);
                    server.execute(() -> {
                        ServerPacketHandler.mainScreenCaptureCharacter(packet, player);
                    });
                });
        ServerPlayNetworking.registerGlobalReceiver(
                C2SMainScreenSave.TYPE, (server, player, handler, buf, responseSender) -> {
                    C2SMainScreenSave packet = C2SMainScreenSave.read(buf);
                    server.execute(() -> {
                        ServerPacketHandler.mainScreenSave(packet, player);
                    });
                });
        ServerPlayNetworking.registerGlobalReceiver(
                C2SMainScreenRemovePlacement.TYPE, (server, player, handler, buf, responseSender) -> {
                    C2SMainScreenRemovePlacement packet = C2SMainScreenRemovePlacement.read(buf);
                    server.execute(() -> {
                        ServerPacketHandler.mainScreenRemovePlacement(packet, player);
                    });
                });
        ServerPlayNetworking.registerGlobalReceiver(
                C2SPlayStory.TYPE, (server, player, handler, buf, responseSender) -> {
                    C2SPlayStory packet = C2SPlayStory.read(buf);
                    server.execute(() -> {
                        ServerPacketHandler.playStory(packet, player);
                    });
                });
        ServerPlayNetworking.registerGlobalReceiver(
                C2SEnterDialogEditor.TYPE, (server, player, handler, buf, responseSender) -> {
                    C2SEnterDialogEditor packet = C2SEnterDialogEditor.read(buf);
                    server.execute(() -> {
                        ServerPacketHandler.enterDialogEditor(packet, player);
                    });
                });
        ServerPlayNetworking.registerGlobalReceiver(
                C2SStopStory.TYPE, (server, player, handler, buf, responseSender) -> {
                    C2SStopStory packet = C2SStopStory.read(buf);
                    server.execute(() -> {
                        ServerPacketHandler.stopStory(packet, player);
                    });
                });
        ServerPlayNetworking.registerGlobalReceiver(
                C2SChangeGamemodePacket.TYPE, (server, player, handler, buf, responseSender) -> {
                    C2SChangeGamemodePacket packet = C2SChangeGamemodePacket.read(buf);
                    server.execute(() -> {
                        ServerPacketHandler.changeGamemode(packet, player);
                    });
                });
        ServerPlayNetworking.registerGlobalReceiver(
                C2SSetStoryLocale.TYPE, (server, player, handler, buf, responseSender) -> {
                    C2SSetStoryLocale packet = C2SSetStoryLocale.read(buf);
                    server.execute(() -> {
                        ServerPacketHandler.setStoryLocale(packet, player);
                    });
                });
    }
}
