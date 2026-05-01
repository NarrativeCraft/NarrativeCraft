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

import fr.loudo.narrativecraft.network.cameraangle.C2SCameraAngleCaptureCharacter;
import fr.loudo.narrativecraft.network.cameraangle.C2SCameraAngleControl;
import fr.loudo.narrativecraft.network.cameraangle.C2SCameraAngleEnter;
import fr.loudo.narrativecraft.network.cameraangle.C2SCameraAngleRemovePlacement;
import fr.loudo.narrativecraft.network.cameraangle.C2SCameraAngleSave;
import fr.loudo.narrativecraft.network.cameraangle.C2SCameraAngleTeleportToTemplate;
import fr.loudo.narrativecraft.network.cutscene.BiCutsceneEnter;
import fr.loudo.narrativecraft.network.cutscene.BiCutscenePlayHeadPacket;
import fr.loudo.narrativecraft.network.cutscene.C2SCutsceneControl;
import fr.loudo.narrativecraft.network.cutscene.C2SCutsceneSave;
import fr.loudo.narrativecraft.network.inkAction.C2SInkActionFinished;
import fr.loudo.narrativecraft.network.interaction.C2SInteractionEnter;
import fr.loudo.narrativecraft.network.interaction.C2SInteractionSave;
import fr.loudo.narrativecraft.network.story.C2SChoiceSelected;
import fr.loudo.narrativecraft.network.story.C2SDialogueFinished;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;

public class ServerPacketRegisterFabric {

    public static void register() {
        PayloadTypeRegistry.serverboundPlay().register(BiCutsceneEnter.TYPE, BiCutsceneEnter.STREAM_CODEC);
        PayloadTypeRegistry.serverboundPlay().register(C2SCutsceneControl.TYPE, C2SCutsceneControl.STREAM_CODEC);
        PayloadTypeRegistry.serverboundPlay().register(C2SCutsceneSave.TYPE, C2SCutsceneSave.STREAM_CODEC);
        PayloadTypeRegistry.serverboundPlay()
                .register(BiCutscenePlayHeadPacket.TYPE, BiCutscenePlayHeadPacket.STREAM_CODEC);
        PayloadTypeRegistry.serverboundPlay().register(C2SCameraAngleEnter.TYPE, C2SCameraAngleEnter.STREAM_CODEC);
        PayloadTypeRegistry.serverboundPlay().register(C2SCameraAngleControl.TYPE, C2SCameraAngleControl.STREAM_CODEC);
        PayloadTypeRegistry.serverboundPlay().register(C2SCameraAngleSave.TYPE, C2SCameraAngleSave.STREAM_CODEC);
        PayloadTypeRegistry.serverboundPlay()
                .register(C2SCameraAngleCaptureCharacter.TYPE, C2SCameraAngleCaptureCharacter.STREAM_CODEC);
        PayloadTypeRegistry.serverboundPlay()
                .register(C2SCameraAngleRemovePlacement.TYPE, C2SCameraAngleRemovePlacement.STREAM_CODEC);
        PayloadTypeRegistry.serverboundPlay()
                .register(C2SCameraAngleTeleportToTemplate.TYPE, C2SCameraAngleTeleportToTemplate.STREAM_CODEC);
        PayloadTypeRegistry.serverboundPlay().register(C2SInteractionEnter.TYPE, C2SInteractionEnter.STREAM_CODEC);
        PayloadTypeRegistry.serverboundPlay().register(C2SInteractionSave.TYPE, C2SInteractionSave.STREAM_CODEC);
        PayloadTypeRegistry.serverboundPlay().register(C2SInkActionFinished.TYPE, C2SInkActionFinished.STREAM_CODEC);
        PayloadTypeRegistry.serverboundPlay().register(C2SDialogueFinished.TYPE, C2SDialogueFinished.STREAM_CODEC);
        PayloadTypeRegistry.serverboundPlay().register(C2SChoiceSelected.TYPE, C2SChoiceSelected.STREAM_CODEC);
    }
}
