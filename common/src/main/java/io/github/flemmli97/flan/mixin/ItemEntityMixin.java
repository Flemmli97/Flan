package io.github.flemmli97.flan.mixin;

import io.github.flemmli97.flan.event.PlayerEvents;
import io.github.flemmli97.flan.player.PlayerClaimData;
import io.github.flemmli97.flan.utils.IOwnedItem;
import net.minecraft.core.UUIDUtil;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.UUID;

@Mixin(ItemEntity.class)
public abstract class ItemEntityMixin implements IOwnedItem {

    @Unique
    private UUID flan$PlayerOrigin;
    @Unique
    private UUID flan$DeathPlayerOrigin;

    @Inject(method = "readAdditionalSaveData", at = @At("RETURN"))
    private void readData(ValueInput input, CallbackInfo ci) {
        this.flan$PlayerOrigin = input.read("Flan:PlayerOrigin", UUIDUtil.CODEC).orElse(null);
    }

    @Inject(method = "addAdditionalSaveData", at = @At("RETURN"))
    private void writeData(ValueOutput output, CallbackInfo ci) {
        output.storeNullable("Flan:PlayerOrigin", UUIDUtil.CODEC, this.flan$PlayerOrigin);
    }

    @Override
    public void flan$setOriginPlayer(Player player) {
        this.flan$PlayerOrigin = player.getUUID();
        if (player instanceof ServerPlayer && PlayerClaimData.get((ServerPlayer) player).setDeathItemOwner())
            this.flan$DeathPlayerOrigin = this.flan$PlayerOrigin;
    }

    @Inject(method = "playerTouch", at = @At(value = "HEAD"), cancellable = true)
    private void pickup(Player player, CallbackInfo info) {
        if (!PlayerEvents.canCollideWith(player, (ItemEntity) (Object) this)) {
            info.cancel();
        }
    }

    @Override
    public UUID flan$getDeathPlayer() {
        return this.flan$DeathPlayerOrigin;
    }

    @Override
    public UUID flan$getPlayerOrigin() {
        return this.flan$PlayerOrigin;
    }
}
