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

package fr.loudo.narrativecraft.recording;

import fr.loudo.narrativecraft.NarrativeCraftMod;
import fr.loudo.narrativecraft.api.events.recording.RecordingSaveEvent;
import fr.loudo.narrativecraft.api.events.recording.RecordingStartEvent;
import fr.loudo.narrativecraft.api.events.recording.RecordingStopEvent;
import fr.loudo.narrativecraft.api.recording.IRecording;
import fr.loudo.narrativecraft.api.recording.IRecordingEntityData;
import fr.loudo.narrativecraft.api.recording.action.AbstractAction;
import fr.loudo.narrativecraft.files.NarrativeCraftFileEditor;
import fr.loudo.narrativecraft.files.NarrativeCraftFileRegistry;
import fr.loudo.narrativecraft.mixin.accessor.AbstractHorseAccessor;
import fr.loudo.narrativecraft.mixin.accessor.EntityAccessor;
import fr.loudo.narrativecraft.mixin.accessor.LivingEntityAccessor;
import fr.loudo.narrativecraft.narrative.animation.Animation;
import fr.loudo.narrativecraft.narrative.character.CharacterStory;
import fr.loudo.narrativecraft.narrative.subscene.Subscene;
import fr.loudo.narrativecraft.network.BiSyncNarrativeEntryPacket;
import fr.loudo.narrativecraft.platform.Services;
import fr.loudo.narrativecraft.playback.Playback;
import fr.loudo.narrativecraft.recording.actions.*;
import fr.loudo.narrativecraft.session.PlayerSession;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.phys.AABB;

public class Recording implements IRecording {

    public static final String RECORDING_EXTENSION = ".ncr";
    private static final double SCAN_RADIUS = 40.0;

    private final UUID id = UUID.randomUUID();
    private final PlayerSession playerSession;
    private final List<RecordingEntityData> recordingEntityDataList = new ArrayList<>();
    private final List<Playback> subscenePlaybacks = new ArrayList<>();
    private int nextNearbyEntityLocalId = 1;
    private boolean isRecording = false;
    private int tick = 0;
    private String pendingOverwriteName = null;

    public Recording(PlayerSession playerSession) {
        this.playerSession = playerSession;
    }

    public void tick() {
        if (!isRecording) return;

        scanNearbyEntities();

        for (RecordingEntityData data : recordingEntityDataList) {
            Entity entity = data.getEntity();
            recordRideState(data);
            data.addAction(new MovementAction(
                    tick, entity.position(), entity.getXRot(), entity.getYRot(), entity.getYHeadRot()));
            data.addAction(new PoseAction(tick, entity.getPose()));
            data.addAction(
                    new EntityByteAction(tick, entity.getEntityData().get(EntityAccessor.getDATA_SHARED_FLAGS_ID())));
            data.addAction(new SilentDeathAction(tick, entity.isRemoved()));
            if (entity instanceof LivingEntity livingEntity) {
                data.addAction(new ChangeItemAction(tick, livingEntity));
                data.addAction(new SwingAction(tick, livingEntity));
                data.addAction(new LivingEntityByteAction(
                        tick, entity.getEntityData().get(LivingEntityAccessor.getDATA_LIVING_ENTITY_FLAGS())));
            }
            if (entity instanceof AbstractHorse horse) {
                data.addAction(
                        new HorseByteAction(tick, horse.getEntityData().get(AbstractHorseAccessor.getDATA_ID_FLAGS())));
            }
            if (entity instanceof Boat boat) {
                data.addAction(new BoatDataAction(tick, boat));
            }
            if (entity instanceof ServerPlayer player) {
                data.addAction(new GameModeAction(tick, player));
            }
        }
        tick++;
    }

    private void scanNearbyEntities() {
        Entity player = playerSession.getPlayer();
        AABB searchBox = player.getBoundingBox().inflate(SCAN_RADIUS);
        List<Entity> found = player.level().getEntities(player, searchBox, e -> !(e instanceof Player));
        for (Entity entity : found) {
            if (entity.getTags().contains(Playback.ENTITY_TAG)) continue;
            if (getRecordingEntityData(entity) == null) {
                RecordingEntityData recordingEntityData =
                        new RecordingEntityData(nextNearbyEntityLocalId++, entity, false, tick, this);
                recordingEntityDataList.add(recordingEntityData);
                seedActions(recordingEntityData);
            }
        }
    }

    private void recordRideState(RecordingEntityData data) {
        Entity entity = data.getEntity();
        Entity currentVehicle = entity.getVehicle();
        Entity previousVehicle = data.getVehicle();
        if (currentVehicle == previousVehicle) return;

        if (currentVehicle != null) {
            // action managed in EntityMixin
            markEntityAsTracked(currentVehicle);
        } else if (!previousVehicle.isRemoved()) {
            RecordingEntityData previousVehicleData = getRecordingEntityData(previousVehicle);
            if (previousVehicleData != null) {
                data.addAction(new StopRideEntityAction(tick, previousVehicleData.getRecordingId()));
            }
        }

        data.setVehicle(currentVehicle);
    }

    public void addAction(AbstractAction action, Entity entity) {
        RecordingEntityData data = getRecordingEntityData(entity);
        if (data == null) return;
        data.addAction(action);
    }

    public void addAction(AbstractAction action, UUID entityId) {
        RecordingEntityData data = getRecordingEntityData(entityId);
        if (data == null) return;
        data.addAction(action);
    }

