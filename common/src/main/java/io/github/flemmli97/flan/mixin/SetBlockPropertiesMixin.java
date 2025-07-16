package io.github.flemmli97.flan.mixin;

import io.github.flemmli97.flan.event.EntityInteractEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.enchantment.EnchantedItemInUse;
import net.minecraft.world.item.enchantment.effects.SetBlockProperties;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SetBlockProperties.class)
public abstract class SetBlockPropertiesMixin {

    @Inject(method = "apply", at = @At(value = "HEAD"), cancellable = true)
    private void freeze(ServerLevel serverLevel, int i, EnchantedItemInUse enchantedItemInUse, Entity entity, Vec3 vec3, CallbackInfo info) {
        if (entity instanceof LivingEntity living && !EntityInteractEvents.canFrostwalkerFreeze(serverLevel, BlockPos.containing(vec3).offset(((SetBlockProperties) (Object) this).offset()), living))
            info.cancel();
    }
}
