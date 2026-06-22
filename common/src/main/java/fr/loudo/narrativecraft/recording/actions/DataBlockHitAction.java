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

package fr.loudo.narrativecraft.recording.actions;

import com.google.common.collect.ImmutableBiMap;
import fr.loudo.narrativecraft.api.recording.action.AbstractAction;
import java.io.IOException;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

public abstract class DataBlockHitAction extends AbstractAction {

    private static final ImmutableBiMap<Direction, Byte> DIRECTION_IDS = ImmutableBiMap.<Direction, Byte>builder()
            .put(Direction.UP, (byte) 1)
            .put(Direction.DOWN, (byte) 2)
            .put(Direction.NORTH, (byte) 3)
            .put(Direction.SOUTH, (byte) 4)
            .put(Direction.WEST, (byte) 5)
            .put(Direction.EAST, (byte) 6)
            .build();

    protected BlockHitResult hitResult;
    protected boolean offhand;

    public DataBlockHitAction(int tick, BlockHitResult hitResult, InteractionHand hand) {
        super(tick);
        this.hitResult = hitResult;
        this.offhand = hand == InteractionHand.OFF_HAND;
    }

    public DataBlockHitAction(int tick) {
        super(tick);
    }

    @Override
    public void write(Writer writer) throws IOException {
        Byte directionId = DIRECTION_IDS.getOrDefault(hitResult.getDirection(), (byte) 2);
        if (directionId == null) throw new IOException("Direction id is null");

        writer.addVec3(hitResult.getLocation());
        writer.addBlockPos(hitResult.getBlockPos());
        writer.addByte(directionId);
        writer.addBoolean(hitResult.isInside());
        writer.addBoolean(offhand);
    }

    @Override
    public void read(Reader reader) throws IOException {
        Vec3 pos = reader.readVec3();
        BlockPos blockPos = reader.readBlockPos();
        byte directionId = reader.readByte();
        Direction direction = DIRECTION_IDS.inverse().get(directionId);
        if (direction == null) throw new IOException("Direction is null, id is " + directionId);

        boolean inside = reader.readBoolean();
        hitResult = new BlockHitResult(pos, direction, blockPos, inside);
        offhand = reader.readBoolean();
    }
}
