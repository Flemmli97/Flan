package io.github.flemmli97.flan.fabric.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import io.github.flemmli97.flan.event.PlayerEvents;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayer.class)
public abstract class ServerPlayerDropMixin {

    @Inject(method = "drop(Z)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerPlayer;drop(Lnet/minecraft/world/item/ItemStack;ZZ)Lnet/minecraft/world/entity/item/ItemEntity;"), cancellable = true)
    private void onDrop(boolean dropStack, CallbackInfo info, @Local ItemStack stack) {
        if (!PlayerEvents.canDropItem((Player) (Object) this, stack)) {
            info.cancel();
        }
    }
}
