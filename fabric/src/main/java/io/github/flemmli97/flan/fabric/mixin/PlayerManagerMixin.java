package io.github.flemmli97.flan.fabric.mixin;

import io.github.flemmli97.flan.event.PlayerEvents;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.level.storage.ValueInput;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;

@Mixin(PlayerList.class)
public abstract class PlayerManagerMixin {

    @Inject(method = "load", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/storage/TagValueInput;create(Lnet/minecraft/util/ProblemReporter;Lnet/minecraft/core/HolderLookup$Provider;Lnet/minecraft/nbt/CompoundTag;)Lnet/minecraft/world/level/storage/ValueInput;"))
    private void singlePlayerRead(ServerPlayer player, ProblemReporter problemReporter, CallbackInfoReturnable<Optional<ValueInput>> cir) {
        PlayerEvents.readClaimData(player);
    }
}
