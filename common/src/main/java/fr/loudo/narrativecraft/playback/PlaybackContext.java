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

package fr.loudo.narrativecraft.playback;

import com.mojang.authlib.GameProfile;
import fr.loudo.narrativecraft.recording.actions.AbstractAction;
import fr.loudo.narrativecraft.recording.actions.ActionResult;
import fr.loudo.narrativecraft.utils.FakePlayer;
import java.util.List;
import java.util.UUID;

import fr.loudo.narrativecraft.utils.Translation;
import fr.loudo.narrativecraft.utils.Utils;
import fr.loudo.narrativecraft.utils.UtilsServer;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

public class PlaybackContext {

    private final Playback playback;
    private final ServerLevel level;
    private LivingEntity entity;

    public PlaybackContext(Playback playback, ServerLevel level) {
        this.playback = playback;
        this.level = level;
    }

    public void start() {
        entity = new FakePlayer(level, new GameProfile(UUID.randomUUID(), "fakeP"), true);
        if (entity instanceof FakePlayer fakePlayer) {
            UtilsServer.broadcastPacket(new ClientboundPlayerInfoUpdatePacket(ClientboundPlayerInfoUpdatePacket.Action.ADD_PLAYER, fakePlayer));
            level.addFreshEntity(fakePlayer);
        }
    }

    public void stop() {
        if (entity == null) return;

        entity.remove(Entity.RemovalReason.KILLED);
        entity = null;
    }

    public void tick() {
        List<AbstractAction> actionsToPlay = playback.getAnimation().getActions().get(playback.getTick());
        if (actionsToPlay == null) return;

        for (AbstractAction action : actionsToPlay) {
            ActionResult result = action.execute(this);
            if (result == ActionResult.ERROR) {
                playback.stop();
                sendError(action);
            }
        }
    }

    private void sendError(AbstractAction action) {
        Utils.sendError(Translation.message("error.playback", playback.getAnimation().getName(), playback.getTick(), action.getType()), playback.getRequester());
    }

    public ServerLevel getLevel() {
        return level;
    }

    public LivingEntity getEntity() {
        return entity;
    }
}
