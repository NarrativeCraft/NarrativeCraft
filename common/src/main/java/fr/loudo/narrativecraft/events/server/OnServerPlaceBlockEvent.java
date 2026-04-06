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

package fr.loudo.narrativecraft.events.server;

import fr.loudo.narrativecraft.NarrativeCraftMod;
import fr.loudo.narrativecraft.recording.RecordingEntityData;
import fr.loudo.narrativecraft.recording.actions.PlaceBlockAction;
import fr.loudo.narrativecraft.recording.actions.SilentPlaceBlockAction;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BedPart;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;

public class OnServerPlaceBlockEvent {
    public static void onPlaceBlock(BlockState state, BlockPos clickedPos, ServerPlayer player) {

        RecordingEntityData data =
                NarrativeCraftMod.getInstance().getRecordingManager().getRecordingEntityData(player);
        if (data == null) return;

        data.addAction(new PlaceBlockAction(data.getRecordingTick(), clickedPos, state));

        ServerLevel level = player.level();
        BlockPos secondPos = null;

        if (state.getBlock() instanceof DoorBlock && state.getValue(DoorBlock.HALF) == DoubleBlockHalf.LOWER) {
            secondPos = clickedPos.above();
        } else if (state.getBlock() instanceof BedBlock && state.getValue(BedBlock.PART) == BedPart.FOOT) {
            Direction facing = state.getValue(BedBlock.FACING);
            secondPos = clickedPos.relative(facing);
        }

        if (secondPos != null) {
            BlockState secondState = level.getBlockState(secondPos);
            if (!secondState.isAir()) {
                data.addAction(new SilentPlaceBlockAction(data.getRecordingTick(), secondPos, secondState));
            }
        }
    }
}
