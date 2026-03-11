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

package fr.loudo.narrativecraft.events.server;

import fr.loudo.narrativecraft.NarrativeCraftMod;
import fr.loudo.narrativecraft.network.BiSyncNarrativeEntryPacket;
import fr.loudo.narrativecraft.network.S2CNarrativeDataClear;
import fr.loudo.narrativecraft.network.S2CScreenClear;
import fr.loudo.narrativecraft.network.handlers.ClientPacketHandlerNeoForge;
import fr.loudo.narrativecraft.network.handlers.ServerPacketHandlerNeoForge;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

@Mod(NarrativeCraftMod.MOD_ID)
public class OnPacketRegisterEventNeoForge {

    public OnPacketRegisterEventNeoForge(IEventBus modBus) {
        modBus.addListener(OnPacketRegisterEventNeoForge::onPacketRegister);
    }

    private static void onPacketRegister(RegisterPayloadHandlersEvent event) {
        final PayloadRegistrar registrar = event.registrar("1");
        registerS2CPackets(registrar);
    }

    private static void registerS2CPackets(PayloadRegistrar registrar) {
        registrar.playBidirectional(
                BiSyncNarrativeEntryPacket.TYPE,
                BiSyncNarrativeEntryPacket.STREAM_CODEC,
                ServerPacketHandlerNeoForge::syncNarrativeEntry);
        registrar.playToClient(
                S2CNarrativeDataClear.TYPE,
                S2CNarrativeDataClear.STREAM_CODEC,
                ClientPacketHandlerNeoForge::clearNarrativeData);
        registrar.playToClient(
                S2CScreenClear.TYPE, S2CScreenClear.STREAM_CODEC, ClientPacketHandlerNeoForge::clearScreen);
    }
}
