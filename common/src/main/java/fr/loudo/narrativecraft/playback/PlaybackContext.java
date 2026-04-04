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
import fr.loudo.narrativecraft.api.playback.IPlaybackContext;
import fr.loudo.narrativecraft.api.recording.action.AbstractAction;
import fr.loudo.narrativecraft.api.recording.action.ActionResult;
import fr.loudo.narrativecraft.recording.RecordingData;
import fr.loudo.narrativecraft.utils.FakePlayer;
import fr.loudo.narrativecraft.utils.Translation;
import fr.loudo.narrativecraft.utils.Utils;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.NavigableMap;
import java.util.Optional;
import java.util.TreeMap;
import java.util.UUID;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.entity.*;
import net.minecraft.world.level.storage.TagValueInput;

public class PlaybackContext implements IPlaybackContext {

    private final Playback playback;
    private final RecordingData recordingData;
    private final ServerLevel level;
    private Entity entity;
    private final NavigableMap<Integer, List<AbstractAction>> rewindLog = new TreeMap<>();
    private boolean isPlaying = false;
    private boolean spawned = false;

    public PlaybackContext(Playback playback, RecordingData recordingData, ServerLevel level) {
        this.playback = playback;
        this.recordingData = recordingData;
        this.level = level;
    }

    public void start() {
        isPlaying = true;
        spawned = false;
        createEntity();
        if (entity != null && recordingData.getSpawnTick() == 0) {
            executeFirstActions();
            addEntityToWorld();
            spawned = true;
        }
    }

    private void executeFirstActions() {
        List<AbstractAction> actions = recordingData.getActions().get(recordingData.getSpawnTick());
        if (actions == null || actions.isEmpty()) return;
        for (AbstractAction action : actions) {
            action.execute(this);
        }
    }

    private void createEntity() {
        if (recordingData.getInitialNbt() != null) {
            Optional<EntityType<?>> typeOpt = EntityType.byString(recordingData.getEntityId());
            if (typeOpt.isEmpty()) {
                Utils.sendError(
                        Translation.message("error.playback_entity_type", recordingData.getEntityId()),
                        playback.getRequester());
                playback.stop();
                return;
            }
            entity = typeOpt.get().create(level, EntitySpawnReason.LOAD);
            if (entity == null) {
                Utils.sendError(
                        Translation.message("error.playback_entity_null", recordingData.getEntityId()),
                        playback.getRequester());
                playback.stop();
                return;
            }
            entity.load(TagValueInput.create(
                    ProblemReporter.DISCARDING, entity.registryAccess(), recordingData.getInitialNbt()));
            entity.setUUID(UUID.randomUUID());
            if (entity instanceof Mob mob) {
                mob.setNoAi(true);
            }
            entity.setInvulnerable(true);
            entity.entityTags().add(Playback.ENTITY_TAG);
            return;
        }

        if (recordingData.getEntityId().equals("minecraft:player")) {
            entity = new FakePlayer(level, new GameProfile(UUID.randomUUID(), "fakeP"), true);
            return;
        }

        Optional<EntityType<?>> entityType = EntityType.byString(recordingData.getEntityId());
        EntityType<?> type = entityType.orElse(null);
        if (type == null) {
            Utils.sendError(
                    Translation.message("error.playback_entity_type", recordingData.getEntityId()),
                    playback.getRequester());
            playback.stop();
            return;
        }
        entity = type.create(level, EntitySpawnReason.MOB_SUMMONED);
        if (entity == null) {
            Utils.sendError(
                    Translation.message("error.playback_entity_null", recordingData.getEntityId()),
                    playback.getRequester());
            playback.stop();
            return;
        }
        entity.setInvulnerable(true);
        entity.entityTags().add(Playback.ENTITY_TAG);
    }

