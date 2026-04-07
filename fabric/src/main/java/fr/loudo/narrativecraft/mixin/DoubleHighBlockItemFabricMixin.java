package fr.loudo.narrativecraft.mixin;

import fr.loudo.narrativecraft.events.server.OnServerPlaceBlockEventFabric;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.DoubleHighBlockItem;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(DoubleHighBlockItem.class)
public class DoubleHighBlockItemFabricMixin {

    @Inject(method = "placeBlock", at = @At(value = "HEAD"))
    private void narrativecraft$atPlaceBlockStart(BlockPlaceContext context, BlockState placementState, CallbackInfoReturnable<Boolean> cir) {
        if (!context.getLevel().isClientSide() && context.getPlayer() instanceof ServerPlayer player) {
            OnServerPlaceBlockEventFabric.placeBlock(placementState, context.getClickedPos(), player);
        }
    }

    @Inject(method = "placeBlock", at = @At(value = "TAIL"))
    private void narrativecraft$atPlaceBlockEnd(BlockPlaceContext context, BlockState placementState, CallbackInfoReturnable<Boolean> cir) {
        if (!context.getLevel().isClientSide() && Boolean.TRUE.equals(cir.getReturnValue())
                && context.getPlayer() instanceof ServerPlayer player) {
            BlockPos upperPos = context.getClickedPos().above();
            BlockState upperState = placementState.setValue(BlockStateProperties.DOUBLE_BLOCK_HALF, DoubleBlockHalf.UPPER);
            OnServerPlaceBlockEventFabric.placeBlockSilently(upperState, upperPos, player);
        }
    }
}
