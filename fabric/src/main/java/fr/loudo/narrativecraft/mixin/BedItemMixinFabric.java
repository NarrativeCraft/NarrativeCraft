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

package fr.loudo.narrativecraft.mixin;

import fr.loudo.narrativecraft.events.server.OnServerPlaceBlockEventFabric;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.BedItem;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BedPart;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BedItem.class)
public class BedItemMixinFabric {

    @Inject(method = "placeBlock", at = @At(value = "HEAD"))
    private void narrativecraft$atPlaceBlockStart(
            BlockPlaceContext context, BlockState state, CallbackInfoReturnable<Boolean> cir) {
        if (!context.getLevel().isClientSide()) {
            OnServerPlaceBlockEventFabric.placeBlock(
                    state, context.getClickedPos(), (ServerPlayer) context.getPlayer());
        }
    }

    @Inject(method = "placeBlock", at = @At(value = "TAIL"))
    private void narrativecraft$atPlaceBlockEnd(
            BlockPlaceContext context, BlockState state, CallbackInfoReturnable<Boolean> cir) {
        if (!context.getLevel().isClientSide() && Boolean.TRUE.equals(cir.getReturnValue())) {
            BlockPos headPos = context.getClickedPos().relative(state.getValue(BedBlock.FACING));
            BlockState headState = state.setValue(BedBlock.PART, BedPart.HEAD);
            OnServerPlaceBlockEventFabric.placeBlockSilently(headState, headPos, (ServerPlayer) context.getPlayer());
        }
    }
}
