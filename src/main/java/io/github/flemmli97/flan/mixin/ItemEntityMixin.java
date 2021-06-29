package io.github.flemmli97.flan.mixin;

import io.github.flemmli97.flan.player.IOwnedItem;
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
public abstract class ItemEntityMixin implements IOwnedItem {

    @Unique
    private UUID playerOrigin;
    @Unique
    private UUID deathPlayerOrigin;

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

    @Override
    public void setOriginPlayer(PlayerEntity player) {
        this.playerOrigin = player.getUuid();
        if (player.isDead())
            this.deathPlayerOrigin = this.playerOrigin;
    }

    @Override
    public UUID getDeathPlayer() {
        return this.deathPlayerOrigin;
    }

    @Override
    public UUID getPlayerOrigin() {
        return this.playerOrigin;
    }
}
