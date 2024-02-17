package io.github.flemmli97.flan.mixin;

import io.github.flemmli97.flan.platform.integration.create.CreateCompat;
import net.minecraft.world.entity.vehicle.AbstractMinecart;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractMinecart.class)
public abstract class AbstractMinecartMixin {

    @Inject(method = "tick", at = @At("RETURN"))
    private void updateMovement(CallbackInfo info) {
        if (!CreateCompat.canMinecartPass((AbstractMinecart) (Object) this))
            ((AbstractMinecart) (Object) this).setDeltaMovement(((AbstractMinecart) (Object) this).getDeltaMovement().scale(-1));
    }
}
