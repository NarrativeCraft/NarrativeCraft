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

package fr.loudo.narrativecraft.events.server;

import fr.loudo.narrativecraft.NarrativeCraftMod;
import fr.loudo.narrativecraft.recording.RecordingEntityData;
import fr.loudo.narrativecraft.recording.actions.RightClickBlockAction;
import fr.loudo.narrativecraft.recording.actions.UseItemOnBlockAction;
import fr.loudo.narrativecraft.signals.SignalPlayerRightClickBlock;
import fr.loudo.narrativecraft.signals.SignalPlayerUseItemOnBlock;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

public class OnRightClickBlockEvent {

    public static void rightClickBlock(ServerPlayer player, InteractionHand hand, BlockHitResult hitResult) {
        handleSignal(player, hand, hitResult);
        handleRecording(player, hand, hitResult);
    }

    private static void handleSignal(ServerPlayer player, InteractionHand hand, BlockHitResult hitResult) {
        BlockState state = player.level().getBlockState(hitResult.getBlockPos());
        NarrativeCraftMod.getInstance()
                .getSignalEmitter()
                .emit(new SignalPlayerRightClickBlock(state, hitResult.getBlockPos()), player);

        ItemStack itemStack = player.getItemInHand(hand);
        if (itemStack.isEmpty()) return;

        NarrativeCraftMod.getInstance()
                .getSignalEmitter()
                .emit(new SignalPlayerUseItemOnBlock(itemStack, state, hitResult.getBlockPos()), player);
    }

    private static void handleRecording(ServerPlayer player, InteractionHand hand, BlockHitResult hitResult) {
        RecordingEntityData data =
                NarrativeCraftMod.getInstance().getRecordingManager().getRecordingEntityData(player);
        if (data == null) return;

        data.addAction(new RightClickBlockAction(data.getRecordingTick(), hitResult, hand));

        ItemStack itemStack = player.getItemInHand(hand);
        if (!itemStack.isEmpty()) {
            data.addAction(new UseItemOnBlockAction(data.getRecordingTick(), hitResult, hand));
        }
        data.setLastInteractedBlockPos(hitResult.getBlockPos());
    }
}
