package io.github.flemmli97.flan.mixin;

import io.github.flemmli97.flan.event.BlockInteractEvents;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractBlock.AbstractBlockState.class)
public abstract class AbstractBlockStateMixin {

    @Inject(method = "onEntityCollision", at = @At(value = "HEAD"), cancellable = true)
    private void collision(World world, BlockPos pos, Entity entity, CallbackInfo info) {
        if (BlockInteractEvents.cancelEntityBlockCollision(this.asBlockState(), world, pos, entity)) {
            info.cancel();
        }
    }

    /**
     * Can't use the hooks from both fabric or forge cause they are too generic.
     * Wouldn't be able to place blocks after cancelling them
     */
    @Inject(method = "onUse", at = @At(value = "HEAD"), cancellable = true)
    private void useBlock(World world, PlayerEntity player, Hand hand, BlockHitResult result, CallbackInfoReturnable<ActionResult> info) {
        ActionResult res = BlockInteractEvents.useBlocks(player, world, hand, result);
        if (res != ActionResult.PASS) {
            info.setReturnValue(res);
            info.cancel();
        }
    }

    @Shadow
    protected abstract BlockState asBlockState();
}
