package io.github.flemmli97.flan.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import io.github.flemmli97.flan.Flan;
import io.github.flemmli97.flan.event.EntityInteractEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.enchantment.EnchantedItemInUse;
import net.minecraft.world.item.enchantment.effects.ReplaceDisk;
import net.minecraft.world.level.levelgen.blockpredicates.BlockPredicate;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.Optional;

@Mixin(ReplaceDisk.class)
public abstract class ReplaceDiskMixin {

    @ModifyExpressionValue(method = "apply", at = @At(value = "FIELD", target = "Lnet/minecraft/world/item/enchantment/effects/ReplaceDisk;predicate:Ljava/util/Optional;"))
    private Optional<BlockPredicate> onApply(Optional<BlockPredicate> predicate, ServerLevel serverLevel, int i, EnchantedItemInUse enchantedItemInUse, Entity entity, Vec3 vec3, @Local(ordinal = 1) BlockPos pos) {
        if (entity instanceof LivingEntity living && !EntityInteractEvents.canFrostwalkerFreeze(serverLevel, pos, living))
            return Optional.of(Flan.NONE_PREDICATE);
        return predicate;
    }
}
