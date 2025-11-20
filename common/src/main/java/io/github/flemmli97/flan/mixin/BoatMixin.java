package io.github.flemmli97.flan.mixin;

import io.github.flemmli97.flan.event.EntityInteractEvents;
import io.github.flemmli97.flan.utils.VehiclePositionTracker;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.entity.vehicle.VehicleEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Boat.class)
public abstract class BoatMixin extends VehicleEntity implements VehiclePositionTracker {

    @Unique
    private Vec3 lastPosition;

    private BoatMixin(EntityType<?> entityType, Level level) {
        super(entityType, level);
    }

    @Inject(method = "tick", at = @At("RETURN"))
    private void updateMovement(CallbackInfo info) {
        EntityInteractEvents.handleVehiclePass((Boat) (Object) this);
    }

    @Override
    public Vec3 flan$updateAndGetLastPosition() {
        Vec3 pos = this.lastPosition;
        if (pos == null) {
            pos = this.position();
        }
        this.lastPosition = this.position();
        return pos;
    }
}
