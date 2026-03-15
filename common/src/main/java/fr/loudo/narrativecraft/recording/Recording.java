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

package fr.loudo.narrativecraft.recording;

import fr.loudo.narrativecraft.files.NarrativeCraftFileEditor;
import fr.loudo.narrativecraft.files.NarrativeCraftFileRegistry;
import fr.loudo.narrativecraft.mixin.accessor.EntityAccessor;
import fr.loudo.narrativecraft.narrative.animation.Animation;
import fr.loudo.narrativecraft.network.BiSyncNarrativeEntryPacket;
import fr.loudo.narrativecraft.platform.Services;
import fr.loudo.narrativecraft.recording.actions.AbstractAction;
import fr.loudo.narrativecraft.recording.actions.EntityByteAction;
import fr.loudo.narrativecraft.recording.actions.MovementAction;
import fr.loudo.narrativecraft.recording.actions.PoseAction;
import fr.loudo.narrativecraft.session.PlayerSession;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;

public class Recording {

    public static final String RECORDING_EXTENSION = ".ncr";

    private final UUID id = UUID.randomUUID();
    private final PlayerSession playerSession;
    private final List<RecordingEntityData> recordingEntityData = new ArrayList<>();
    private boolean isRecording = false;
    private int tick = 0;

    public Recording(PlayerSession playerSession) {
        this.playerSession = playerSession;
        recordingEntityData.add(new RecordingEntityData(0, playerSession.getPlayer(), true));
    }

    public void tick() {
        if (!isRecording) return;
        for (RecordingEntityData recordingEntityData : recordingEntityData) {
            Entity entity = recordingEntityData.getEntity();
            recordingEntityData.addAction(new MovementAction(
                    tick, entity.position(), entity.getXRot(), entity.getYRot(), entity.getYHeadRot()));
            recordingEntityData.addAction(new PoseAction(tick, entity.getPose()));
            recordingEntityData.addAction(
                    new EntityByteAction(tick, entity.getEntityData().get(EntityAccessor.getDATA_SHARED_FLAGS_ID())));
        }
        tick++;
    }

    public void addAction(AbstractAction action, Entity entity) {
        RecordingEntityData recordingEntityData = getRecordingEntityData(entity);
        if (entity == null) return;
        recordingEntityData.addAction(action);
    }

    public void start() {
        isRecording = true;
    }

    public void stop() {
        isRecording = false;
    }

    public Entity getEntityByRecordingId(Entity entity) {
        for (RecordingEntityData recordingEntityData : recordingEntityData) {
            if (recordingEntityData.getEntity().getUUID().equals(entity.getUUID())) {
                return recordingEntityData.getEntity();
            }
        }
        return null;
    }

    public RecordingEntityData getRecordingEntityData(Entity entity) {
        for (RecordingEntityData recordingEntityData : recordingEntityData) {
            if (recordingEntityData.getEntity().getUUID().equals(entity.getUUID())) {
                return recordingEntityData;
            }
        }
        return null;
    }

    public boolean save(String name) {

        Animation animation = new Animation(id, name, playerSession.getScene());

        if (NarrativeCraftFileRegistry.getInstance().create(animation) == NarrativeCraftFileEditor.OPERATION_FAILED) {
            return false;
        }

        for (RecordingEntityData recordingEntityData : recordingEntityData) {
            if (!recordingEntityData.isTracked()) continue;
            animation.getRecordingDataList().add(recordingEntityData.getRecordingData());
        }

        playerSession.getScene().getAnimationManager().add(animation);
        Services.PACKET.sendToPlayer(
                getPlayer(), BiSyncNarrativeEntryPacket.add(animation.getId(), animation.toPayload()));

        return true;
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

    public List<RecordingEntityData> getRecordingEntityData() {
        return recordingEntityData;
    }
}
