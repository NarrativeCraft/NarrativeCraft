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

import com.google.common.collect.ImmutableBiMap;
import fr.loudo.narrativecraft.api.playback.IPlaybackContext;
import fr.loudo.narrativecraft.api.playback.IPlaybackSession;
import fr.loudo.narrativecraft.api.recording.action.AbstractAction;
import fr.loudo.narrativecraft.api.recording.action.ActionResult;
import fr.loudo.narrativecraft.utils.FakePlayer;
import java.io.IOException;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

public class RightClickBlockAction extends AbstractAction {

    public static final String ID = "right_click_block";

    private static final ImmutableBiMap<Direction, Byte> DIRECTION_IDS = ImmutableBiMap.<Direction, Byte>builder()
            .put(Direction.UP, (byte) 1)
            .put(Direction.DOWN, (byte) 2)
            .put(Direction.NORTH, (byte) 3)
            .put(Direction.SOUTH, (byte) 4)
            .put(Direction.WEST, (byte) 5)
            .put(Direction.EAST, (byte) 6)
            .build();

    private BlockHitResult blockHitResult;
    private boolean offHand;

    public static RightClickBlockAction defaultInstance(int tick, BlockPos blockPos) {
        BlockHitResult hitResult = new BlockHitResult(Vec3.ZERO, Direction.DOWN, blockPos, false);
        return new RightClickBlockAction(tick, hitResult, false);
    }

    public RightClickBlockAction(int tick, BlockHitResult blockHitResult, boolean offHand) {
        super(tick);
        this.blockHitResult = blockHitResult;
        this.offHand = offHand;
    }

    public RightClickBlockAction(int tick, BlockHitResult blockHitResult, InteractionHand hand) {
        super(tick);
        this.blockHitResult = blockHitResult;
        this.offHand = hand == InteractionHand.MAIN_HAND;
    }

    public RightClickBlockAction(int tick) {
        super(tick);
    }

    @Override
    public Optional<AbstractAction> createRewindSnapshot(IPlaybackContext context, IPlaybackSession session) {
        BlockState blockState = session.getLevel().getBlockState(blockHitResult.getBlockPos());
        // If the right-clicked block is a chest, then close it because it was previously opened
        if (blockState.is(Blocks.CHEST)) {
            return Optional.of(new CloseContainerAction(tick));
        }
        return Optional.of(new RightClickBlockAction(tick, blockHitResult, offHand));
    }

    @Override
    public void write(Writer writer) throws IOException {

        Byte directionId = DIRECTION_IDS.getOrDefault(blockHitResult.getDirection(), (byte) 2);
        if (directionId == null) throw new IOException("Direction id is null");

        writer.addVec3(blockHitResult.getLocation());
        writer.addBlockPos(blockHitResult.getBlockPos());
        writer.addByte((byte) directionId);

        writer.addBoolean(blockHitResult.isInside());
        writer.addBoolean(offHand);
    }

    @Override
    public void read(Reader reader) throws IOException {
        Vec3 pos = reader.readVec3();
        BlockPos blockPos = reader.readBlockPos();
        byte directionId = reader.readByte();
        Direction direction = DIRECTION_IDS.inverse().get(directionId);
        if (direction == null) throw new IOException("Direction is null, id is " + directionId);

        boolean inside = reader.readBoolean();

        blockHitResult = new BlockHitResult(pos, direction, blockPos, inside);
        offHand = reader.readBoolean();
    }

    @Override
    public String getId() {
        return ID;
    }

    @Override
    public ActionResult execute(IPlaybackContext context, IPlaybackSession session) {
        if (!(context.getEntity() instanceof FakePlayer player)) return ActionResult.IGNORED;

        InteractionHand hand = offHand ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND;
        ItemStack itemStack = player.getItemInHand(hand);

        BlockPos blockPos = blockHitResult.getBlockPos();
        BlockState blockState = session.getLevel().getBlockState(blockPos);

        InteractionResult result = blockState.useItemOn(itemStack, session.getLevel(), player, hand, blockHitResult);
        if (result != InteractionResult.TRY_WITH_EMPTY_HAND) return ActionResult.OK;

        // TODO: make this action also client-side for session.forSpecificPlayers(). seems to be a bit tricky,
        // unfortunately for now it can be only played
        // server-side.
        blockState.useWithoutItem(session.getLevel(), player, blockHitResult);

        return ActionResult.OK;
    }
}
