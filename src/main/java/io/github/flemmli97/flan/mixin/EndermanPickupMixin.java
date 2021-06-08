package io.github.flemmli97.flan.mixin;

import io.github.flemmli97.flan.event.EntityInteractEvents;
import net.minecraft.entity.mob.EndermanEntity;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(targets = "net/minecraft/entity/mob/EndermanEntity$PickUpBlockGoal")
public abstract class EndermanPickupMixin {

    @Shadow
    private EndermanEntity enderman;

    @ModifyVariable(method = "Lnet/minecraft/entity/mob/EndermanEntity$PickUpBlockGoal;tick()V", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;getBlockState(Lnet/minecraft/util/math/BlockPos;)Lnet/minecraft/block/BlockState;"), ordinal = 0)
    private BlockPos pickupCheck(BlockPos pos) {
        if (!EntityInteractEvents.canEndermanInteract(this.enderman, pos)) {
            return BlockPos.ORIGIN;
        }
        return pos;
    }
}
