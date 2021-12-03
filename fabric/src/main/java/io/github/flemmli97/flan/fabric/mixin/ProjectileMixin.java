package io.github.flemmli97.flan.fabric.mixin;

import io.github.flemmli97.flan.event.EntityInteractEvents;
import net.minecraft.world.entity.projectile.DragonFireball;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ThrownEgg;
import net.minecraft.world.entity.projectile.ThrownEnderpearl;
import net.minecraft.world.entity.projectile.ThrownExperienceBottle;
import net.minecraft.world.entity.projectile.ThrownPotion;
import net.minecraft.world.phys.HitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = {Projectile.class, ThrownEgg.class, ThrownPotion.class, ThrownExperienceBottle.class, DragonFireball.class, ThrownEnderpearl.class})
public abstract class ProjectileMixin {

    @Inject(method = "onHit", at = @At(value = "HEAD"), cancellable = true)
    private void collision(HitResult hitResult, CallbackInfo info) {
        if (EntityInteractEvents.projectileHit((Projectile) (Object) this, hitResult)) {
            info.cancel();
        }
    }
}
