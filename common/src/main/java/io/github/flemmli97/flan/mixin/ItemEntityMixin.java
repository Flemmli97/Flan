package io.github.flemmli97.flan.mixin;

import io.github.flemmli97.flan.event.EntityInteractEvents;
import io.github.flemmli97.flan.player.PlayerClaimData;
import io.github.flemmli97.flan.utils.IOwnedItem;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
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
    private void readData(CompoundTag tag, CallbackInfo info) {
        if (tag.contains("Flan:PlayerOrigin"))
            this.flan$PlayerOrigin = tag.getUUID("Flan:PlayerOrigin");
    }

    @Inject(method = "addAdditionalSaveData", at = @At("RETURN"))
    private void writeData(CompoundTag tag, CallbackInfo info) {
        if (this.flan$PlayerOrigin != null)
            tag.putUUID("Flan:PlayerOrigin", this.flan$PlayerOrigin);
    }

    @Override
    public void flan$setOriginPlayer(Player player) {
        this.flan$PlayerOrigin = player.getUUID();
        if (player instanceof ServerPlayer && PlayerClaimData.get((ServerPlayer) player).setDeathItemOwner())
            this.flan$DeathPlayerOrigin = this.flan$PlayerOrigin;
    }

    @Inject(method = "playerTouch", at = @At(value = "HEAD"), cancellable = true)
    private void pickup(Player player, CallbackInfo info) {
        if (!EntityInteractEvents.canCollideWith(player, (ItemEntity) (Object) this)) {
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
