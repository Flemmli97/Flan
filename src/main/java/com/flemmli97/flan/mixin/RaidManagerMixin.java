package com.flemmli97.flan.mixin;

import com.flemmli97.flan.event.WorldEvents;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.village.raid.Raid;
import net.minecraft.village.raid.RaidManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(RaidManager.class)
public class RaidManagerMixin {

    @Inject(method = "startRaid", at = @At(value = "HEAD"), cancellable = true)
    public void checkRaid(ServerPlayerEntity player, CallbackInfoReturnable<Raid> info) {
        if (!WorldEvents.canStartRaid(player)) {
            info.setReturnValue(null);
            info.cancel();
        }
    }
}
