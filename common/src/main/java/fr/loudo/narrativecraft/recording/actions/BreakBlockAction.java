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

package fr.loudo.narrativecraft.recording.actions;

import fr.loudo.narrativecraft.api.playback.IPlaybackContext;
import fr.loudo.narrativecraft.api.playback.IPlaybackSession;
import fr.loudo.narrativecraft.api.recording.action.AbstractAction;
import fr.loudo.narrativecraft.api.recording.action.ActionResult;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.game.ClientboundBlockUpdatePacket;
import net.minecraft.network.protocol.game.ClientboundLevelEventPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;

public class BreakBlockAction extends DataBlockAction {

    public static final String ID = "break_block";

    public BreakBlockAction(int tick, BlockPos blockPos, BlockState blockState) {
        super(tick, blockPos, blockState);
    }

    public BreakBlockAction(int tick) {
        super(tick);
    }

    @Override
    public ActionResult execute(IPlaybackContext context, IPlaybackSession session) {

        if (session.forSpecificPlayers()) {
            for (ServerPlayer player : session.getTargetedPlayers()) {
                player.connection.send(new ClientboundBlockUpdatePacket(blockPos, Blocks.AIR.defaultBlockState()));
                player.connection.send(new ClientboundLevelEventPacket(
                        2001, // Block break + block break sound
                        blockPos,
                        Block.getId(blockState),
                        false));
            }
        } else {
            handleDoorBreak(session.getLevel());
            session.getLevel().destroyBlock(blockPos, false, context.getEntity());
        }

        return ActionResult.OK;
    }

    /*
    Prevent specific case of door item dropping if breaking from the top
     */
    private void handleDoorBreak(ServerLevel level) {
        if (blockState.hasProperty(BlockStateProperties.DOUBLE_BLOCK_HALF)) {
            DoubleBlockHalf half = blockState.getValue(BlockStateProperties.DOUBLE_BLOCK_HALF);
            if (half == DoubleBlockHalf.UPPER) {
                BlockPos below = blockPos.below();
                level.setBlock(below, Blocks.AIR.defaultBlockState(), Block.UPDATE_CLIENTS);
            }
        }
    }

    @Override
    public Optional<AbstractAction> createRewindSnapshot(IPlaybackContext context, IPlaybackSession session) {
        return Optional.of(new SilentPlaceBlockAction(tick, blockPos, blockState));
    }

    @Override
    public String getId() {
        return ID;
    }
}
