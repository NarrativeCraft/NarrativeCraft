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

import fr.loudo.narrativecraft.api.recording.action.AbstractAction;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;

public class RecordingData {

    private static final List<String> TRANSIENT_NBT_KEYS = List.of(
            "Health",
            "HurtTime",
            "HurtByTimestamp",
            "DeathTime",
            "AbsorptionAmount",
            "ActiveEffects",
            "FoodEffects",
            "Fire",
            "Air",
            "Motion",
            "FallDistance",
            "AngerTime",
            "AngryAt");

    private final Map<Integer, List<AbstractAction>> actions = new HashMap<>();
    private final int recordingId;
    private final String entityId;
    /** Non-null only for tracked nearby entities; used to restore their exact state at playback spawn. */
    private CompoundTag initialNbt;

    private int spawnTick = 0;

    public RecordingData(int recordingId, Entity entity) {
        this.recordingId = recordingId;
        this.entityId = EntityType.getKey(entity.getType()).toString();
    }

    public RecordingData(int recordingId, String entityId) {
        this.recordingId = recordingId;
        this.entityId = entityId;
    }

    public void addAction(AbstractAction action) {
        actions.computeIfAbsent(action.getTick(), k -> new ArrayList<>()).add(action);
    }

    private CompoundTag sanitizeEntityNbt(CompoundTag nbt) {
        if (nbt == null) return null;
        CompoundTag sanitized = nbt.copy();
        TRANSIENT_NBT_KEYS.forEach(sanitized::remove);
        return sanitized;
    }

    public Map<Integer, List<AbstractAction>> getActions() {
        return actions;
    }

    public int getRecordingId() {
        return recordingId;
    }

    public String getEntityId() {
        return entityId;
    }

    public CompoundTag getInitialNbt() {
        return initialNbt;
    }

    public void setInitialNbt(CompoundTag initialNbt) {
        this.initialNbt = sanitizeEntityNbt(initialNbt);
    }

    public int getSpawnTick() {
        return spawnTick;
    }

    public void setSpawnTick(int spawnTick) {
        this.spawnTick = spawnTick;
    }
}
