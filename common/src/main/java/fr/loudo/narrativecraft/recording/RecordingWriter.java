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

import fr.loudo.narrativecraft.recording.actions.AbstractAction;
import fr.loudo.narrativecraft.recording.actions.Action;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.UUID;
import net.minecraft.world.phys.Vec3;

public class RecordingWriter implements Action.Writer {

    public static final byte VERSION = 1;

    private final DataOutputStream outputStream;

    public RecordingWriter(DataOutputStream outputStream) {
        this.outputStream = outputStream;
    }

    public void writeHeader(UUID recordingId, UUID playerUUID, String name, int actionCount) throws IOException {
        outputStream.writeByte('N');
        outputStream.writeByte('C');
        outputStream.writeByte(VERSION);
        addUUID(recordingId);
        addUUID(playerUUID);
        outputStream.writeUTF(name);
        outputStream.writeInt(actionCount);
    }

    public void writeActionRecord(AbstractAction action) throws IOException {
        outputStream.writeInt(action.getTick());
        outputStream.writeByte(action.getType().getId());
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
}
