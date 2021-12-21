package io.github.flemmli97.flan.mixin;

import io.github.flemmli97.flan.event.BlockInteractEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BlockBehaviour.BlockStateBase.class)
public abstract class AbstractBlockStateMixin {

    @Inject(method = "entityInside", at = @At(value = "HEAD"), cancellable = true)
    private void collision(Level world, BlockPos pos, Entity entity, CallbackInfo info) {
        if (BlockInteractEvents.cancelEntityBlockCollision(this.asState(), world, pos, entity)) {
            info.cancel();
        }
    }

    @Shadow
    protected abstract BlockState asState();
}
