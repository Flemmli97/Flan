package io.github.flemmli97.flan.fabric.mixin;

import io.github.flemmli97.flan.event.WorldEvents;
import net.minecraft.block.FireBlock;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Random;

@Mixin(FireBlock.class)
public abstract class FabricFireMixin {

    @Inject(method = "trySpreadingFire", at = @At(value = "HEAD"), cancellable = true)
    private void spread(World world, BlockPos pos, int spreadFactor, Random rand, int currentAge, CallbackInfo info) {
        if (!world.isClient && !WorldEvents.canFireSpread((ServerWorld) world, pos)) {
            info.cancel();
        }
    }
}
