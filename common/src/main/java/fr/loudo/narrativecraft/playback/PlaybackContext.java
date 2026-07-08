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

import fr.loudo.narrativecraft.api.playback.IPlaybackContext;
import fr.loudo.narrativecraft.api.recording.action.AbstractAction;
import fr.loudo.narrativecraft.api.recording.action.ActionResult;
import fr.loudo.narrativecraft.narrative.character.CharacterType;
import fr.loudo.narrativecraft.narrative.character.ICharacterStory;
import fr.loudo.narrativecraft.recording.RecordingData;
import fr.loudo.narrativecraft.utils.FakePlayer;
import fr.loudo.narrativecraft.utils.Translation;
import fr.loudo.narrativecraft.utils.Utils;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoRemovePacket;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.item.ItemEntity;

import java.util.*;

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
            action.execute(this, playback);
        }
    }

    private void createEntity() {
        ICharacterStory characterStory = playback.getAnimation().getCharacterStory();
        boolean hasCharacter = characterStory != null;
        boolean isCharacterEntity = recordingData.getRecordingId() != 0;

        String entityId = (hasCharacter && !isCharacterEntity)
                ? Utils.getEntityTypeString(characterStory.getEntityType())
                : recordingData.getEntityId();

        if (entityId.equals("minecraft:player")) {
            String name = hasCharacter ? characterStory.getName() : "Somebody";
            entity = new FakePlayer(
                    level,
                    FakePlayer.createCharacterProfile(playback.getAnimation().getCharacterStory(), name),
                    true);
            return;
        }

        Optional<EntityType<?>> typeOpt = EntityType.byString(entityId);
        if (typeOpt.isEmpty()) {
            Utils.sendError(Translation.message("error.playback_entity_type", entityId), playback.getRequester());
            playback.stop();
            return;
        }

        entity = typeOpt.get().create(level);
        if (entity == null) {
            Utils.sendError(Translation.message("error.playback_entity_null", entityId), playback.getRequester());
            playback.stop();
            return;
        }

        CompoundTag nbt = recordingData.getInitialNbt();
        if (nbt != null && !nbt.isEmpty()) {
            entity.load(nbt);
            entity.setUUID(UUID.randomUUID());
        }

        if (hasCharacter && isCharacterEntity && entity instanceof LivingEntity && characterStory.getCharacterType() == CharacterType.NORMAL) {
            String name = characterStory.getName();
            if (name != null && !name.isBlank()) {
                entity.setCustomName(Component.literal(name));
                entity.setCustomNameVisible(true);
            }
        }

        if (entity instanceof Mob mob) {
            mob.setNoAi(true);
        }

        entity.setInvulnerable(true);
        entity.getTags().add(Playback.ENTITY_TAG);
    }

    private void addEntityToWorld() {
        if (!entity.isRemoved()) {
            if (entity instanceof FakePlayer fakePlayer) {
                Collection<ServerPlayer> players =
                        playback.forSpecificPlayers() ? playback.getTargetedPlayers() : level.players();
                for (ServerPlayer player : players) {
                    player.connection.send(new ClientboundPlayerInfoUpdatePacket(
                            ClientboundPlayerInfoUpdatePacket.Action.ADD_PLAYER, fakePlayer));
                }
                level.addNewPlayer(fakePlayer);
                return;
            }
            if (entity instanceof ItemEntity itemEntity) {
                float f = level.getRandom().nextFloat() * 0.5F;
                float angle = level.getRandom().nextFloat() * (float) (Math.PI * 2);
                itemEntity.setDeltaMovement(-Math.sin(angle) * f, 0.2F, Math.cos(angle) * f);
                level.addFreshEntity(entity);
                return;
            }
            level.addFreshEntity(entity);
        }
    }

    public void respawnEntity() {
        killEntity();
        createEntity();
        addEntityToWorld();
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
        if (entity == null) return;
        executeStateActionsFromTick(tick);
        List<Integer> keysToUndo = new ArrayList<>(rewindLog.tailMap(tick).keySet());
        Collections.reverse(keysToUndo);
        for (int t : keysToUndo) {
            for (AbstractAction snapshot : rewindLog.get(t)) {
                snapshot.execute(this, playback);
            }
        }
        rewindLog.tailMap(tick, true).clear();
        playback.clearBlockStateLogFrom(tick);
        if (tick < recordingData.getSpawnTick()) {
            killEntity();
        }
    }

    private void killEntity() {
        if (entity == null) return;
        entity.remove(Entity.RemovalReason.KILLED);
        if (entity instanceof FakePlayer player) {
            for (ServerPlayer serverPlayer : playback.getLevel().players()) {
                serverPlayer.connection.send(new ClientboundPlayerInfoRemovePacket(List.of(player.getUUID())));
            }
        }
        entity = null;
        spawned = false;
    }

    private void executeActionsFromTick(int tick) {
        List<AbstractAction> actionsToPlay = recordingData.getActions().get(tick);
        if (actionsToPlay != null) {
            for (AbstractAction action : actionsToPlay) {
                action.createRewindSnapshot(this, playback).forEach(snapshot -> rewindLog
                        .computeIfAbsent(action.getTick(), k -> new ArrayList<>())
                        .add(snapshot));
                ActionResult result = action.execute(this, playback);
                if (result == ActionResult.ERROR) {
                    playback.stop();
                    sendError(action);
                    return;
                }
            }
        }
    }

    private void executeStateActionsFromTick(int tick) {
        Set<Class<? extends AbstractAction>> covered = new HashSet<>();
        Map<Integer, List<AbstractAction>> allActions = recordingData.getActions();
        List<Integer> keys = new ArrayList<>(allActions.keySet());
        keys.sort(Collections.reverseOrder());
        for (int t : keys) {
            if (t > tick) continue;
            List<AbstractAction> actions = allActions.get(t);
            for (int i = actions.size() - 1; i >= 0; i--) {
                AbstractAction action = actions.get(i);
                if (action.shouldExecuteOnRewind() && covered.add(action.getClass())) {
                    action.execute(this, playback);
                }
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

    @Override
    public Entity getEntity() {
        return entity;
    }

    public void setEntity(Entity entity) {
        this.entity = entity;
    }
}
