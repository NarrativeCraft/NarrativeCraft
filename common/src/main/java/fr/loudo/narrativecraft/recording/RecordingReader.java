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
import java.io.DataInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import net.minecraft.world.phys.Vec3;

public class RecordingReader implements Action.Reader {

    private final DataInputStream inputStream;

    public RecordingReader(DataInputStream inputStream) {
        this.inputStream = inputStream;
    }

    @Override
    public byte readByte() throws IOException {
        return inputStream.readByte();
    }

    @Override
    public long readLong() throws IOException {
        return inputStream.readLong();
    }

    @Override
    public int readInt() throws IOException {
        return inputStream.readInt();
    }

    @Override
    public double readDouble() throws IOException {
        return inputStream.readDouble();
    }

    @Override
    public float readFloat() throws IOException {
        return inputStream.readFloat();
    }

    @Override
    public UUID readUUID() throws IOException {
        return new UUID(inputStream.readLong(), inputStream.readLong());
    }

    @Override
    public Vec3 readVec3() throws IOException {
        return new Vec3(inputStream.readDouble(), inputStream.readDouble(), inputStream.readDouble());
    }

    public record RecordingHeader(UUID recordingId, UUID playerUUID, int actionCount) {}

    public RecordingHeader readHeader() throws IOException {
        byte magic0 = inputStream.readByte();
        byte magic1 = inputStream.readByte();
        if (magic0 != 'N' || magic1 != 'C') {
            throw new IOException("Invalid .ncr file: bad magic bytes");
        }
        inputStream.readByte(); // version, reserved for future format migrations
        UUID recordingId = readUUID();
        UUID playerUUID = readUUID();
        int actionCount = inputStream.readInt();
        return new RecordingHeader(recordingId, playerUUID, actionCount);
    }

    public List<AbstractAction> readAllActions(int count) throws IOException {
        List<AbstractAction> result = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            int tick = inputStream.readInt();
            int id = inputStream.readByte() & 0xFF;
            AbstractAction action = RecordingActionType.getById(id).createAction(tick);
            action.read(this);
            result.add(action);
        }
        return result;
    }
}
