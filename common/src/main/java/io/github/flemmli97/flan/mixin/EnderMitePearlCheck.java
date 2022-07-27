package io.github.flemmli97.flan.mixin;

import io.github.flemmli97.flan.event.PlayerEvents;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.entity.projectile.ThrownEnderpearl;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(ThrownEnderpearl.class)
public abstract class EnderMitePearlCheck extends ThrowableItemProjectile {

    private EnderMitePearlCheck(EntityType<? extends ThrowableItemProjectile> entityType, Level level) {
        super(entityType, level);
    }

    @ModifyConstant(method = "onHit", constant = @Constant(floatValue = 0.05f))
    private float newChance(float old) {
        return PlayerEvents.canSpawnFromPlayer(this.getOwner(), old);
    }

}
