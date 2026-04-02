package fr.loudo.narrativecraft.recording.actions;

import fr.loudo.narrativecraft.api.playback.IPlaybackContext;
import fr.loudo.narrativecraft.api.recording.action.ActionResult;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.game.ClientboundBlockUpdatePacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public class SilentPlaceBlockAction extends DataBlockAction {

    public static final String ID = "silent_place_block";

    public SilentPlaceBlockAction(int tick) {
        super(tick);
    }

    public SilentPlaceBlockAction(int tick, BlockPos blockPos, BlockState blockState) {
        super(tick, blockPos, blockState);
    }

    @Override
    public String getId() {
        return ID;
    }

    @Override
    public ActionResult execute(IPlaybackContext context) {

        ServerLevel level = context.getLevel();

        if (context.forSpecificPlayers()) {
            for (ServerPlayer player : context.getTargetedPlayers()) {
                player.connection.send(new ClientboundBlockUpdatePacket(blockPos, blockState));
                if (blockState.is(Blocks.AIR)) {
                    return ActionResult.OK;
                }
            }
        } else {
            level.setBlock(blockPos, blockState, 3);
            if (blockState.is(Blocks.AIR)) {
                return ActionResult.OK;
            }
        }

        return ActionResult.OK;
    }
}
