package io.github.flemmli97.flan.fabric.mixin;

import io.github.flemmli97.flan.event.PlayerEvents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerList.class)
public abstract class PlayerManagerMixin {

    @Inject(method = "load", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerPlayer;load(Lnet/minecraft/nbt/CompoundTag;)V"))
    private void singlePlayerRead(ServerPlayer player, CallbackInfoReturnable<CompoundTag> info) {
        PlayerEvents.readClaimData(player);
    }
}
