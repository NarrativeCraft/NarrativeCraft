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

package fr.loudo.narrativecraft.mixin;

import fr.loudo.narrativecraft.NarrativeCraftMod;
import fr.loudo.narrativecraft.recording.Recording;
import fr.loudo.narrativecraft.recording.RecordingEntityData;
import fr.loudo.narrativecraft.recording.actions.RideEntityAction;
import fr.loudo.narrativecraft.recording.actions.StopRideEntityAction;
import javax.annotation.Nullable;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public class EntityMixin {

    @Shadow
    private @Nullable Entity vehicle;

    @Inject(method = "startRiding(Lnet/minecraft/world/entity/Entity;)Z", at = @At("HEAD"))
    private void narrativecraft$startRiding(Entity p_20330_, CallbackInfoReturnable<Boolean> cir) {

        Entity entity = (Entity) (Object) this;
        Recording recording =
                NarrativeCraftMod.getInstance().getRecordingManager().getRecording(entity);
        if (recording == null) return;

        recording.addAction(new RideEntityAction(recording.getTick(), recording.markEntityAsTracked(vehicle)), entity);
    }

    @Inject(method = "stopRiding", at = @At("HEAD"))
    private void narrativecraft$stopRiding(CallbackInfo ci) {

        if (vehicle == null || vehicle.isRemoved()) return;

        Entity entity = (Entity) (Object) this;
        Recording recording =
                NarrativeCraftMod.getInstance().getRecordingManager().getRecording(entity);
        if (recording == null) return;
        RecordingEntityData recordingEntityData = recording.getRecordingEntityData(vehicle);
        if (recordingEntityData == null) return;

        recording.addAction(
                new StopRideEntityAction(recording.getTick(), recordingEntityData.getRecordingId()), entity);
    }
}
