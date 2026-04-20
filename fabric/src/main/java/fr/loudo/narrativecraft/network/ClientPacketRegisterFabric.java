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

import fr.loudo.narrativecraft.network.cameraangle.S2CCameraAngleCharacterCaptured;
import fr.loudo.narrativecraft.network.cameraangle.S2CCameraAngleEditorData;
import fr.loudo.narrativecraft.network.cameraangle.S2CCameraAnglePlacementEntitySpawned;
import fr.loudo.narrativecraft.network.cutscene.BiCutscenePlayHeadPacket;
import fr.loudo.narrativecraft.network.cutscene.S2CCutsceneEditorData;
import fr.loudo.narrativecraft.network.dialog.S2CDialogTest;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;

public class ClientPacketRegisterFabric {

    public static void register() {
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
    }
}
