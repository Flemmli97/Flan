package io.github.flemmli97.flan.mixin;

import io.github.flemmli97.flan.event.BlockInteractEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.TurtleEggBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TurtleEggBlock.class)
public abstract class TurtleEggMixin {

    @Inject(method = "destroyEgg", at = @At(value = "HEAD"), cancellable = true)
    private void collision(Level world, BlockState blockState, BlockPos pos, Entity entity, int chance, CallbackInfo info) {
        if (BlockInteractEvents.canBreakTurtleEgg(world, pos, entity)) {
            info.cancel();
        }
    }
}
