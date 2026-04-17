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

import fr.loudo.narrativecraft.NarrativeCraftMod;
import fr.loudo.narrativecraft.api.recording.action.AbstractAction;
import fr.loudo.narrativecraft.api.recording.action.Action;
import fr.loudo.narrativecraft.api.recording.action.ActionType;
import fr.loudo.narrativecraft.utils.Utils;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.world.phys.Vec3;

public class RecordingWriter implements Action.Writer {

    public static final byte VERSION = 1;

    private final Map<String, Byte> localActionsIds = new HashMap<>();
    private final DataOutputStream output;

    public RecordingWriter(DataOutputStream output) {
        this.output = output;
    }

    public void writeHeader(UUID recordingId, String name, int entityCount, int totalTick, String characterRef)
            throws IOException {
        output.writeByte('N');
        output.writeByte('C');
        output.writeByte(VERSION);
        addUUID(recordingId);
        output.writeUTF(name);
        output.writeInt(totalTick);
        output.writeInt(entityCount);
        output.writeUTF(characterRef);
    }

    public void writeLocalActionsId() throws IOException {
        Collection<ActionType> actionTypes =
                NarrativeCraftMod.getInstance().getActionRegistry().getActionsType();
        writeActionSize((byte) actionTypes.size());
        byte id = 0;
        for (ActionType action :
                NarrativeCraftMod.getInstance().getActionRegistry().getActionsType()) {
            writeActionId(id, action.id());
            id++;
        }
    }

    public void writeActionSize(byte size) throws IOException {
        output.writeByte(size);
    }

    public void writeActionId(byte id, String actionId) throws IOException {
        output.writeByte(id);
        output.writeUTF(actionId);
        localActionsIds.put(actionId, id);
    }

    public void writeEntityHeader(RecordingEntityData recordingEntityData, int actionCount) throws IOException {
        writeEntityHeader(
                recordingEntityData.getRecordingId(),
                Utils.getEntityTypeString(recordingEntityData.getEntity()),
                recordingEntityData.getRecordingData().getInitialNbt(),
                recordingEntityData.getRecordingData().getSpawnTick(),
                actionCount);
    }

    public void writeEntityHeader(
            int recordingId, String entityType, CompoundTag initialNbt, int spawnTick, int actionCount)
            throws IOException {
        output.writeInt(recordingId);
        output.writeUTF(entityType);
        output.writeBoolean(initialNbt != null);
        if (initialNbt != null) {
            writeCompoundTag(initialNbt);
        }
        output.writeInt(spawnTick);
        output.writeInt(actionCount);
    }

    public void writeCompoundTag(CompoundTag tag) throws IOException {
        NbtIo.write(tag, output);
    }

    public void writeActionRecord(AbstractAction action) throws IOException {
        output.writeInt(action.getTick());
        byte id = localActionsIds.getOrDefault(action.getId(), (byte) -1);
        if (id == -1) {
            throw new IllegalArgumentException(
                    "Unknown action id " + action.getId() + ". Seems like you forgot to register your action!");
        }
        output.writeByte(id);
        action.write(this);
    }

    @Override
    public void addByte(byte value) throws IOException {
        output.writeByte(value);
    }

    @Override
    public void addShort(short value) throws IOException {
        output.writeShort(value);
    }

    @Override
    public void addInt(int value) throws IOException {
        output.writeInt(value);
    }

    @Override
    public void addLong(long value) throws IOException {
        output.writeLong(value);
    }

    @Override
    public void addDouble(double value) throws IOException {
        output.writeDouble(value);
    }

    @Override
    public void addFloat(float value) throws IOException {
        output.writeFloat(value);
    }

    @Override
    public void addString(String value) throws IOException {
        output.writeUTF(value);
    }

    @Override
    public void addBoolean(boolean value) throws IOException {
        output.writeBoolean(value);
    }

    @Override
    public void addUUID(UUID uuid) throws IOException {
        output.writeLong(uuid.getMostSignificantBits());
        output.writeLong(uuid.getLeastSignificantBits());
    }

    @Override
    public void addVec3(Vec3 pos) throws IOException {
        output.writeDouble(pos.x);
        output.writeDouble(pos.y);
        output.writeDouble(pos.z);
    }

    @Override
    public void addBlockPos(BlockPos blockPos) throws IOException {
        output.writeInt(blockPos.getX());
        output.writeInt(blockPos.getY());
        output.writeInt(blockPos.getZ());
    }
}
