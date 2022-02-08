package io.github.flemmli97.flan.mixin;

import io.github.flemmli97.flan.event.WorldEvents;
import net.minecraft.block.BlockState;
import net.minecraft.block.PistonBlock;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PistonBlock.class)
public abstract class PistonMixin {

    /**
     * Forge does have an event for piston interaction but the location of the event makes it not really applicable
     * for this usecase. Would need to calculate all the affected blocks (specially in case of slime contraptions)
     */
    @Inject(method = "isMovable", at = @At(value = "HEAD"), cancellable = true)
    private static void checkMovable(BlockState blockState, World world, BlockPos blockPos, Direction direction, boolean canBreak, Direction pistonDir, CallbackInfoReturnable<Boolean> info) {
        if (!WorldEvents.pistonCanPush(blockState, world, blockPos, direction, pistonDir)) {
            info.setReturnValue(false);
            info.cancel();
        }
    }
}
