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

package fr.loudo.narrativecraft.events.server;

import fr.loudo.narrativecraft.NarrativeCraftMod;
import fr.loudo.narrativecraft.files.DeserializationResult;
import fr.loudo.narrativecraft.narrative.NarrativeEntryInit;
import fr.loudo.narrativecraft.narrative.cameraangle.CameraAngle;
import fr.loudo.narrativecraft.server.settings.NarrativeServerSettings;
import fr.loudo.narrativecraft.session.PlayerSession;
import fr.loudo.narrativecraft.utils.Translation;
import fr.loudo.narrativecraft.utils.Utils;
import fr.loudo.narrativecraft.utils.UtilsServer;
import net.minecraft.server.level.ServerPlayer;

public class OnPlayerJoinEvent {

    public static void onPlayerJoin(ServerPlayer player) {
        NarrativeEntryInit.sendDataToPlayer(player);
        for (DeserializationResult<?> deserializationResult :
                NarrativeCraftMod.getInstance().getCorruptedDeserialization()) {
            Utils.sendError(Translation.message("error.corrupted_entry", deserializationResult.folderName()), player);
        }

        PlayerSession playerSession = new PlayerSession(player);
        NarrativeCraftMod.getInstance().getPlayerSessionManager().add(playerSession);

        CameraAngle mainScreen = NarrativeCraftMod.getInstance().getMainScreenData();
        if (NarrativeServerSettings.showMainScreenOnJoin
                && !mainScreen.getCameras().isEmpty()
                && player.level().getServer().isSingleplayer()) {
            UtilsServer.openMainScreenToPlayer(player);
        }
    }
}
