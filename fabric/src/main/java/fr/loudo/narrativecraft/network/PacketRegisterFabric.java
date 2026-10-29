/*
 * NarrativeCraft - Create narrative games inside Minecraft. No coding, no game engine, only text and logic.
 * Copyright (c) 2026 LOUDO and contributors
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

import fr.loudo.narrativecraft.client.network.C2SFreezeWorld;
import fr.loudo.narrativecraft.client.network.C2SRequestOpenFile;
import fr.loudo.narrativecraft.client.network.C2SRequestTree;
import fr.loudo.narrativecraft.client.network.C2SStudioToggle;
import fr.loudo.narrativecraft.client.network.handlers.ClientPacketHandler;
import fr.loudo.narrativecraft.network.handlers.ServerPacketHandler;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;

public class PacketRegisterFabric {

    public static void serverRegister() {
        PayloadTypeRegistry.serverboundPlay()
                .register(
                        fr.loudo.narrativecraft.client.network.C2SFreezeWorld.TYPE,
                        fr.loudo.narrativecraft.client.network.C2SFreezeWorld.STREAM_CODEC);
        ServerPlayNetworking.registerGlobalReceiver(
                C2SFreezeWorld.TYPE, (packet, context) -> ServerPacketHandler.freezeWorld(packet, context.player()));

        PayloadTypeRegistry.serverboundPlay().register(C2SStudioToggle.TYPE, C2SStudioToggle.STREAM_CODEC);
        ServerPlayNetworking.registerGlobalReceiver(
                C2SStudioToggle.TYPE, (packet, context) -> ServerPacketHandler.studioToggle(packet, context.player()));

        PayloadTypeRegistry.serverboundPlay().register(C2SRequestTree.TYPE, C2SRequestTree.STREAM_CODEC);
        ServerPlayNetworking.registerGlobalReceiver(
                C2SRequestTree.TYPE, (packet, context) -> ServerPacketHandler.requestTree(packet, context.player()));

        PayloadTypeRegistry.serverboundPlay().register(C2SRequestOpenFile.TYPE, C2SRequestOpenFile.STREAM_CODEC);
        ServerPlayNetworking.registerGlobalReceiver(
                C2SRequestOpenFile.TYPE,
                (packet, context) -> ServerPacketHandler.requestOpenFile(packet, context.player()));

        PayloadTypeRegistry.clientboundPlay().registerLarge(S2CInitTree.TYPE, S2CInitTree.STREAM_CODEC, 5_242_880);
        PayloadTypeRegistry.clientboundPlay()
                .registerLarge(S2COpenScene.TYPE, S2COpenScene.STREAM_CODEC, S2COpenScene.MAX_SIZE);
    }

    public static void clientRegister() {
        ClientPlayNetworking.registerGlobalReceiver(
                S2CInitTree.TYPE, (packet, context) -> ClientPacketHandler.initTree(packet));
        ClientPlayNetworking.registerGlobalReceiver(
                S2COpenScene.TYPE, (packet, context) -> ClientPacketHandler.openScene(packet));
    }
}
