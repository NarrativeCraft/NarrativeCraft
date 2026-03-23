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

import fr.loudo.narrativecraft.narrative.animation.Animation;
import fr.loudo.narrativecraft.recording.RecordingData;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import net.minecraft.network.protocol.game.ClientboundRemoveEntitiesPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;

public class Playback {

    private final UUID id = UUID.randomUUID();
    private final Animation animation;
    private final ServerPlayer requester;
    private final List<PlaybackContext> contexts = new ArrayList<>();
    private final Collection<ServerPlayer> targetedPlayers = new ArrayList<>();
    private int tick = 0;
    private int totalTicks = 0;
    private boolean isPlaying;
    private boolean ended = false;

    public Playback(Animation animation, ServerPlayer requester) {
        this.animation = animation;
        this.requester = requester;
    }

    public void start() {
        tick = 0;
        totalTicks = 0;
        ended = false;
        contexts.clear();

        for (RecordingData recordingData : animation.getRecordingDataList()) {
            contexts.add(new PlaybackContext(this, recordingData, requester.level()));

            int entityMaxTick = recordingData.getActions().keySet().stream()
                    .mapToInt(Integer::intValue)
                    .max()
                    .orElse(-1);
            totalTicks = Math.max(totalTicks, entityMaxTick + 1);
        }

        isPlaying = true;
        for (PlaybackContext context : contexts) {
            context.start();
        }
        if (forSpecificPlayers()) {
            hideEntitiesToOtherPlayers();
        }
    }

    public void start(Collection<ServerPlayer> targetedPlayers) {
        this.targetedPlayers.addAll(targetedPlayers);
        start();
    }

    public void stop() {
        isPlaying = false;
        for (PlaybackContext context : contexts) {
            context.stop();
        }
    }

    public void tick() {
        if (!isPlaying) return;

        if (tick >= totalTicks) {
            stop();
            ended = true;
            return;
        }

        for (PlaybackContext context : contexts) {
            context.tick();
        }

        tick++;
    }

    public void hideEntitiesToOtherPlayers() {
        for (ServerPlayer serverPlayer : requester.level().players()) {
            if (targetedPlayers.contains(serverPlayer)) continue;
            for (PlaybackContext context : contexts) {
                serverPlayer.connection.send(
                        new ClientboundRemoveEntitiesPacket(context.getEntity().getId()));
            }
        }
    }

    public void hideEntitiesToPlayer(ServerPlayer player) {
        for (PlaybackContext context : contexts) {
            player.connection.send(
                    new ClientboundRemoveEntitiesPacket(context.getEntity().getId()));
        }
    }

    public boolean entityFromPlayback(Entity entity) {
        for (PlaybackContext context : contexts) {
            if (context.getEntity().getUUID().equals(entity.getUUID())) {
                return true;
            }
        }
        return false;
    }

    public boolean isEnded() {
        return ended;
    }

    public UUID getId() {
        return id;
    }

    public Animation getAnimation() {
        return animation;
    }

    public ServerPlayer getRequester() {
        return requester;
    }

    public int getTick() {
        return tick;
    }

    public Collection<ServerPlayer> getTargetedPlayers() {
        return targetedPlayers;
    }

    public boolean forSpecificPlayers() {
        return !targetedPlayers.isEmpty();
    }

    public Entity getEntityByRecordingId(int recordingId) {
        for (PlaybackContext context : contexts) {
            if (context.getRecordingId() == recordingId) {
                return context.getEntity();
            }
        }
        return null;
    }

    public boolean isPlaying() {
        return isPlaying;
    }

    public void setPlaying(boolean playing) {
        isPlaying = playing;
    }
}
