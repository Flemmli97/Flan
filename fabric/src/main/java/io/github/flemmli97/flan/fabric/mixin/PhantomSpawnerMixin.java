package io.github.flemmli97.flan.fabric.mixin;

import io.github.flemmli97.flan.event.WorldEvents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.monster.Phantom;
import net.minecraft.world.level.levelgen.PhantomSpawner;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PhantomSpawner.class)
public class PhantomSpawnerMixin {

    @Unique
    private Phantom flan_phantom_var;

    @ModifyVariable(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/monster/Phantom;finalizeSpawn(Lnet/minecraft/world/level/ServerLevelAccessor;Lnet/minecraft/world/DifficultyInstance;Lnet/minecraft/world/entity/MobSpawnType;Lnet/minecraft/world/entity/SpawnGroupData;Lnet/minecraft/nbt/CompoundTag;)Lnet/minecraft/world/entity/SpawnGroupData;"))
    private Phantom phantomSpawnCheckPrepare(Phantom phantom) {
        this.flan_phantom_var = phantom;
        return phantom;
    }

    @Inject(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/monster/Phantom;finalizeSpawn(Lnet/minecraft/world/level/ServerLevelAccessor;Lnet/minecraft/world/DifficultyInstance;Lnet/minecraft/world/entity/MobSpawnType;Lnet/minecraft/world/entity/SpawnGroupData;Lnet/minecraft/nbt/CompoundTag;)Lnet/minecraft/world/entity/SpawnGroupData;"),
            cancellable = true)
    private void phantomSpawnCheck(ServerLevel level, boolean spawnEnemies, boolean spawnFriendlies, CallbackInfoReturnable<Integer> cir) {
        if (WorldEvents.preventMobSpawn(level, this.flan_phantom_var))
            cir.setReturnValue(0);
    }
}
