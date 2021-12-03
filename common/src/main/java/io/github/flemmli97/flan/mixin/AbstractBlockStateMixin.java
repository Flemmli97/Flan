package io.github.flemmli97.flan.mixin;

import io.github.flemmli97.flan.event.BlockInteractEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BlockBehaviour.BlockStateBase.class)
public abstract class AbstractBlockStateMixin {

    @Inject(method = "entityInside", at = @At(value = "HEAD"), cancellable = true)
    private void collision(Level world, BlockPos pos, Entity entity, CallbackInfo info) {
        if (BlockInteractEvents.cancelEntityBlockCollision(this.asState(), world, pos, entity)) {
            info.cancel();
        }
    }

    /**
     * Can't use the hooks from both fabric or forge cause they are too generic.
     * Wouldn't be able to place blocks after cancelling them
     */
    @Inject(method = "use", at = @At(value = "HEAD"), cancellable = true)
    private void useBlock(Level world, Player player, InteractionHand hand, BlockHitResult result, CallbackInfoReturnable<InteractionResult> info) {
        InteractionResult res = BlockInteractEvents.useBlocks(player, world, hand, result);
        if (res != InteractionResult.PASS) {
            info.setReturnValue(res);
            info.cancel();
        }
    }

    @Shadow
    protected abstract BlockState asState();
}
