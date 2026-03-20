package fr.loudo.narrativecraft.mixin;

import fr.loudo.narrativecraft.NarrativeCraftMod;
import fr.loudo.narrativecraft.recording.Recording;
import fr.loudo.narrativecraft.recording.actions.DestroyBlockStageAction;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerLevel.class)
public class ServerLevelMixin {

    @Inject(method = "destroyBlockProgress", at = @At("RETURN"))
    private void narrativecraft$destroyBlockProgress(int breakerId, BlockPos pos, int progress, CallbackInfo ci) {
        for (Recording recording : NarrativeCraftMod.getInstance().getRecordingManager().getList()) {
            if (!recording.isRecording()) continue;
            if (recording.getPlayer().getId() != breakerId) continue;

            recording.addAction(new DestroyBlockStageAction(recording.getTick(), pos, progress), recording.getPlayer());
        }
    }

}
