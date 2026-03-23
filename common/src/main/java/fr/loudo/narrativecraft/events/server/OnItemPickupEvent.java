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
import fr.loudo.narrativecraft.playback.Playback;
import fr.loudo.narrativecraft.recording.Recording;
import fr.loudo.narrativecraft.recording.actions.ItemPickupAction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.item.ItemEntity;

public class OnItemPickupEvent {

    public static void onPickup(ServerPlayer player, ItemEntity itemEntity) {
        Recording recording =
                NarrativeCraftMod.getInstance().getRecordingManager().getRecording(player);
        if (recording != null) {
            ItemPickupAction action = new ItemPickupAction(
                    recording.getTick(),
                    recording.markEntityAsTracked(itemEntity),
                    itemEntity.getItem().getCount());
            recording.addAction(action, player);
        }
    }

    public static boolean canPickup(ItemEntity itemEntity) {
        for (Playback playback :
                NarrativeCraftMod.getInstance().getPlaybackManager().getActivePlaybacks()) {
            if (playback.entityFromPlayback(itemEntity)) {
                return false;
            }
        }
        return true;
    }
}
