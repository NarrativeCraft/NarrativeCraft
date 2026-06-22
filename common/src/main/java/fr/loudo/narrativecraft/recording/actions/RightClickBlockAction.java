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

import fr.loudo.narrativecraft.api.playback.IPlaybackContext;
import fr.loudo.narrativecraft.api.playback.IPlaybackSession;
import fr.loudo.narrativecraft.api.recording.action.AbstractAction;
import fr.loudo.narrativecraft.api.recording.action.ActionResult;
import fr.loudo.narrativecraft.utils.FakePlayer;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

public class RightClickBlockAction extends DataBlockHitAction {

    public static final String ID = "right_click_block";

    public static RightClickBlockAction defaultInstance(int tick, BlockPos blockPos) {
        BlockHitResult hitResult = new BlockHitResult(Vec3.ZERO, Direction.DOWN, blockPos, false);
        return new RightClickBlockAction(tick, hitResult, InteractionHand.MAIN_HAND);
    }

    public RightClickBlockAction(int tick, BlockHitResult hitResult, InteractionHand hand) {
        super(tick, hitResult, hand);
    }

    public RightClickBlockAction(int tick) {
        super(tick);
    }

    @Override
    public List<AbstractAction> createRewindSnapshot(IPlaybackContext context, IPlaybackSession session) {
        BlockState blockState = session.getLevel().getBlockState(hitResult.getBlockPos());
        if (blockState.is(Blocks.CHEST)) {
            return List.of(new CloseContainerAction(tick));
        }
        return List.of(new RightClickBlockAction(
                tick, hitResult, offhand ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND));
    }

    @Override
    public String getId() {
        return ID;
    }

    @Override
    public ActionResult execute(IPlaybackContext context, IPlaybackSession session) {
        if (!(context.getEntity() instanceof FakePlayer player)) return ActionResult.IGNORED;

        InteractionHand hand = offhand ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND;
        ItemStack itemStack = player.getItemInHand(hand);

        BlockPos blockPos = hitResult.getBlockPos();
        BlockState blockState = session.getLevel().getBlockState(blockPos);

        ItemInteractionResult result = blockState.useItemOn(itemStack, session.getLevel(), player, hand, hitResult);

        // TODO: make this action also client-side for session.forSpecificPlayers(). seems to be a bit tricky,
        // unfortunately for now it can be only played server-side.
        blockState.useWithoutItem(session.getLevel(), player, hitResult);

        return ActionResult.OK;
    }
}
