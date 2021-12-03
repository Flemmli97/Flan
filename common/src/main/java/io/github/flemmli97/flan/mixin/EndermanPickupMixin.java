package io.github.flemmli97.flan.mixin;

import io.github.flemmli97.flan.event.EntityInteractEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.monster.EnderMan;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(targets = "net/minecraft/world/entity/monster/EnderMan$EndermanTakeBlockGoal")
public abstract class EndermanPickupMixin {

    @Shadow
    private EnderMan enderman;

    @ModifyVariable(method = "Lnet/minecraft/world/entity/monster/EnderMan$EndermanTakeBlockGoal;tick()V", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;getBlockState(Lnet/minecraft/core/BlockPos;)Lnet/minecraft/world/level/block/state/BlockState;"), ordinal = 0)
    private BlockPos pickupCheck(BlockPos pos) {
        if (!EntityInteractEvents.canEndermanInteract(this.enderman, pos)) {
            return BlockPos.ZERO;
        }
        return pos;
    }
}
