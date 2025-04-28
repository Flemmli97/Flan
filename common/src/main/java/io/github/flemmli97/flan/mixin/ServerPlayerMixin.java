package io.github.flemmli97.flan.mixin;

import io.github.flemmli97.flan.event.EntityInteractEvents;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerPlayer.class)
public abstract class ServerPlayerMixin {

    @Inject(method = "Lnet/minecraft/server/level/ServerPlayer;createItemStackToDrop(Lnet/minecraft/world/item/ItemStack;ZZ)Lnet/minecraft/world/entity/item/ItemEntity;",
            at = @At("RETURN"))
    private void ownerDrop(CallbackInfoReturnable<ItemEntity> info) {
        var itemEntity = info.getReturnValue();
        if (itemEntity != null) {
            EntityInteractEvents.updateDroppedItem((Player) (Object) this, itemEntity);
        }
    }
}
