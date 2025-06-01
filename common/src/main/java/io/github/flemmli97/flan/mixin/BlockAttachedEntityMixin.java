package io.github.flemmli97.flan.mixin;

import io.github.flemmli97.flan.event.EntityInteractEvents;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.decoration.BlockAttachedEntity;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.ServerExplosion;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BlockAttachedEntity.class)
public abstract class BlockAttachedEntityMixin {

    @Inject(method = "ignoreExplosion", at = @At("HEAD"), cancellable = true)
    private void doIgnoreExplosion(Explosion explosion, CallbackInfoReturnable<Boolean> info) {
        if (explosion instanceof ServerExplosion serverExplosion && EntityInteractEvents.preventDamage((Entity) (Object) this, serverExplosion.getDamageSource())) {
            info.setReturnValue(true);
        }
    }
}
