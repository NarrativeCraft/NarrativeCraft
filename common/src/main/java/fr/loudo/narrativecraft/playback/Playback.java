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

package fr.loudo.narrativecraft.playback;

import fr.loudo.narrativecraft.NarrativeCraftMod;
import fr.loudo.narrativecraft.api.events.playback.PlaybackEndEvent;
import fr.loudo.narrativecraft.api.events.playback.PlaybackPauseEvent;
import fr.loudo.narrativecraft.api.events.playback.PlaybackResumeEvent;
import fr.loudo.narrativecraft.api.events.playback.PlaybackStartEvent;
import fr.loudo.narrativecraft.api.playback.IPlaybackSession;
import fr.loudo.narrativecraft.api.recording.action.AbstractAction;
import fr.loudo.narrativecraft.narrative.animation.Animation;
import fr.loudo.narrativecraft.network.story.S2CCharacterStoryAction;
import fr.loudo.narrativecraft.platform.Services;
import fr.loudo.narrativecraft.recording.RecordingData;
import fr.loudo.narrativecraft.recording.actions.MovementAction;
import fr.loudo.narrativecraft.utils.UtilsServer;
import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.game.ClientboundRemoveEntitiesPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class Playback implements IPlaybackSession {

    public static final String ENTITY_TAG = "nc_playback";

    private final UUID id = UUID.randomUUID();
    private final Animation animation;
    private final ServerPlayer requester;
    private final List<PlaybackContext> contexts = new ArrayList<>();
    private final Collection<ServerPlayer> targetedPlayers = new ArrayList<>();
    private final NavigableMap<Integer, Map<BlockPos, BlockState>> blockStateLog = new TreeMap<>();
    private int tick = 0;
    private int maxTick = 0;
    private boolean isPlaying, killOnEnd;
    private boolean ended = false;

    public Playback(Animation animation, ServerPlayer requester) {
        this.animation = animation;
        this.requester = requester;
    }

    public void start() {
        tick = 0;
        maxTick = 0;
        ended = false;
        contexts.clear();
        blockStateLog.clear();

        for (RecordingData recordingData : animation.getRecordingDataList()) {
            contexts.add(new PlaybackContext(this, recordingData, requester.level()));
        }

        maxTick = animation.getTotalTick();
        isPlaying = true;
        for (PlaybackContext context : contexts) {
            context.start();
        }
        if (forSpecificPlayers()) {
            hideEntitiesToOtherPlayers();
        }
        for (ServerPlayer player : requester.level().players()) {
            Services.PACKET.sendToPlayer(
                    player,
                    new S2CCharacterStoryAction(
                            animation.getCharacterStory().getId(), S2CCharacterStoryAction.Action.ADD));
        }
        UtilsServer.broadcastCharacterSkin(requester.level().players(), animation.getCharacterStory());
        NarrativeCraftMod.EVENT_BUS.post(new PlaybackStartEvent(this));
    }

    public Entity getMasterEntity() {
        if (contexts.isEmpty()) return null;
        return contexts.getFirst().getEntity();
    }

    public void setMasterEntity(Entity entity) {
        if (contexts.isEmpty()) return;
        contexts.getFirst().setEntity(entity);
    }

    public void start(Collection<ServerPlayer> targetedPlayers) {
        this.targetedPlayers.addAll(targetedPlayers);
        for (ServerPlayer player : targetedPlayers) {
            Services.PACKET.sendToPlayer(
                    player,
                    new S2CCharacterStoryAction(
                            animation.getCharacterStory().getId(), S2CCharacterStoryAction.Action.ADD));
        }
        start();
    }

    public void play() {
        isPlaying = true;
        ended = false;
        for (PlaybackContext context : contexts) {
            context.play();
        }
        NarrativeCraftMod.EVENT_BUS.post(new PlaybackResumeEvent(this));
    }

    public void pause() {
        isPlaying = false;
        for (PlaybackContext context : contexts) {
            context.pause();
        }
        NarrativeCraftMod.EVENT_BUS.post(new PlaybackPauseEvent(this));
    }

    public void stop() {
        isPlaying = false;
        if (killOnEnd) {
            resetActions();
            for (ServerPlayer player : requester.level().players()) {
                Services.PACKET.sendToPlayer(
                        player,
                        new S2CCharacterStoryAction(
                                animation.getCharacterStory().getId(), S2CCharacterStoryAction.Action.REMOVE));
            }
        }
        for (PlaybackContext context : contexts) {
            if (killOnEnd) {
                context.stop();
            } else {
                context.pause();
            }
        }
        NarrativeCraftMod.EVENT_BUS.post(new PlaybackEndEvent(this));
    }

    public void stopAndKill() {
        killOnEnd = true;
        stop();
    }

    public void tick() {
        if (!isPlaying) return;

        if (tick >= maxTick) {
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
                if (context.getEntity() == null) continue;
                serverPlayer.connection.send(
                        new ClientboundRemoveEntitiesPacket(context.getEntity().getId()));
            }
        }
    }

    public void hideEntitiesToPlayer(ServerPlayer player) {
        for (PlaybackContext context : contexts) {
            if (context.getEntity() == null) continue;
            player.connection.send(
                    new ClientboundRemoveEntitiesPacket(context.getEntity().getId()));
        }
    }

    public boolean entityFromPlayback(Entity entity) {
        for (PlaybackContext context : contexts) {
            if (context.getEntity() == null) continue;
            if (context.getEntity().getUUID().equals(entity.getUUID())) {
                return true;
            }
        }
        return false;
    }

    public void moveTo(int toTick) {
        int fromTick = this.tick;
        this.tick = toTick;
        for (int t = fromTick + 1; t <= toTick; t++) {
            for (PlaybackContext context : contexts) {
                context.moveTo(t - 1, t);
            }
        }
    }

    public void rewindTo(int tick) {
        for (PlaybackContext context : contexts) {
            context.rewindTo(tick);
        }
        this.tick = tick;
    }

    @Override
    public ServerLevel getLevel() {
        return requester.level();
    }

    @Override
    public void respawnEntityByRecordingId(int recordingId) {
        PlaybackContext target = getContextByRecordingId(recordingId);
        if (target != null) {
            target.respawnEntity();
        }
    }

    public void resetActions() {
        for (PlaybackContext context : contexts) {
            context.rewindTo(0);
        }
    }

    public Vec3 getFirstPosition() {
        RecordingData recordingData = animation.getRecordingDataList().get(0);
        for (List<AbstractAction> actions : recordingData.getActions().values()) {
            for (AbstractAction action : actions) {
                if (action instanceof MovementAction movementAction) {
                    return movementAction.getPosition();
                }
            }
        }
        return null;
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

    @Override
    public Collection<ServerPlayer> getTargetedPlayers() {
        return targetedPlayers;
    }

    @Override
    public boolean forSpecificPlayers() {
        return !targetedPlayers.isEmpty();
    }

    public boolean isKillOnEnd() {
        return killOnEnd;
    }

    public void setKillOnEnd(boolean killOnEnd) {
        this.killOnEnd = killOnEnd;
    }

    @Override
    public Entity getEntityByRecordingId(int recordingId) {
        for (PlaybackContext context : contexts) {
            if (context.getRecordingId() == recordingId) {
                return context.getEntity();
            }
        }
        return null;
    }

    public PlaybackContext getContextByRecordingId(int recordingId) {
        for (PlaybackContext context : contexts) {
            if (context.getRecordingId() == recordingId) {
                return context;
            }
        }
        return null;
    }

    @Override
    public BlockState getBlockStateAtTick(BlockPos pos, int tick) {
        NavigableMap<Integer, Map<BlockPos, BlockState>> before = blockStateLog.headMap(tick, false);
        for (int t : before.descendingKeySet()) {
            Map<BlockPos, BlockState> changes = before.get(t);
            if (changes != null && changes.containsKey(pos)) return changes.get(pos);
        }
        return getLevel().getBlockState(pos);
    }

    @Override
    public void recordBlockState(int tick, BlockPos pos, BlockState state) {
        blockStateLog.computeIfAbsent(tick, k -> new HashMap<>()).put(pos, state);
    }

    @Override
    public void clearBlockStateLogFrom(int tick) {
        blockStateLog.tailMap(tick, true).clear();
    }

    public boolean isPlaying() {
        return isPlaying;
    }
}
