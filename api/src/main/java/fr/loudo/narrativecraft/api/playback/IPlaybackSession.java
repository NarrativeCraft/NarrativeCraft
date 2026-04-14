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

package fr.loudo.narrativecraft.api.playback;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Collection;

public interface IPlaybackSession {

    ServerLevel getLevel();

    /**
     * Returns the entity with the given recording ID, or null if not found.
     */
    Entity getEntityByRecordingId(int recordingId);

    void respawnEntityByRecordingId(int recordingId);

    /**
     * Whether the playback targets a specific group of players.
     * If false, actions affect everyone and the world.
     * If true, actions are rendered client-side only for {@link #getTargetedPlayers()}.
     */
    boolean forSpecificPlayers();

    /**
     * Returns the targeted players, or an empty collection if the playback affects everyone.
     */
    Collection<ServerPlayer> getTargetedPlayers();

    /**
     * Returns the virtual block state at the given position just before tick {@code tick} ran.
     * Falls back to the real world state if no entry exists in the log.
     */
    BlockState getBlockStateAtTick(BlockPos pos, int tick);

    /**
     * Records the block state at the given position after an action ran at {@code tick}.
     */
    void recordBlockState(int tick, BlockPos pos, BlockState state);

    /**
     * Removes all recorded block states at or after the given tick (inclusive).
     */
    void clearBlockStateLogFrom(int tick);
}
