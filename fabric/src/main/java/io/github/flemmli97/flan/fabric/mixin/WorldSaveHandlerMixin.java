package io.github.flemmli97.flan.fabric.mixin;

import io.github.flemmli97.flan.event.PlayerEvents;
import net.minecraft.network.Connection;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.server.players.PlayerList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerList.class)
public class WorldSaveHandlerMixin {

    @Inject(method = "placeNewPlayer", at = @At(value = "HEAD"))
    private void readClaimData(Connection connection, ServerPlayer player, CommonListenerCookie cookie, CallbackInfo ci) {
        PlayerEvents.readClaimData(player);
    }

    @Inject(method = "save", at = @At(value = "RETURN"))
    private void saveClaimData(ServerPlayer player, CallbackInfo ci) {
        PlayerEvents.saveClaimData(player);
    }
}
