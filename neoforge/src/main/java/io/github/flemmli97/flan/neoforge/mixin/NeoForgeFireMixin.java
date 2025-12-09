package io.github.flemmli97.flan.neoforge.mixin;

import io.github.flemmli97.flan.event.WorldEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.FireBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(FireBlock.class)
public abstract class NeoForgeFireMixin {

    /**
     * Check for blocks reacting to fire (e.g. tnt)
     */
    @Inject(method = "checkBurnOut", at = @At(value = "HEAD"), cancellable = true, remap = false)
    private void spread(Level level, BlockPos pos, int spreadFactor, RandomSource rand, int currentAge, Direction dir, CallbackInfo info) {
        if (!level.isClientSide() && !WorldEvents.canFireSpread((ServerLevel) level, pos)) {
            info.cancel();
        }
    }
}
