package io.github.flemmli97.flan.fabric.mixin;

import io.github.flemmli97.flan.event.EntityInteractEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public abstract class LivingDamageMixin {

    @Inject(method = "hurt", at = @At(value = "HEAD"), cancellable = true)
    private void onDamage(DamageSource source, float amount, CallbackInfoReturnable<Boolean> info) {
        if (EntityInteractEvents.preventDamage((Entity) (Object) this, source)) {
            info.setReturnValue(false);
            info.cancel();
        }
    }
}
