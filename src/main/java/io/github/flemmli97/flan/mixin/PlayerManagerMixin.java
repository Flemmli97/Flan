package io.github.flemmli97.flan.mixin;

import io.github.flemmli97.flan.event.PlayerEvents;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerManager.class)
public abstract class PlayerManagerMixin {

    @Inject(method = "loadPlayerData", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/network/ServerPlayerEntity;readNbt(Lnet/minecraft/nbt/NbtCompound;)V"))
    private void singlePlayerRead(ServerPlayerEntity player, CallbackInfoReturnable<NbtCompound> info) {
        PlayerEvents.readClaimData(player);
    }
}
