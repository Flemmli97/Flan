package io.github.flemmli97.flan.mixin;

import io.github.flemmli97.flan.event.WorldEvents;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.raid.Raid;
import net.minecraft.world.entity.raid.Raids;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Raids.class)
public abstract class RaidManagerMixin {

    @Inject(method = "createOrExtendRaid", at = @At(value = "HEAD"), cancellable = true)
    private void checkRaid(ServerPlayer player, CallbackInfoReturnable<Raid> info) {
        if (!WorldEvents.canStartRaid(player)) {
            info.setReturnValue(null);
            info.cancel();
        }
    }
}
