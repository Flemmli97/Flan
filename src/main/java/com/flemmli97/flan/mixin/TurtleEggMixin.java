package com.flemmli97.flan.mixin;

import com.flemmli97.flan.event.BlockInteractEvents;
import net.minecraft.block.TurtleEggBlock;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TurtleEggBlock.class)
public abstract class TurtleEggMixin {

    @Inject(method = "onSteppedOn", at = @At(value = "HEAD"), cancellable = true)
    public void collision(World world, BlockPos pos, Entity entity, CallbackInfo info) {
        if (BlockInteractEvents.canBreakTurtleEgg(world, pos, entity)) {
            info.cancel();
        }
    }
}
