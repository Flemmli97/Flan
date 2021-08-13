package io.github.flemmli97.flan.mixin;

import io.github.flemmli97.flan.event.PlayerEvents;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.world.WorldSaveHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(WorldSaveHandler.class)
public abstract class WorldSaveHandlerMixin {

    @Inject(method = "savePlayerData", at = @At(value = "RETURN"))
    private void save(PlayerEntity player, CallbackInfo info) {
        PlayerEvents.saveClaimData(player);
    }

    @Inject(method = "loadPlayerData", at = @At(value = "RETURN"))
    private void load(PlayerEntity player, CallbackInfoReturnable<NbtCompound> info) {
        PlayerEvents.readClaimData(player);
    }
}
