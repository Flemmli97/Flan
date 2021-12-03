package io.github.flemmli97.flan.fabric.mixin;

import io.github.flemmli97.flan.event.WorldEvents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.ExplosionDamageCalculator;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(ServerLevel.class)
public abstract class ServerWorldMixin {

    @Inject(method = "explode", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Explosion;explode()V", shift = At.Shift.AFTER), locals = LocalCapture.CAPTURE_FAILHARD)
    private void explosionHook(Entity entity, DamageSource damageSource, ExplosionDamageCalculator explosionBehavior, double d, double e, double f, float g, boolean bl, Explosion.BlockInteraction destructionType, CallbackInfoReturnable<Explosion> info, Explosion explosion) {
        WorldEvents.modifyExplosion(explosion, (ServerLevel) (Object) this);
    }
}
