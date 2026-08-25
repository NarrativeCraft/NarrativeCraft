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

package fr.loudo.narrativecraft.utils;

import com.google.common.io.Files;
import fr.loudo.narrativecraft.NarrativeCraftMod;
import fr.loudo.narrativecraft.managers.PlayerSessionManager;
import fr.loudo.narrativecraft.narrative.NarrativeEnvironment;
import fr.loudo.narrativecraft.narrative.cameraangle.CameraAngle;
import fr.loudo.narrativecraft.narrative.character.ICharacterStory;
import fr.loudo.narrativecraft.narrative.mainScreen.MainScreenMakerEditor;
import fr.loudo.narrativecraft.narrative.story.StoryHandler;
import fr.loudo.narrativecraft.network.S2CCharacterSkin;
import fr.loudo.narrativecraft.network.S2CScreenClear;
import fr.loudo.narrativecraft.network.mainScreen.BiMainScreenEnter;
import fr.loudo.narrativecraft.network.mainScreen.S2COpenMainScreen;
import fr.loudo.narrativecraft.platform.Services;
import fr.loudo.narrativecraft.server.settings.NarrativeServerSettings;
import fr.loudo.narrativecraft.session.PlayerSession;
import java.io.File;
import java.util.Collection;
import java.util.UUID;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
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

    public static void broadcastPacket(Packet<?> packet) {
        for (ServerPlayer player :
                NarrativeCraftMod.getInstance().getServer().getPlayerList().getPlayers()) {
            player.connection.send(packet);
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

    public static void broadcastCharacterSkin(Collection<ServerPlayer> players, ICharacterStory characterStory) {
        PlayerSessionManager sessionManager = NarrativeCraftMod.getInstance().getPlayerSessionManager();
        try {
            File skinFile = characterStory.getSkinFile();
            if (skinFile == null || !skinFile.exists()) return;
            byte[] skinBytes = Files.toByteArray(skinFile);
            for (ServerPlayer player : players) {
                PlayerSession playerSession = sessionManager.getByPlayer(player);
                if (playerSession == null) continue;
                // cache system, saves bandwidth
                if (playerSession.getCharacterIdsSkinLoaded().contains(characterStory.getId())) continue;
                Services.PACKET.sendToPlayer(player, new S2CCharacterSkin(characterStory.getId(), skinBytes));
                playerSession.getCharacterIdsSkinLoaded().add(characterStory.getId());
            }
        } catch (Exception e) {
            NarrativeCraftMod.LOGGER.error("Failed to send character skin of {}", characterStory.getName(), e);
        }
    }

    public static void sendCharacterSkin(ServerPlayer player, ICharacterStory characterStory) {
        if (characterStory == null) return;
        PlayerSessionManager sessionManager = NarrativeCraftMod.getInstance().getPlayerSessionManager();
        try {
            File skinFile = characterStory.getSkinFile();
            if (skinFile == null || !skinFile.exists()) return;
            byte[] skinBytes = Files.toByteArray(skinFile);
            PlayerSession playerSession = sessionManager.getByPlayer(player);
            if (playerSession == null) return;
            // cache system, saves bandwidth
            if (playerSession.getCharacterIdsSkinLoaded().contains(characterStory.getId())) return;
            Services.PACKET.sendToPlayer(player, new S2CCharacterSkin(characterStory.getId(), skinBytes));
            playerSession.getCharacterIdsSkinLoaded().add(characterStory.getId());
        } catch (Exception e) {
            NarrativeCraftMod.LOGGER.error("Failed to send character skin of {}", characterStory.getName(), e);
        }
    }

    public static void openMainScreenToPlayer(ServerPlayer player, boolean canContinue, boolean finishedStory) {
        PlayerSession playerSession =
                NarrativeCraftMod.getInstance().getPlayerSessionManager().getByPlayer(player);
        if (playerSession == null) return;

        Services.PACKET.sendToPlayer(player, new BiMainScreenEnter(NarrativeEnvironment.PRODUCTION));
        MainScreenMakerEditor editor = new MainScreenMakerEditor(
                NarrativeCraftMod.getInstance().getMainScreenData(), playerSession, NarrativeEnvironment.PRODUCTION);
        playerSession.openEditor(editor);
        Services.PACKET.sendToPlayer(player, new S2COpenMainScreen(canContinue, finishedStory));
    }

    public static void openMainScreenToPlayer(ServerPlayer player) {
        CameraAngle mainScreen = NarrativeCraftMod.getInstance().getMainScreenData();
        if (!NarrativeServerSettings.showMainScreenOnJoin
                || mainScreen == null
                || mainScreen.getCameras().isEmpty()
                || !player.level().getServer().isSingleplayer()) {
            return;
        }
        PlayerSession playerSession =
                NarrativeCraftMod.getInstance().getPlayerSessionManager().getByPlayer(player);
        if (playerSession == null) return;

        StoryHandler storyHandler =
                NarrativeCraftMod.getInstance().getSaveFileManager().loadSave(playerSession);

        boolean canContinue = storyHandler != null;
        boolean finishedStory = storyHandler != null && storyHandler.hasFinishedStory();

        openMainScreenToPlayer(player, canContinue, finishedStory);
    }
}
