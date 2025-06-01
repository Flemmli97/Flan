package io.github.flemmli97.flan.mixin;

import io.github.flemmli97.flan.event.EntityInteractEvents;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(LivingEntity.class)
public abstract class PlayerMixin {

    @ModifyVariable(method = "createItemStackToDrop", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/item/ItemEntity;setPickUpDelay(I)V"))
    private ItemEntity ownerDrop(ItemEntity entity) {
        if ((Object) this instanceof Player player)
            EntityInteractEvents.updateDroppedItem(player, entity);
        return entity;
    }
}
