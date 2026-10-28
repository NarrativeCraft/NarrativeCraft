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
import fr.loudo.narrativecraft.client.network.C2SRequestTree;
import fr.loudo.narrativecraft.client.network.C2SStudioToggle;
import fr.loudo.narrativecraft.client.network.handlers.ClientPacketHandler;
import fr.loudo.narrativecraft.network.handlers.ServerPacketHandler;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

public class ServerPacketRegisterNeoForge {

    public static void register(PayloadRegistrar registrar) {
        registrar.playToServer(
                fr.loudo.narrativecraft.client.network.C2SFreezeWorld.TYPE,
                C2SFreezeWorld.STREAM_CODEC,
                (packet, context) -> ServerPacketHandler.freezeWorld(packet, (ServerPlayer) context.player()));
        registrar.playToServer(
                C2SStudioToggle.TYPE,
                C2SStudioToggle.STREAM_CODEC,
                (packet, context) -> ServerPacketHandler.studioToggle(packet, (ServerPlayer) context.player()));
        registrar.playToServer(
                C2SRequestTree.TYPE,
                C2SRequestTree.STREAM_CODEC,
                (packet, context) -> ServerPacketHandler.requestTree(packet, (ServerPlayer) context.player()));
        registrar.playToClient(
                S2CInitTree.TYPE, S2CInitTree.STREAM_CODEC, (packet, context) -> ClientPacketHandler.initTree(packet));
    }
}
