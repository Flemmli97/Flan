package io.github.flemmli97.flan.mixin;

import io.github.flemmli97.flan.event.EntityInteractEvents;
import net.minecraft.entity.projectile.DragonFireballEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.entity.projectile.thrown.EggEntity;
import net.minecraft.entity.projectile.thrown.EnderPearlEntity;
import net.minecraft.entity.projectile.thrown.ExperienceBottleEntity;
import net.minecraft.entity.projectile.thrown.PotionEntity;
import net.minecraft.util.hit.HitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = {ProjectileEntity.class, EggEntity.class, PotionEntity.class, ExperienceBottleEntity.class, DragonFireballEntity.class, EnderPearlEntity.class})
public abstract class ProjectileMixin {

    @Inject(method = "onCollision", at = @At(value = "HEAD"), cancellable = true)
    private void collision(HitResult hitResult, CallbackInfo info) {
        if (EntityInteractEvents.projectileHit((ProjectileEntity) (Object) this, hitResult)) {
            info.cancel();
        }
    }
}
