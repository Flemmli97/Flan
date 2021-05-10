package com.flemmli97.flan.mixin;

import com.flemmli97.flan.player.IOwnedItem;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.UUID;

@Mixin(ItemEntity.class)
public class ItemEntityMixin implements IOwnedItem {

    @Unique
    private UUID playerOrigin;

    @Inject(method = "readCustomDataFromNbt", at = @At("RETURN"))
    private void readData(NbtCompound tag, CallbackInfo info) {
        if (tag.contains("Flan:PlayerOrigin"))
            this.playerOrigin = tag.getUuid("Flan:PlayerOrigin");
    }

    @Inject(method = "writeCustomDataToNbt", at = @At("RETURN"))
    private void writeData(NbtCompound tag, CallbackInfo info) {
        if (this.playerOrigin != null)
            tag.putUuid("Flan:PlayerOrigin", this.playerOrigin);
    }

    public void setOriginPlayer(PlayerEntity player) {
        this.playerOrigin = player.getUuid();
    }

    public UUID getPlayerOrigin() {
        return this.playerOrigin;
    }
}