    private void addEntityToWorld() {

        if (entity instanceof FakePlayer fakePlayer) {
            for (ServerPlayer player : playback.getTargetedPlayers()) {
                player.connection.send(new ClientboundPlayerInfoUpdatePacket(
                        ClientboundPlayerInfoUpdatePacket.Action.ADD_PLAYER, fakePlayer));
            }
            level.addNewPlayer(fakePlayer);
            return;
        }

        level.addFreshEntity(entity);
    }

    public void respawnEntity() {
        killEntity();
        createEntity();
        addEntityToWorld();
    }

    @Override
    public void respawnEntityByRecordingId(int recordingId) {
        PlaybackContext target = playback.getContextByRecordingId(recordingId);
        if (target != null) {
            target.respawnEntity();
        }
    }

    public boolean forSpecificPlayers() {
        return playback.forSpecificPlayers();
    }

    public Collection<ServerPlayer> getTargetedPlayers() {
        return playback.getTargetedPlayers();
    }

    public void stop() {
        if (entity == null) return;
        entity.remove(Entity.RemovalReason.KILLED);
        entity = null;
        pause();
    }

    public void play() {
        isPlaying = true;
    }

    public void pause() {
        isPlaying = false;
    }

    @Override
    public Entity getEntityByRecordingId(int recordingId) {
        return playback.getEntityByRecordingId(recordingId);
    }

    public int getRecordingId() {
        return recordingData.getRecordingId();
    }

    public void tick() {
        if (!isPlaying) return;

        int currentTick = playback.getTick();

        if (!spawned && currentTick >= recordingData.getSpawnTick()) {
            if (entity == null) createEntity();
            if (entity != null) {
                addEntityToWorld();
                spawned = true;
            }
        }

        if (!spawned) return;

        executeActionsFromTick(currentTick);
    }

    public void moveTo(int fromTick, int toTick) {
        for (int t = fromTick + 1; t <= toTick; t++) {
            if (!spawned && t >= recordingData.getSpawnTick()) {
                if (entity == null) createEntity();
                if (entity != null) {
                    addEntityToWorld();
                    spawned = true;
                }
            }
            if (spawned) executeActionsFromTick(t);
        }
    }

    public void rewindTo(int tick) {
        executeStateActionsFromTick(tick);
        List<Integer> keysToUndo = new ArrayList<>(rewindLog.tailMap(tick).keySet());
        Collections.reverse(keysToUndo);
        for (int t : keysToUndo) {
            for (AbstractAction snapshot : rewindLog.get(t)) {
                snapshot.execute(this);
            }
        }
        rewindLog.tailMap(tick, true).clear();
        if (tick < recordingData.getSpawnTick()) {
            killEntity();
        }
    }

    private void killEntity() {
        if (entity == null) return;
        entity.remove(Entity.RemovalReason.KILLED);
        entity = null;
        spawned = false;
    }

    private void executeActionsFromTick(int tick) {
        List<AbstractAction> actionsToPlay = recordingData.getActions().get(tick);
        if (actionsToPlay != null) {
            for (AbstractAction action : actionsToPlay) {
                action.createRewindSnapshot(this).ifPresent(snapshot -> rewindLog
                        .computeIfAbsent(tick, k -> new ArrayList<>())
                        .add(snapshot));
                ActionResult result = action.execute(this);
                if (result == ActionResult.ERROR) {
                    playback.stop();
                    sendError(action);
                    return;
                }
            }
        }
    }

    private void executeStateActionsFromTick(int tick) {
        List<AbstractAction> actionsToPlay = recordingData.getActions().get(tick);
        if (actionsToPlay == null) return;
        for (AbstractAction action : actionsToPlay) {
            if (action.shouldExecuteOnRewind()) {
                action.execute(this);
            }
        }
    }

    private void sendError(AbstractAction action) {
        Utils.sendError(
                Translation.message(
                        "error.playback",
                        playback.getAnimation().getName(),
                        playback.getTick(),
                        action.getClass().getName()),
                playback.getRequester());
    }

    public ServerLevel getLevel() {
        return level;
    }

    public Entity getEntity() {
        return entity;
    }
}
