package io.github.flemmli97.flan.mixin;

import io.github.flemmli97.flan.event.EntityInteractEvents;
import net.minecraft.block.BlockState;
import net.minecraft.enchantment.FrostWalkerEnchantment;
import net.minecraft.entity.LivingEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(FrostWalkerEnchantment.class)
public abstract class FrostWalkerMixin {

    @Redirect(method = "freezeWater", at = @At(value = "INVOKE", target = "Lnet/minecraft/block/BlockState;canPlaceAt(Lnet/minecraft/world/WorldView;Lnet/minecraft/util/math/BlockPos;)Z"))
    private static boolean freeze(BlockState state, WorldView world, BlockPos pos, LivingEntity entity) {
        if (world instanceof ServerWorld && !EntityInteractEvents.canFrostwalkerFreeze((ServerWorld) world, pos, entity))
            return false;
        return state.canPlaceAt(world, pos);
    }
}
