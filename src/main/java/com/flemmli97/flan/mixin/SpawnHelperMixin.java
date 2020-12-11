package com.flemmli97.flan.mixin;

import com.flemmli97.flan.event.WorldEvents;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.SpawnHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(SpawnHelper.class)
public class SpawnHelperMixin {

    @Inject(method = "isValidSpawn", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/mob/MobEntity;canSpawn(Lnet/minecraft/world/WorldAccess;Lnet/minecraft/entity/SpawnReason;)Z"), cancellable = true)
    private static void isValidSpawn(ServerWorld world, MobEntity entity, double squaredDistance, CallbackInfoReturnable<Boolean> info) {
        if (WorldEvents.preventMobSpawn(world, entity)) {
            info.setReturnValue(false);
            info.cancel();
        }
    }
}
