package io.github.flemmli97.flan.mixin;

import io.github.flemmli97.flan.event.EntityInteractEvents;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Player.class)
public abstract class PlayerMixin {

    @ModifyVariable(method = "drop(Lnet/minecraft/world/item/ItemStack;ZZ)Lnet/minecraft/world/entity/item/ItemEntity;", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/item/ItemEntity;setPickUpDelay(I)V"))
    private ItemEntity ownerDrop(ItemEntity entity) {
        EntityInteractEvents.updateDroppedItem((Player) (Object) this, entity);
        return entity;
    }

    @Inject(method = "touch", at = @At(value = "HEAD"), cancellable = true)
    private void entityCollide(Entity entity, CallbackInfo info) {
        if (!EntityInteractEvents.canCollideWith((Player) (Object) this, entity)) {
            info.cancel();
        }
    }

}
