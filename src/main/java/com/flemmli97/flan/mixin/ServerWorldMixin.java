package com.flemmli97.flan.mixin;

import com.flemmli97.flan.IClaimData;
import com.flemmli97.flan.claim.ClaimStorage;
import com.flemmli97.flan.event.WorldEvents;
import net.minecraft.entity.Entity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.explosion.Explosion;
import net.minecraft.world.explosion.ExplosionBehavior;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(ServerWorld.class)
public abstract class ServerWorldMixin implements IClaimData {
    @Unique
    private ClaimStorage claimData;

    @Inject(method = "<init>*", at = @At("RETURN"))
    private void initData(CallbackInfo info) {
        ServerWorld world = ((ServerWorld) (Object) this);
        this.claimData = new ClaimStorage(world.getServer(), world);
    }

    @Inject(method = "saveLevel()V", at = @At("RETURN"))
    private void saveClaimData(CallbackInfo info) {
        ServerWorld world = ((ServerWorld) (Object) this);
        this.claimData.save(world.getServer(), world.getRegistryKey());
    }

    @Inject(method = "createExplosion", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/explosion/Explosion;collectBlocksAndDamageEntities()V", shift = At.Shift.AFTER), locals = LocalCapture.CAPTURE_FAILHARD)
    private void explosionHook(Entity entity, DamageSource damageSource, ExplosionBehavior explosionBehavior, double d, double e, double f, float g, boolean bl, Explosion.DestructionType destructionType, CallbackInfoReturnable<Explosion> info, Explosion explosion) {
        WorldEvents.modifyExplosion(explosion, (ServerWorld) (Object) this);
    }

    @Override
    public ClaimStorage getClaimData() {
        return this.claimData;
    }
}
