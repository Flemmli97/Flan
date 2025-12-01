package io.github.flemmli97.flan.fabric.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import io.github.flemmli97.flan.event.PlayerEvents;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.ValueInput;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerPlayer.class)
public abstract class ServerPlayerDropMixin {

    @Inject(method = "drop(Z)Z", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerPlayer;drop(Lnet/minecraft/world/item/ItemStack;ZZ)Lnet/minecraft/world/entity/item/ItemEntity;"), cancellable = true)
    private void onDrop(boolean dropStack, CallbackInfoReturnable<Boolean> info, @Local ItemStack stack) {
        if (!PlayerEvents.canDropItem((Player) (Object) this, stack)) {
            info.setReturnValue(null);
            info.cancel();
        }
    }

    /**
     * Moved from WorldSaveHandlerMixin. Reads claim data of player.
     * */
    @Inject(method = "readAdditionalSaveData", at = @At("RETURN"))
    private void onReadData(ValueInput input, CallbackInfo ci) {
        // Cast 'this' to getting the player instance
        ServerPlayer player = (ServerPlayer) (Object) this;

        // Trigger event
        PlayerEvents.readClaimData(player);
    }
}
