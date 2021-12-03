package io.github.flemmli97.flan.fabric.mixin;

import io.github.flemmli97.flan.event.WorldEvents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.NaturalSpawner;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(NaturalSpawner.class)
public abstract class SpawnHelperMixin {

    @Inject(method = "isValidPositionForMob", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Mob;checkSpawnRules(Lnet/minecraft/world/level/LevelAccessor;Lnet/minecraft/world/entity/MobSpawnType;)Z"), cancellable = true)
    private static void isValidSpawn(ServerLevel world, Mob entity, double squaredDistance, CallbackInfoReturnable<Boolean> info) {
        if (WorldEvents.preventMobSpawn(world, entity)) {
            info.setReturnValue(false);
            info.cancel();
        }
    }
}
