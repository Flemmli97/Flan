package io.github.flemmli97.flan.mixin;

import io.github.flemmli97.flan.event.WorldEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.FluidState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(FlowingFluid.class)
public abstract class FluidMixin {

    @Inject(method = "canMaybePassThrough", at = @At(value = "HEAD"), cancellable = true)
    private void crossClaimFlow(BlockGetter level, BlockPos fluidPos, BlockState fluidBlockState, Direction flowDirection, BlockPos flowTo,
                                BlockState flowToBlockState, FluidState fluidState, CallbackInfoReturnable<Boolean> info) {
        if (!WorldEvents.canFlow(fluidBlockState, level, fluidPos, flowDirection)) {
            info.setReturnValue(false);
        }
    }
}
