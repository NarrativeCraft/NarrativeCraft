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

package fr.loudo.narrativecraft.managers;

import fr.loudo.narrativecraft.api.managers.IRecordingManager;
import fr.loudo.narrativecraft.api.recording.IRecording;
import fr.loudo.narrativecraft.recording.Recording;
import fr.loudo.narrativecraft.recording.RecordingEntityData;
import java.util.UUID;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;

public class RecordingManager extends Manager<Recording> implements IRecordingManager {

    public Recording getById(UUID id) {
        for (Recording recording : list) {
            if (recording.getId().equals(id)) {
                return recording;
            }
        }
        return null;
    }

    public boolean isRecording(ServerPlayer player) {
        for (Recording recording : list) {
            if (recording.getPlayer().getUUID().equals(player.getUUID()) && recording.isRecording()) {
                return true;
            }
        }
        return false;
    }

    public Recording getRecording(ServerPlayer player) {
        for (Recording recording : list) {
            if (recording.getPlayer().getUUID().equals(player.getUUID())) {
                return recording;
            }
        }
        return null;
    }

    @Override
    public IRecording getRecording(UUID playerId) {
        for (Recording recording : list) {
            if (recording.getPlayer().getUUID().equals(playerId)) {
                return recording;
            }
        }
        return null;
    }

    @Override
    public Recording getRecording(Entity entity) {
        for (Recording recording : list) {
            for (RecordingEntityData recordingEntityData : recording.getRecordingEntityDataList()) {
                if (recordingEntityData.getEntity().getUUID().equals(entity.getUUID())) {
                    return recording;
                }
            }
        }
        return null;
    }

    public RecordingEntityData getRecordingEntityData(Entity entity) {
        for (Recording recording : list) {
            for (RecordingEntityData data : recording.getRecordingEntityDataList()) {
                if (data.getEntity().getUUID().equals(entity.getUUID())) {
                    return data;
                }
            }
        }
        return null;
    }
}
