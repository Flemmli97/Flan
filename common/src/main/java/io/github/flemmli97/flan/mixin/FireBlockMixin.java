package io.github.flemmli97.flan.mixin;

import io.github.flemmli97.flan.event.WorldEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.FireBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Random;

@Mixin(FireBlock.class)
public abstract class FireBlockMixin {

    /**
     * Stop ticking overall if fire is in claim
     */
    @Inject(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerLevel;getGameRules()Lnet/minecraft/world/level/GameRules;"), cancellable = true)
    private void tick(BlockState state, ServerLevel world, BlockPos pos, Random random, CallbackInfo info) {
        if (!WorldEvents.canFireSpread(world, pos)) {
            info.cancel();
        }
    }

    /**
     * Check if fire can spread to this block
     */
    @Inject(method = "getFireOdds", at = @At(value = "HEAD"), cancellable = true)
    private void burn(LevelReader worldView, BlockPos pos, CallbackInfoReturnable<Integer> info) {
        if (worldView instanceof ServerLevel && !WorldEvents.canFireSpread((ServerLevel) worldView, pos)) {
            info.setReturnValue(0);
            info.cancel();
        }
    }
}
