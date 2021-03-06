package io.github.flemmli97.flan.mixin;

import io.github.flemmli97.flan.event.WorldEvents;
import net.minecraft.block.BlockState;
import net.minecraft.block.FireBlock;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Random;

@Mixin(FireBlock.class)
public abstract class FireBlockMixin {

    @Inject(method = "scheduledTick", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/world/ServerWorld;getGameRules()Lnet/minecraft/world/GameRules;"), cancellable = true)
    private void tick(BlockState state, ServerWorld world, BlockPos pos, Random random, CallbackInfo info) {
        if (!WorldEvents.canFireSpread(world, pos)) {
            info.cancel();
        }
    }

    @Inject(method = "getBurnChance(Lnet/minecraft/world/WorldView;Lnet/minecraft/util/math/BlockPos;)I", at = @At(value = "HEAD"), cancellable = true)
    private void burn(WorldView worldView, BlockPos pos, CallbackInfoReturnable<Integer> info) {
        if (worldView instanceof ServerWorld && !WorldEvents.canFireSpread((ServerWorld) worldView, pos)) {
            info.setReturnValue(0);
            info.cancel();
        }
    }
}
