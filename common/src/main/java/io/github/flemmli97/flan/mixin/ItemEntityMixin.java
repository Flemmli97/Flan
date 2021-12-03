package io.github.flemmli97.flan.mixin;

import io.github.flemmli97.flan.player.IOwnedItem;
import io.github.flemmli97.flan.player.PlayerClaimData;
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
    private UUID flanPlayerOrigin;
    @Unique
    private UUID flanDeathPlayerOrigin;

    @Inject(method = "readAdditionalSaveData", at = @At("RETURN"))
    private void readData(CompoundTag tag, CallbackInfo info) {
        if (tag.contains("Flan:PlayerOrigin"))
            this.flanPlayerOrigin = tag.getUUID("Flan:PlayerOrigin");
    }

    @Inject(method = "addAdditionalSaveData", at = @At("RETURN"))
    private void writeData(CompoundTag tag, CallbackInfo info) {
        if (this.flanPlayerOrigin != null)
            tag.putUUID("Flan:PlayerOrigin", this.flanPlayerOrigin);
    }

    @Override
    public void setOriginPlayer(Player player) {
        this.flanPlayerOrigin = player.getUUID();
        if (player instanceof ServerPlayer && PlayerClaimData.get((ServerPlayer) player).setDeathItemOwner())
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
