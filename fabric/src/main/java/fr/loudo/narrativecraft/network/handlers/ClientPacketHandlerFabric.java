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
import fr.loudo.narrativecraft.network.cutscene.BiCutscenePlayHeadPacket;
import fr.loudo.narrativecraft.network.cutscene.S2CCutsceneEditorData;
import fr.loudo.narrativecraft.network.dialog.S2CDialogTest;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

public class ClientPacketHandlerFabric {

    public static void handle() {
        ClientPlayNetworking.registerGlobalReceiver(BiSyncNarrativeEntryPacket.TYPE, (packet, context) -> {
            ClientPacketHandler.narrativeEntry(packet);
        });
        ClientPlayNetworking.registerGlobalReceiver(S2CNarrativeDataClear.TYPE, (packet, context) -> {
            ClientPacketHandler.clearNarrativeData();
        });
        ClientPlayNetworking.registerGlobalReceiver(S2CScreenClear.TYPE, (packet, context) -> {
            ClientPacketHandler.clearScreen();
        });
        ClientPlayNetworking.registerGlobalReceiver(S2CPlayerSession.TYPE, (packet, context) -> {
            ClientPacketHandler.setSession(packet);
        });
        ClientPlayNetworking.registerGlobalReceiver(S2CToastMessage.TYPE, (packet, context) -> {
            ClientPacketHandler.showToast(packet);
        });
        ClientPlayNetworking.registerGlobalReceiver(S2CCutsceneEditorData.TYPE, (packet, context) -> {
            ClientPacketHandler.loadCutsceneEditorData(packet);
        });
        ClientPlayNetworking.registerGlobalReceiver(BiCutscenePlayHeadPacket.TYPE, (packet, context) -> {
            ClientPacketHandler.updatePlayHeadCutscene(packet);
        });
        ClientPlayNetworking.registerGlobalReceiver(S2CDialogTest.TYPE, (packet, context) -> {
            ClientPacketHandler.handleDialogTest(packet);
        });
    }
}
