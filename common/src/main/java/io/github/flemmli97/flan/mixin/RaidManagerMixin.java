package io.github.flemmli97.flan.mixin;

import io.github.flemmli97.flan.event.WorldEvents;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.village.raid.Raid;
import net.minecraft.village.raid.RaidManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(RaidManager.class)
public abstract class RaidManagerMixin {

    @Inject(method = "startRaid", at = @At(value = "HEAD"), cancellable = true)
    private void checkRaid(ServerPlayerEntity player, CallbackInfoReturnable<Raid> info) {
        if (!WorldEvents.canStartRaid(player)) {
            info.setReturnValue(null);
            info.cancel();
        }
    }
}
