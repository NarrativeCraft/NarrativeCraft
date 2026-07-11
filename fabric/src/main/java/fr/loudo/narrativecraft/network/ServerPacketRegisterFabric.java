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

package fr.loudo.narrativecraft.network;

import fr.loudo.narrativecraft.network.cameraangle.*;
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
import fr.loudo.narrativecraft.network.story.C2SSetStoryLocale;
import fr.loudo.narrativecraft.network.story.C2SStopStory;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;

public class ServerPacketRegisterFabric {

    public static void register() {
        PayloadTypeRegistry.playC2S().register(BiCutsceneEnter.TYPE, BiCutsceneEnter.STREAM_CODEC);
        PayloadTypeRegistry.playC2S().register(C2SCutsceneControl.TYPE, C2SCutsceneControl.STREAM_CODEC);
        PayloadTypeRegistry.playC2S().register(C2SCutsceneSave.TYPE, C2SCutsceneSave.STREAM_CODEC);
        PayloadTypeRegistry.playC2S().register(BiCutscenePlayHeadPacket.TYPE, BiCutscenePlayHeadPacket.STREAM_CODEC);
        PayloadTypeRegistry.playC2S().register(BiCameraAngleEnter.TYPE, BiCameraAngleEnter.STREAM_CODEC);
        PayloadTypeRegistry.playC2S().register(BiStopEditorMaker.TYPE, BiStopEditorMaker.STREAM_CODEC);
        PayloadTypeRegistry.playC2S().register(C2SCameraAngleSave.TYPE, C2SCameraAngleSave.STREAM_CODEC);
        PayloadTypeRegistry.playC2S()
                .register(C2SCameraAngleCaptureCharacter.TYPE, C2SCameraAngleCaptureCharacter.STREAM_CODEC);
        PayloadTypeRegistry.playC2S()
                .register(C2SCameraAngleRemovePlacement.TYPE, C2SCameraAngleRemovePlacement.STREAM_CODEC);
        PayloadTypeRegistry.playC2S()
                .register(C2SCameraAngleTeleportToTemplate.TYPE, C2SCameraAngleTeleportToTemplate.STREAM_CODEC);
        PayloadTypeRegistry.playC2S()
                .register(C2SCameraAngleSetEntityPose.TYPE, C2SCameraAngleSetEntityPose.STREAM_CODEC);
        PayloadTypeRegistry.playC2S()
                .register(C2SCameraAngleAddTemplateReference.TYPE, C2SCameraAngleAddTemplateReference.STREAM_CODEC);
        PayloadTypeRegistry.playC2S()
                .register(
                        C2SCameraAngleRemoveTemplateReference.TYPE, C2SCameraAngleRemoveTemplateReference.STREAM_CODEC);
        PayloadTypeRegistry.playC2S().register(BiInteractionEnter.TYPE, BiInteractionEnter.STREAM_CODEC);
        PayloadTypeRegistry.playC2S().register(C2SInteractionSave.TYPE, C2SInteractionSave.STREAM_CODEC);
        PayloadTypeRegistry.playC2S().register(C2SInkActionFinished.TYPE, C2SInkActionFinished.STREAM_CODEC);
        PayloadTypeRegistry.playC2S().register(C2SDialogueFinished.TYPE, C2SDialogueFinished.STREAM_CODEC);
        PayloadTypeRegistry.playC2S().register(C2SChoiceSelected.TYPE, C2SChoiceSelected.STREAM_CODEC);
        PayloadTypeRegistry.playC2S().register(C2SPlayStitchStory.TYPE, C2SPlayStitchStory.STREAM_CODEC);
        PayloadTypeRegistry.playC2S().register(BiMainScreenEnter.TYPE, BiMainScreenEnter.STREAM_CODEC);
        PayloadTypeRegistry.playC2S()
                .register(C2SMainScreenCaptureCharacter.TYPE, C2SMainScreenCaptureCharacter.STREAM_CODEC);
        PayloadTypeRegistry.playC2S().register(C2SMainScreenSave.TYPE, C2SMainScreenSave.STREAM_CODEC);
        PayloadTypeRegistry.playC2S()
                .register(C2SMainScreenRemovePlacement.TYPE, C2SMainScreenRemovePlacement.STREAM_CODEC);
        PayloadTypeRegistry.playC2S().register(C2SEnterDialogEditor.TYPE, C2SEnterDialogEditor.STREAM_CODEC);
        PayloadTypeRegistry.playC2S().register(C2SStopStory.TYPE, C2SStopStory.STREAM_CODEC);
        PayloadTypeRegistry.playC2S().register(C2SChangeGamemodePacket.TYPE, C2SChangeGamemodePacket.STREAM_CODEC);
        PayloadTypeRegistry.playC2S().register(C2SSetStoryLocale.TYPE, C2SSetStoryLocale.STREAM_CODEC);
    }
}
