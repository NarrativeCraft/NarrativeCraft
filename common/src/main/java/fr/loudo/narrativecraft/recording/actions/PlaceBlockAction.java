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

import fr.loudo.narrativecraft.playback.PlaybackContext;
import fr.loudo.narrativecraft.recording.RecordingActionType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.network.protocol.game.ClientboundBlockUpdatePacket;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;

public class PlaceBlockAction extends DataBlockAction {

    public PlaceBlockAction(int tick, BlockPos blockPos, BlockState blockState) {
        super(tick, blockPos, blockState);
    }

    public PlaceBlockAction(int tick) {
        super(tick);
    }

    @Override
    public RecordingActionType getType() {
        return RecordingActionType.PLACE_BLOCK;
    }

    @Override
    public ActionResult execute(PlaybackContext context) {

        ServerLevel level = context.getLevel();
        SoundType soundType = blockState.getSoundType();

        if (context.forSpecificPlayers()) {
            for (ServerPlayer player : context.getTargetedPlayers()) {
                player.connection.send(new ClientboundBlockUpdatePacket(blockPos, blockState));
                if (blockState.is(Blocks.AIR)) {
                    return ActionResult.OK;
                }

                player.connection.send(new ClientboundSoundPacket(
                        Holder.direct(soundType.getPlaceSound()),
                        SoundSource.BLOCKS,
                        blockPos.getX(),
                        blockPos.getY(),
                        blockPos.getZ(),
                        (soundType.getVolume() + 1.0f) / 2.0f,
                        soundType.getPitch() * 0.8f,
                        level.getRandom().nextLong()
                ));
            }
        } else {
            level.setBlock(blockPos, blockState, 3);
            if (blockState.is(Blocks.AIR)) {
                return ActionResult.OK;
            }

            level.playSound(
                    context.getEntity(),
                    blockPos,
                    soundType.getPlaceSound(),
                    SoundSource.BLOCKS,
                    (soundType.getVolume() + 1.0f) / 2.0f,
                    soundType.getPitch() * 0.8f);
        }

        return ActionResult.OK;
    }
}
