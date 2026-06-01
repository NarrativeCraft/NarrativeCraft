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

package fr.loudo.narrativecraft.api.recording.action;

import fr.loudo.narrativecraft.api.playback.IPlaybackContext;
import fr.loudo.narrativecraft.api.playback.IPlaybackSession;
import java.io.IOException;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;

public interface Action {

    ActionResult execute(IPlaybackContext context, IPlaybackSession session);

    interface Writer {
        void addByte(byte value) throws IOException;

        void addShort(short value) throws IOException;

        void addInt(int value) throws IOException;

        void addLong(long value) throws IOException;

        void addDouble(double value) throws IOException;

        void addFloat(float value) throws IOException;

        void addString(String value) throws IOException;

        void addBoolean(boolean value) throws IOException;

        void addUUID(UUID uuid) throws IOException;

        void addVec3(Vec3 pos) throws IOException;

        void addBlockPos(BlockPos blockPos) throws IOException;
    }

    interface Reader {
        byte readByte() throws IOException;

        short readShort() throws IOException;

        int readInt() throws IOException;

        double readDouble() throws IOException;

        long readLong() throws IOException;

        float readFloat() throws IOException;

        boolean readBoolean() throws IOException;

        String readString() throws IOException;

        UUID readUUID() throws IOException;

        Vec3 readVec3() throws IOException;

        BlockPos readBlockPos() throws IOException;
    }
}
