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
import fr.loudo.narrativecraft.api.recording.action.AbstractAction;
import fr.loudo.narrativecraft.api.recording.action.Action;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;
import net.minecraft.world.phys.Vec3;

public class RecordingReader implements Action.Reader {

    private final Map<Byte, String> localActionsIds = new HashMap<>();
    private final DataInputStream input;

    public RecordingReader(DataInputStream input) {
        this.input = input;
    }

    @Override
    public byte readByte() throws IOException {
        return input.readByte();
    }

    @Override
    public short readShort() throws IOException {
        return input.readShort();
    }

    @Override
    public long readLong() throws IOException {
        return input.readLong();
    }

    @Override
    public int readInt() throws IOException {
        return input.readInt();
    }

    @Override
    public double readDouble() throws IOException {
        return input.readDouble();
    }

    @Override
    public float readFloat() throws IOException {
        return input.readFloat();
    }

    @Override
    public boolean readBoolean() throws IOException {
        return input.readBoolean();
    }

    @Override
    public String readString() throws IOException {
        return input.readUTF();
    }

    @Override
    public UUID readUUID() throws IOException {
        return new UUID(input.readLong(), input.readLong());
    }

    @Override
    public Vec3 readVec3() throws IOException {
        return new Vec3(input.readDouble(), input.readDouble(), input.readDouble());
    }

    @Override
    public BlockPos readBlockPos() throws IOException {
        return new BlockPos(input.readInt(), input.readInt(), input.readInt());
    }

    public record RecordingHeader(UUID recordingId, String name, int entityCount, int totalTick, UUID characterId) {}

    public record EntityHeader(
            int entityRecordingId, String entityType, CompoundTag initialNbt, int spawnTick, int actionCount) {}

    public RecordingHeader readHeader() throws IOException {
        byte magic0 = input.readByte();
        byte magic1 = input.readByte();
        if (magic0 != 'N' || magic1 != 'C') {
            throw new IOException("Invalid .ncr file: bad magic bytes");
        }
        input.readByte(); // version, reserved for future format migrations
        UUID recordingId = readUUID();
        String name = input.readUTF();
        int totalTick = input.readInt();
        int entityCount = input.readInt();
        UUID characterId = readUUID();
        return new RecordingHeader(recordingId, name, entityCount, totalTick, characterId);
    }

    public Map<Byte, String> readLocalActionsId() throws IOException {
        byte actionsCount = input.readByte();
        for (int i = 0; i < actionsCount; i++) {
            byte id = input.readByte();
            String actionId = input.readUTF();
            localActionsIds.put(id, actionId);
        }
        return localActionsIds;
    }

    public EntityHeader readEntityHeader() throws IOException {
        int entityRecordingId = input.readInt();
        String entityType = input.readUTF();
        boolean hasInitialNbt = input.readBoolean();
        CompoundTag initialNbt = hasInitialNbt ? readCompoundTag() : null;
        int spawnTick = input.readInt();
        int actionCount = input.readInt();
        return new EntityHeader(entityRecordingId, entityType, initialNbt, spawnTick, actionCount);
    }

    public CompoundTag readCompoundTag() throws IOException {
        return NbtIo.read(input, NbtAccounter.unlimitedHeap());
    }

    public List<AbstractAction> readAllActions(int count) throws IOException {
        List<AbstractAction> result = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            int tick = input.readInt();
            byte id = input.readByte();
            String actionId = localActionsIds.get(id);
            AbstractAction action =
                    NarrativeCraftMod.getInstance().getActionRegistry().createAction(actionId, tick);
            if (action == null) {
                throw new IOException(String.format("Action with id %s not found", actionId));
            }
            action.read(this);
            result.add(action);
        }
        return result;
    }
}
