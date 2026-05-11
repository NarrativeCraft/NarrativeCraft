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

import fr.loudo.narrativecraft.network.BiSyncNarrativeEntryPacket;
import fr.loudo.narrativecraft.network.cameraangle.BiCameraAngleEnter;
import fr.loudo.narrativecraft.network.cameraangle.C2SCameraAngleCaptureCharacter;
import fr.loudo.narrativecraft.network.cameraangle.C2SCameraAngleControl;
import fr.loudo.narrativecraft.network.cameraangle.C2SCameraAngleRemovePlacement;
import fr.loudo.narrativecraft.network.cameraangle.C2SCameraAngleSave;
import fr.loudo.narrativecraft.network.cameraangle.C2SCameraAngleTeleportToTemplate;
import fr.loudo.narrativecraft.network.cutscene.BiCutsceneEnter;
import fr.loudo.narrativecraft.network.cutscene.BiCutscenePlayHeadPacket;
import fr.loudo.narrativecraft.network.cutscene.C2SCutsceneControl;
import fr.loudo.narrativecraft.network.cutscene.C2SCutsceneSave;
import fr.loudo.narrativecraft.network.inkAction.C2SInkActionFinished;
import fr.loudo.narrativecraft.network.interaction.BiInteractionEnter;
import fr.loudo.narrativecraft.network.interaction.C2SInteractionSave;
import fr.loudo.narrativecraft.network.story.C2SChoiceSelected;
import fr.loudo.narrativecraft.network.story.C2SDialogueFinished;
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
        ServerPlayNetworking.registerGlobalReceiver(C2SCameraAngleControl.TYPE, (packet, context) -> {
            ServerPacketHandler.cameraAngleControl(packet, context.player());
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
    }
}
