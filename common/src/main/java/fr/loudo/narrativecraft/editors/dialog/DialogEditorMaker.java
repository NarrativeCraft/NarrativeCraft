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

package fr.loudo.narrativecraft.editors.dialog;

import com.mojang.authlib.GameProfile;
import fr.loudo.narrativecraft.editors.EditorMaker;
import fr.loudo.narrativecraft.narrative.NarrativeEnvironment;
import fr.loudo.narrativecraft.narrative.character.ICharacterStory;
import fr.loudo.narrativecraft.network.BiStopEditorMaker;
import fr.loudo.narrativecraft.network.dialog.S2CDialogEditorEntitySpawned;
import fr.loudo.narrativecraft.platform.Services;
import fr.loudo.narrativecraft.session.PlayerSession;
import fr.loudo.narrativecraft.utils.FakePlayer;
import fr.loudo.narrativecraft.utils.UtilsServer;
import java.util.List;
import java.util.UUID;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket;
import net.minecraft.network.protocol.game.ClientboundRemoveEntitiesPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.GameType;
import net.minecraft.world.phys.Vec3;

public class DialogEditorMaker implements EditorMaker {

    private final PlayerSession playerSession;
    private final ICharacterStory character;
    private Entity fakePlayer;

    public DialogEditorMaker(PlayerSession playerSession, ICharacterStory character) {
        this.playerSession = playerSession;
        this.character = character;
    }

    @Override
    public void init() {
        ServerPlayer player = playerSession.getPlayer();
        ServerLevel level = player.level();

        GameProfile profile = character != null
                ? new GameProfile(character.getId(), character.getName())
                : new GameProfile(UUID.randomUUID(), player.getGameProfile().name());

        FakePlayer entity = new FakePlayer(level, profile, true);

        double yaw = Math.toRadians(player.getYRot());
        Vec3 pos = player.position().add(-Math.sin(yaw) * 3, 0, Math.cos(yaw) * 3);
        entity.setPos(pos);
        entity.setYRot(player.getYRot() + 180f);
        entity.setYHeadRot(player.getYRot() + 180f);

        player.connection.send(
                new ClientboundPlayerInfoUpdatePacket(ClientboundPlayerInfoUpdatePacket.Action.ADD_PLAYER, entity));
        level.addNewPlayer(entity);

        if (character != null) {
            UtilsServer.broadcastCharacterSkin(List.of(player), character);
        }

        fakePlayer = entity;
        playerSession.changeGameMode(GameType.SPECTATOR);
        Services.PACKET.sendToPlayer(player, new S2CDialogEditorEntitySpawned(entity.getId()));
    }

    @Override
    public void stop() {
        if (fakePlayer != null) {
            fakePlayer.remove(Entity.RemovalReason.DISCARDED);
            fakePlayer = null;
        }
        playerSession.changeGameMode(playerSession.getLastGameType());
        Services.PACKET.sendToPlayer(playerSession.getPlayer(), BiStopEditorMaker.INSTANCE);
    }

    @Override
    public void tick() {
        if (fakePlayer == null) return;
        for (ServerPlayer player : playerSession.getPlayer().level().players()) {
            if (player.getId() == playerSession.getPlayer().getId()) continue;
            player.connection.send(new ClientboundRemoveEntitiesPacket(fakePlayer.getId()));
        }
    }

    @Override
    public void teleportToEditorOrigin() {}

    @Override
    public NarrativeEnvironment getEnvironment() {
        return NarrativeEnvironment.DEVELOPMENT;
    }
}
