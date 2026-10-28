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

package fr.loudo.narrativecraft.network.handlers;

import com.google.common.hash.Hashing;
import fr.loudo.narrativecraft.client.network.C2SFreezeWorld;
import fr.loudo.narrativecraft.client.network.C2SRequestTree;
import fr.loudo.narrativecraft.client.network.C2SStudioToggle;
import fr.loudo.narrativecraft.files.NarrativeFiles;
import fr.loudo.narrativecraft.network.S2CInitTree;
import fr.loudo.narrativecraft.platform.Services;
import fr.loudo.narrativecraft.studio.NarrativeStudio;
import fr.loudo.narrativecraft.studio.assets.TreeIO;
import java.nio.charset.StandardCharsets;
import net.minecraft.server.ServerTickRateManager;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.permissions.Permissions;

public class ServerPacketHandler {

    public static void freezeWorld(C2SFreezeWorld packet, ServerPlayer player) {
        if (!player.permissions().hasPermission(Permissions.COMMANDS_MODERATOR)) return;
        ServerTickRateManager tickRateManager = player.level().getServer().tickRateManager();
        if (packet.state()) {
            if (tickRateManager.isSprinting()) tickRateManager.stopSprinting();
            if (tickRateManager.isSteppingForward()) tickRateManager.stopStepping();
        }
        tickRateManager.setFrozen(packet.state());
    }

    public static void studioToggle(C2SStudioToggle packet, ServerPlayer player) {
        if (!player.permissions().hasPermission(Permissions.COMMANDS_MODERATOR)) return;
        if (!packet.isEntering()) {
            NarrativeStudio.getInstance().removePlayer(player);
            return;
        }
        NarrativeStudio.getInstance().addPlayer(player);
    }

    public static void requestTree(C2SRequestTree packet, ServerPlayer player) {
        if (!player.permissions().hasPermission(Permissions.COMMANDS_MODERATOR)) return;
        String treeJson = TreeIO.serialize(
                        NarrativeFiles.getInstance().getAssets().generateTree())
                .toString();
        String assetsHash =
                Hashing.sha256().hashString(treeJson, StandardCharsets.UTF_8).toString();
        // if no files/directory name were edited server side, no need to send the tree again (saves bandwidth, mostly
        // for big projects tho but still worth adding)
        if (!assetsHash.equals(packet.assetsHash())) {
            Services.PACKET.sendToPlayer(player, new S2CInitTree(treeJson));
        }
    }
}