    public int markEntityAsTracked(Entity entity) {
        RecordingEntityData data = getRecordingEntityData(entity);
        if (data == null) {
            data = new RecordingEntityData(nextNearbyEntityLocalId++, entity, false, tick, this);
            recordingEntityDataList.add(data);
        }

        if (!data.isTracked()) {
            CompoundTag nbt = new CompoundTag();
            entity.saveWithoutId(nbt);
            data.getRecordingData().setInitialNbt(nbt);
            data.getRecordingData().setSpawnTick(data.getFirstSeenTick());
            data.setTracked(true);
        }
        seedActions(data);
        return data.getRecordingId();
    }

    public void start() {
        isRecording = true;
        recordingEntityDataList.clear();
        recordingEntityDataList.add(new RecordingEntityData(0, playerSession.getPlayer(), true, 0, this));

        for (RecordingEntityData data : recordingEntityDataList) {
            seedActions(data);
        }

        // If player is already riding an entity first tick
        Entity vehicle = getPlayer().getVehicle();
        if (vehicle != null) {
            recordingEntityDataList.add(new RecordingEntityData(nextNearbyEntityLocalId++, vehicle, false, 0, this));
            addAction(new RideEntityAction(0, markEntityAsTracked(vehicle)), getPlayer());
        }

        NarrativeCraftMod.EVENT_BUS.post(new RecordingStartEvent(getPlayer(), this));
    }

    public void stop() {
        isRecording = false;
        for (Playback playback : subscenePlaybacks) {
            playback.stopAndKill();
            NarrativeCraftMod.getInstance().getPlaybackManager().remove(playback);
        }
        subscenePlaybacks.clear();
        NarrativeCraftMod.EVENT_BUS.post(new RecordingStopEvent(getPlayer(), this));
    }

    public void addSubscenePlayback(Playback playback) {
        subscenePlaybacks.add(playback);
    }

    private void seedActions(RecordingEntityData data) {
        data.seedLastAction(SwingAction.ID, new SwingAction(0));
        data.seedLastAction(SilentDeathAction.ID, new SilentDeathAction(0));
    }

    public RecordingEntityData getRecordingEntityData(Entity entity) {
        UUID uuid = entity.getUUID();
        for (RecordingEntityData data : recordingEntityDataList) {
            if (data.getEntity().getUUID().equals(uuid)) return data;
        }
        return null;
    }

    public RecordingEntityData getRecordingEntityData(UUID entityId) {
        for (RecordingEntityData data : recordingEntityDataList) {
            if (data.getEntity().getUUID().equals(entityId)) return data;
        }
        return null;
    }

    public boolean save(String name) {
        return save(name, null);
    }

    public boolean save(String name, Animation animationToOverwrite) {
        CharacterStory characterStory =
                NarrativeCraftMod.getInstance().getCharacterManager().getList().get(0);
        Animation animation = new Animation(id, name, playerSession.getScene(), tick, characterStory);

        if (animationToOverwrite != null) {
            List<Subscene> linkedSubscenes = animationToOverwrite.getLinkedSubscenes();
            if (NarrativeCraftFileRegistry.getInstance().delete(animationToOverwrite)
                    == NarrativeCraftFileEditor.OPERATION_FAILED) {
                return false;
            }
            animationToOverwrite.getScene().getAnimationManager().remove(animationToOverwrite);
            Services.PACKET.sendToPlayer(
                    getPlayer(),
                    BiSyncNarrativeEntryPacket.delete(animationToOverwrite.getId(), animationToOverwrite.toPayload()));
            for (Subscene linkedSubscene : linkedSubscenes) {
                linkedSubscene.getAnimations().add(animation);
            }
        }

        if (NarrativeCraftFileRegistry.getInstance().create(animation) == NarrativeCraftFileEditor.OPERATION_FAILED) {
            return false;
        }

        for (RecordingEntityData data : recordingEntityDataList) {
            if (!data.isTracked()) continue;
            animation.getRecordingDataList().add(data.getRecordingData());
        }

        playerSession.getScene().getAnimationManager().add(animation);
        Services.PACKET.sendToPlayer(
                getPlayer(), BiSyncNarrativeEntryPacket.add(animation.getId(), animation.toPayload()));
        NarrativeCraftMod.EVENT_BUS.post(new RecordingSaveEvent(getPlayer(), this, animation.getName()));
        return true;
    }

    public int getEntityTrackedSize() {
        return recordingEntityDataList.stream()
                .filter(RecordingEntityData::isTracked)
                .toList()
                .size();
    }

    public UUID getId() {
        return id;
    }

    public ServerPlayer getPlayer() {
        return playerSession.getPlayer();
    }

    public PlayerSession getPlayerSession() {
        return playerSession;
    }

    public boolean isRecording() {
        return isRecording;
    }

    public void setRecording(boolean recording) {
        isRecording = recording;
    }

    public int getTick() {
        return tick;
    }

    public List<RecordingEntityData> getRecordingEntityDataList() {
        return recordingEntityDataList;
    }

    public List<IRecordingEntityData> getRecordingEntities() {
        return new ArrayList<>(recordingEntityDataList);
    }

    public String getPendingOverwriteName() {
        return pendingOverwriteName;
    }

    public void setPendingOverwriteName(String pendingOverwriteName) {
        this.pendingOverwriteName = pendingOverwriteName;
    }
}
