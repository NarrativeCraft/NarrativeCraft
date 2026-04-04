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

import java.util.Collection;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;

public interface IPlaybackContext {

    /**
     * Determine if the action applies to everyone including the world, or if it's only for a group of players.
     * If it's for everyone, the action will be seen by everyone in the server and the world will be also affected.
     * However, if it only applies to a small group, the action will be only rendered client-side only to the group.
     * @return if it's for everyone or only a small group of players
     */
    boolean forSpecificPlayers();

    /**
     * If it returns an empty collection, it means the action applies to everyone. Else, the action only applies to the group returned.
     * You can check if the action can be played to everyone or a small group using {@link #forSpecificPlayers()}
     * @return The group of player to play the action
     */
    Collection<ServerPlayer> getTargetedPlayers();

    void respawnEntityByRecordingId(int recordingId);

    ServerLevel getLevel();

    Entity getEntity();

    /**
     * Returns the entity with the given recording ID, or null if not found.
     * Used by actions like RideEntityAction to reference other entities recorded in the same animation.
     */
    Entity getEntityByRecordingId(int recordingId);
}
