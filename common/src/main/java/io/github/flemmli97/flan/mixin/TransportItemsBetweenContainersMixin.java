package io.github.flemmli97.flan.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import io.github.flemmli97.flan.event.EntityInteractEvents;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.behavior.TransportItemsBetweenContainers;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(TransportItemsBetweenContainers.class)
public class TransportItemsBetweenContainersMixin {

    @Shadow
    private TransportItemsBetweenContainers.TransportItemTarget target;

    @WrapOperation(method = "isTargetValidToPick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/ai/behavior/TransportItemsBetweenContainers;isWantedBlock(Lnet/minecraft/world/entity/PathfinderMob;Lnet/minecraft/world/level/block/state/BlockState;)Z"))
    private boolean isTargetValidToPick(TransportItemsBetweenContainers instance, PathfinderMob mob, BlockState block, Operation<Boolean> original, @Local TransportItemsBetweenContainers.TransportItemTarget target) {
        if (target != null && EntityInteractEvents.preventMobInteractionAt(mob, target.pos()))
            return false;
        return original.call(instance, mob, block);
    }

    @Inject(method = "hasValidTarget", at = @At("HEAD"), cancellable = true)
    private void isWantedBlock(Level level, PathfinderMob body, CallbackInfoReturnable<Boolean> info) {
        if (this.target != null && EntityInteractEvents.preventMobInteractionAt(body, this.target.pos()))
            info.setReturnValue(false);
    }
}
