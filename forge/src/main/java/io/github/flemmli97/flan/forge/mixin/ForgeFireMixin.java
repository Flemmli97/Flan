package io.github.flemmli97.flan.forge.mixin;

import io.github.flemmli97.flan.event.WorldEvents;
import net.minecraft.block.FireBlock;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Random;

@Mixin(FireBlock.class)
public abstract class ForgeFireMixin {

    @Inject(method = "tryCatchFire", at = @At(value = "HEAD"), cancellable = true)
    private void spread(World world, BlockPos pos, int spreadFactor, Random rand, int currentAge, Direction dir, CallbackInfo info) {
        if (!world.isClient && !WorldEvents.canFireSpread((ServerWorld) world, pos)) {
            info.cancel();
        }
    }
}
