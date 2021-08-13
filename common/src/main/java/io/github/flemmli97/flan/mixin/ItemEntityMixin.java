package io.github.flemmli97.flan.mixin;

import io.github.flemmli97.flan.player.IOwnedItem;
import io.github.flemmli97.flan.player.PlayerClaimData;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.UUID;

@Mixin(ItemEntity.class)
public abstract class ItemEntityMixin implements IOwnedItem {

    @Unique
    private UUID flanPlayerOrigin;
    @Unique
    private UUID flanDeathPlayerOrigin;

    @Inject(method = "readCustomDataFromTag", at = @At("RETURN"))
    private void readData(CompoundTag tag, CallbackInfo info) {
        if (tag.contains("Flan:PlayerOrigin"))
            this.flanPlayerOrigin = tag.getUuid("Flan:PlayerOrigin");
    }

    @Inject(method = "writeCustomDataToTag", at = @At("RETURN"))
    private void writeData(CompoundTag tag, CallbackInfo info) {
        if (this.flanPlayerOrigin != null)
            tag.putUuid("Flan:PlayerOrigin", this.flanPlayerOrigin);
    }

    @Override
    public void setOriginPlayer(PlayerEntity player) {
        this.flanPlayerOrigin = player.getUuid();
        if (player instanceof ServerPlayerEntity && PlayerClaimData.get((ServerPlayerEntity) player).setDeathItemOwner())
            this.flanDeathPlayerOrigin = this.flanPlayerOrigin;
    }

    @Override
    public UUID getDeathPlayer() {
        return this.flanDeathPlayerOrigin;
    }

    @Override
    public UUID getPlayerOrigin() {
        return this.flanPlayerOrigin;
    }
}
