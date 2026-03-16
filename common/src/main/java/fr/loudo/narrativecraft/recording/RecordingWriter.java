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

import fr.loudo.narrativecraft.api.NarrativeCraftAPI;
import fr.loudo.narrativecraft.api.recording.action.AbstractAction;
import fr.loudo.narrativecraft.api.recording.action.Action;
import fr.loudo.narrativecraft.utils.Utils;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;

public class RecordingWriter implements Action.Writer {

    public static final byte VERSION = 1;

    private final DataOutputStream outputStream;

    public RecordingWriter(DataOutputStream outputStream) {
        this.outputStream = outputStream;
    }

    public void writeHeader(UUID recordingId, String name, int entityCount) throws IOException {
        outputStream.writeByte('N');
        outputStream.writeByte('C');
        outputStream.writeByte(VERSION);
        addUUID(recordingId);
        outputStream.writeUTF(name);
        outputStream.writeInt(entityCount);
    }

    public void writeEntityHeader(RecordingEntityData recordingEntityData, int actionCount) throws IOException {
        outputStream.writeInt(recordingEntityData.getRecordingId());
        outputStream.writeUTF(Utils.getEntityTypeString(recordingEntityData.getEntity()));
        outputStream.writeInt(actionCount);
    }

    public void writeActionRecord(AbstractAction action) throws IOException {
        outputStream.writeInt(action.getTick());
        int id = NarrativeCraftAPI.getInstance().getRegistry().getId(action.getClass());
        outputStream.writeByte(id);
        action.write(this);
    }

    @Override
    public void addByte(byte value) throws IOException {
        outputStream.writeByte(value);
    }

    @Override
    public void addInt(int value) throws IOException {
        outputStream.writeInt(value);
    }

    @Override
    public void addLong(long value) throws IOException {
        outputStream.writeLong(value);
    }

    @Override
    public void addDouble(double value) throws IOException {
        outputStream.writeDouble(value);
    }

    @Override
    public void addFloat(float value) throws IOException {
        outputStream.writeFloat(value);
    }

    @Override
    public void addString(String value) throws IOException {
        outputStream.writeUTF(value);
    }

    @Override
    public void addBoolean(boolean value) throws IOException {
        outputStream.writeBoolean(value);
    }

    @Override
    public void addUUID(UUID uuid) throws IOException {
        outputStream.writeLong(uuid.getMostSignificantBits());
        outputStream.writeLong(uuid.getLeastSignificantBits());
    }

    @Override
    public void addVec3(Vec3 pos) throws IOException {
        outputStream.writeDouble(pos.x);
        outputStream.writeDouble(pos.y);
        outputStream.writeDouble(pos.z);
    }

    @Override
    public void addBlockPos(BlockPos blockPos) throws IOException {
        outputStream.writeInt(blockPos.getX());
        outputStream.writeInt(blockPos.getY());
        outputStream.writeInt(blockPos.getZ());
    }
}
