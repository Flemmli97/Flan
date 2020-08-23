package com.flemmli97.flan.mixin;

import com.flemmli97.flan.event.EntityInteractEvents;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.util.hit.HitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ProjectileEntity.class)
public abstract class ProjectileMixin {

    @Inject(method = "onCollision", at = @At(value = "HEAD"), cancellable = true)
    public void collision(HitResult hitResult, CallbackInfo info) {
        if (EntityInteractEvents.projectileHit((ProjectileEntity) (Object) this, hitResult)) {
            info.cancel();
        }
    }
}
