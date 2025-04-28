package io.github.flemmli97.flan.fabric.mixin;

import io.github.flemmli97.flan.event.EntityInteractEvents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.boss.enderdragon.EndCrystal;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.vehicle.VehicleEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin({LivingEntity.class, ItemFrame.class, VehicleEntity.class, ArmorStand.class, EndCrystal.class, ItemEntity.class})
public abstract class EntityDamageMixin {

    @Inject(method = "hurtServer", at = @At(value = "HEAD"), cancellable = true)
    private void onDamage(ServerLevel level, DamageSource source, float amount, CallbackInfoReturnable<Boolean> info) {
        if (EntityInteractEvents.preventDamage((Entity) (Object) this, source)) {
            info.setReturnValue(false);
            info.cancel();
        }
    }
}
