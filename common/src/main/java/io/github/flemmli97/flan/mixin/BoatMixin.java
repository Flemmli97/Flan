package io.github.flemmli97.flan.mixin;

import io.github.flemmli97.flan.event.EntityInteractEvents;
import net.minecraft.world.entity.vehicle.AbstractBoat;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractBoat.class)
public class BoatMixin {

    @Inject(method = "tick", at = @At("RETURN"))
    private void updateMovement(CallbackInfo info) {
        EntityInteractEvents.handleVehiclePass((AbstractBoat) (Object) this);
    }
}
