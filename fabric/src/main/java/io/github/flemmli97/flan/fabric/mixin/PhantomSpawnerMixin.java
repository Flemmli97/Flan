package io.github.flemmli97.flan.fabric.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import io.github.flemmli97.flan.event.WorldEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.levelgen.PhantomSpawner;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PhantomSpawner.class)
public class PhantomSpawnerMixin {

    @Inject(method = "tick",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/DifficultyInstance;getDifficulty()Lnet/minecraft/world/Difficulty;"),
            cancellable = true)
    private void phantomSpawnCheck(ServerLevel level, boolean spawnEnemies, boolean spawnFriendlies, CallbackInfo info, @Local(ordinal = 1) BlockPos pos) {
        if (WorldEvents.preventMobSpawn(level, pos, MobCategory.MONSTER))
            info.cancel();
    }
}
