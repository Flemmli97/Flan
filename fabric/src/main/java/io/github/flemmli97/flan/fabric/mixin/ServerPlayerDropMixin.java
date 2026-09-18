package io.github.flemmli97.flan.fabric.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import io.github.flemmli97.flan.event.PlayerEvents;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Prediction;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerPlayer.class)
public abstract class ServerPlayerDropMixin {

    @Inject(
            method = "drop(Lnet/minecraft/world/item/ItemStack;ZLnet/minecraft/util/Prediction;)Lnet/minecraft/world/entity/item/ItemEntity;",
            cancellable = true,
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/player/Player;drop(Lnet/minecraft/world/item/ItemStack;ZLnet/minecraft/util/Prediction;)Lnet/minecraft/world/entity/item/ItemEntity;",
                    shift = At.Shift.AFTER
            )
    )
    private void onDrop(ItemStack itemStack, boolean thrownFromHand, Prediction prediction, CallbackInfoReturnable<ItemEntity> cir) {
        if (!PlayerEvents.canDropItem((ServerPlayer) (Object) this, itemStack)) {
            cir.setReturnValue(null);
        }
    }

    @Inject(method = "drop(Z)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerPlayer;drop(Lnet/minecraft/world/item/ItemStack;ZLnet/minecraft/util/Prediction;)Lnet/minecraft/world/entity/item/ItemEntity;"), cancellable = true)
    private void onDrop(boolean all, CallbackInfo info, @Local ItemStack removed) {
        if (!PlayerEvents.canDropItem((ServerPlayer) (Object) this, removed)) {
            info.cancel();
        }
    }
}