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

    //TODO this injection conflicts with lithium as they change the calculation and there are cases, where this check is not called.
    // Can't figure out what are this cases in code, but they are present in tests (water can flow into claim sometimes)
    // Disabling lithuim fluid flow optimisation fixes the issue like a workaround.
    // (https://github.com/CaffeineMC/lithium/blob/develop/common/src/main/java/net/caffeinemc/mods/lithium/mixin/block/fluid/flow/FlowingFluidMixin.java#L203)
    @Inject(method = "canMaybePassThrough", at = @At(value = "HEAD"), cancellable = true)
    private void crossClaimFlow(BlockGetter world, BlockPos fluidPos, BlockState fluidBlockState, Direction flowDirection, BlockPos flowTo,
                                BlockState flowToBlockState, FluidState fluidState, CallbackInfoReturnable<Boolean> info) {
        if (!WorldEvents.canFlow(fluidBlockState, world, fluidPos, flowDirection)) {
            info.setReturnValue(false);
        }
    }
}
