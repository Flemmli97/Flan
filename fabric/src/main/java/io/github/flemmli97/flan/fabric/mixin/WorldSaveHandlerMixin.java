package io.github.flemmli97.flan.fabric.mixin;

import io.github.flemmli97.flan.event.PlayerEvents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.storage.PlayerDataStorage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerDataStorage.class)
public abstract class WorldSaveHandlerMixin {

    @Inject(method = "save", at = @At(value = "RETURN"))
    private void save(Player player, CallbackInfo info) {
        PlayerEvents.saveClaimData(player);
    }

    @Inject(method = "load", at = @At(value = "RETURN"))
    private void load(Player player, CallbackInfoReturnable<CompoundTag> info) {
        PlayerEvents.readClaimData(player);
    }
}
