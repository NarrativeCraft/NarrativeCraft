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
import fr.loudo.narrativecraft.network.cameraangle.C2SCameraAngleAddTemplateReference;
import fr.loudo.narrativecraft.network.cameraangle.C2SCameraAngleCaptureCharacter;
import fr.loudo.narrativecraft.network.cameraangle.C2SCameraAngleControl;
import fr.loudo.narrativecraft.network.cameraangle.C2SCameraAngleRemovePlacement;
import fr.loudo.narrativecraft.network.cameraangle.C2SCameraAngleRemoveTemplateReference;
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
import fr.loudo.narrativecraft.network.story.C2SPlayStitchStory;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class ServerPacketHandlerNeoForge {

    public static void syncNarrativeEntry(BiSyncNarrativeEntryPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            ServerPacketHandler.narrativeEntry(packet, context.player());
        });
    }

    public static void cutsceneState(BiCutsceneEnter packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            ServerPacketHandler.cutsceneState(packet, context.player());
        });
    }

    public static void cutsceneControl(C2SCutsceneControl packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            ServerPacketHandler.cutsceneControl(packet, context.player());
        });
    }

    public static void cutsceneSave(C2SCutsceneSave packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            ServerPacketHandler.cutsceneSave(packet, context.player());
        });
    }

    public static void playHeadUpdate(BiCutscenePlayHeadPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            ServerPacketHandler.playHeadUpdate(packet, context.player());
        });
    }

    public static void cameraAngleEnter(BiCameraAngleEnter packet, IPayloadContext context) {
        context.enqueueWork(() -> ServerPacketHandler.cameraAngleEnter(packet, context.player()));
    }

    public static void cameraAngleControl(C2SCameraAngleControl packet, IPayloadContext context) {
        context.enqueueWork(() -> ServerPacketHandler.cameraAngleControl(packet, context.player()));
    }

    public static void cameraAngleSave(C2SCameraAngleSave packet, IPayloadContext context) {
        context.enqueueWork(() -> ServerPacketHandler.cameraAngleSave(packet, context.player()));
    }

    public static void cameraAngleCaptureCharacter(C2SCameraAngleCaptureCharacter packet, IPayloadContext context) {
        context.enqueueWork(() -> ServerPacketHandler.cameraAngleCaptureCharacter(packet, context.player()));
    }

    public static void cameraAngleRemovePlacement(C2SCameraAngleRemovePlacement packet, IPayloadContext context) {
        context.enqueueWork(() -> ServerPacketHandler.cameraAngleRemovePlacement(packet, context.player()));
    }

    public static void cameraAngleAddTemplateReference(
            C2SCameraAngleAddTemplateReference packet, IPayloadContext context) {
        context.enqueueWork(() -> ServerPacketHandler.cameraAngleAddTemplateReference(packet, context.player()));
    }

    public static void cameraAngleRemoveTemplateReference(
            C2SCameraAngleRemoveTemplateReference packet, IPayloadContext context) {
        context.enqueueWork(() -> ServerPacketHandler.cameraAngleRemoveTemplateReference(packet, context.player()));
    }

    public static void cameraAngleTeleportToTemplate(C2SCameraAngleTeleportToTemplate packet, IPayloadContext context) {
        context.enqueueWork(() -> ServerPacketHandler.cameraAngleTeleportToTemplate(packet, context.player()));
    }

    public static void interactionEnter(BiInteractionEnter packet, IPayloadContext context) {
        context.enqueueWork(() -> ServerPacketHandler.interactionEnter(packet, context.player()));
    }

    public static void interactionSave(C2SInteractionSave packet, IPayloadContext context) {
        context.enqueueWork(() -> ServerPacketHandler.interactionSave(packet, context.player()));
    }

    public static void inkActionFinished(C2SInkActionFinished packet, IPayloadContext context) {
        context.enqueueWork(() -> ServerPacketHandler.inkActionFinished(packet, context.player()));
    }

    public static void dialogueFinished(C2SDialogueFinished packet, IPayloadContext context) {
        context.enqueueWork(() -> ServerPacketHandler.dialogueFinished(packet, context.player()));
    }

    public static void choiceSelected(C2SChoiceSelected packet, IPayloadContext context) {
        context.enqueueWork(() -> ServerPacketHandler.choiceSelected(packet, context.player()));
    }

    public static void playStitch(C2SPlayStitchStory packet, IPayloadContext context) {
        context.enqueueWork(() -> ServerPacketHandler.playStitch(packet, context.player()));
    }
}
