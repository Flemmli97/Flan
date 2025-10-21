package io.github.flemmli97.flan.fabric.mixin;

import io.github.flemmli97.flan.event.PlayerEvents;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = Player.class)
public abstract class PlayerDropMixin {

    @Unique private static final Logger LOGGER = LoggerFactory.getLogger("PlayerDropMixin");

    @Inject(
        method = "drop(Lnet/minecraft/world/item/ItemStack;Z)Lnet/minecraft/world/entity/item/ItemEntity;",
        at = @At("HEAD"),
        cancellable = true
    )
    private void flan$drop(ItemStack stack, boolean includeThrowerName,
                           CallbackInfoReturnable<ItemEntity> cir) {
        cancelIfNotAllowed(stack, cir);
    }

    @Inject(
        method = "drop(Lnet/minecraft/world/item/ItemStack;ZZ)Lnet/minecraft/world/entity/item/ItemEntity;",
        at = @At("HEAD"),
        cancellable = true
    )
    private void flan$drop(ItemStack stack, boolean dropAround, boolean includeThrowerName,
                           CallbackInfoReturnable<ItemEntity> cir) {
        cancelIfNotAllowed(stack, cir);
    }

    @Unique private void cancelIfNotAllowed(ItemStack stack, CallbackInfoReturnable<ItemEntity> cir) {
        Player player = (Player) (Object) this;

        if (!PlayerEvents.canDropItem(player, stack)) {
            LOGGER.info("[FLAN] Drop blocked for player {}. Item: {} x{}",
                player.getName().getString(),
                stack.getItem().toString(),
                stack.getCount());

            // Cancel the drop entirely; no need to restore item manually
            var inventory = player.getInventory();
            if (inventory.add(stack)) {
                cir.setReturnValue(null);
                cir.cancel();
            }
        }
    }
}
