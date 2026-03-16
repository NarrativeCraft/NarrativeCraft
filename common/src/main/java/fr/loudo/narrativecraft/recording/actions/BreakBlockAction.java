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
import fr.loudo.narrativecraft.api.recording.action.ActionResult;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.game.ClientboundBlockUpdatePacket;
import net.minecraft.network.protocol.game.ClientboundLevelEventPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public class BreakBlockAction extends DataBlockAction {

    public BreakBlockAction(int tick, BlockPos blockPos, BlockState blockState) {
        super(tick, blockPos, blockState);
    }

    public BreakBlockAction(int tick) {
        super(tick);
    }

    @Override
    public ActionResult execute(IPlaybackContext context) {

        if (context.forSpecificPlayers()) {
            for (ServerPlayer player : context.getTargetedPlayers()) {
                player.connection.send(new ClientboundBlockUpdatePacket(blockPos, Blocks.AIR.defaultBlockState()));
                if (context.getLevel().getBlockState(blockPos).getBlock() != Blocks.AIR) {
                    player.connection.send(new ClientboundLevelEventPacket(
                            2001, // Block break + block break sound
                            blockPos,
                            Block.getId(blockState),
                            false));
                }
            }
        } else {
            context.getLevel().destroyBlock(blockPos, false);
        }

        return ActionResult.OK;
    }
}
