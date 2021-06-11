package io.github.flemmli97.flan.mixin;

import io.github.flemmli97.flan.event.EntityInteractEvents;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerEntity.class)
public abstract class PlayerMixin {

    @ModifyVariable(method = "dropItem(Lnet/minecraft/item/ItemStack;ZZ)Lnet/minecraft/entity/ItemEntity;", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/ItemEntity;setPickupDelay(I)V"))
    private ItemEntity ownerDrop(ItemEntity entity) {
        EntityInteractEvents.updateDroppedItem((PlayerEntity) (Object) this, entity);
        return entity;
    }

    @Inject(method = "collideWithEntity", at = @At(value = "HEAD"), cancellable = true)
    public void entityCollide(Entity entity, CallbackInfo info) {
        if (!EntityInteractEvents.canCollideWith((PlayerEntity) (Object) this, entity)) {
            info.cancel();
        }
    }

}
