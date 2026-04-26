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
import fr.loudo.narrativecraft.network.S2CNarrativeDataClear;
import fr.loudo.narrativecraft.network.S2CPlayerSession;
import fr.loudo.narrativecraft.network.S2CScreenClear;
import fr.loudo.narrativecraft.network.S2CToastMessage;
import fr.loudo.narrativecraft.network.cameraangle.S2CCameraAngleCharacterCaptured;
import fr.loudo.narrativecraft.network.cameraangle.S2CCameraAngleEditorData;
import fr.loudo.narrativecraft.network.cameraangle.S2CCameraAnglePlacementEntitySpawned;
import fr.loudo.narrativecraft.network.cutscene.BiCutscenePlayHeadPacket;
import fr.loudo.narrativecraft.network.cutscene.S2CCutsceneEditorData;
import fr.loudo.narrativecraft.network.dialog.S2CDialogTest;
import fr.loudo.narrativecraft.network.interaction.S2CInteractionEditorData;
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

    public static void loadInteractionEditorData(S2CInteractionEditorData packet, IPayloadContext context) {
        context.enqueueWork(() -> ClientPacketHandler.loadInteractionEditorData(packet));
    }
}
