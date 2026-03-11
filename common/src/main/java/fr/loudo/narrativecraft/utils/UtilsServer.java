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

package fr.loudo.narrativecraft.utils;

import fr.loudo.narrativecraft.NarrativeCraftMod;
import fr.loudo.narrativecraft.managers.PlayerSessionManager;
import fr.loudo.narrativecraft.network.S2CScreenClear;
import fr.loudo.narrativecraft.platform.Services;
import fr.loudo.narrativecraft.session.PlayerSession;
import java.util.UUID;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;

public class UtilsServer {

    public static ServerPlayer getPlayerByUUID(UUID playerId) {
        return NarrativeCraftMod.getInstance().getServer().getPlayerList().getPlayer(playerId);
    }

    public static void broadcastPacket(CustomPacketPayload packet) {
        for (ServerPlayer player :
                NarrativeCraftMod.getInstance().getServer().getPlayerList().getPlayers()) {
            Services.PACKET.sendToPlayer(player, packet);
        }
    }

    public static void sendErrorClearScreen(Component message, ServerPlayer player) {
        if (player == null) return;
        Utils.sendError(message, player);
        Services.PACKET.sendToPlayer(player, S2CScreenClear.INSTANCE);
    }

    public static PlayerSession getPlayerSessionByPlayer(ServerPlayer player) {
        PlayerSessionManager playerSessionManager =
                NarrativeCraftMod.getInstance().getPlayerSessionManager();
        PlayerSession playerSession = playerSessionManager.getByPlayer(player);
        if (playerSession == null) {
            playerSession = new PlayerSession(player);
            playerSessionManager.add(playerSession);
        }
        return playerSession;
    }
}
