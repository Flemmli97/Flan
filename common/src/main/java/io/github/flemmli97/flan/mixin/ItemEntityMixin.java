package io.github.flemmli97.flan.mixin;

import io.github.flemmli97.flan.player.IOwnedItem;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundTag;
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

    @Inject(method = "readCustomDataFromTag", at = @At("RETURN"))
    private void readData(CompoundTag tag, CallbackInfo info) {
        if (tag.contains("io.github.flemmli97.flan.Flan:PlayerOrigin"))
            this.playerOrigin = tag.getUuid("io.github.flemmli97.flan.Flan:PlayerOrigin");
    }

    @Inject(method = "writeCustomDataToTag", at = @At("RETURN"))
    private void writeData(CompoundTag tag, CallbackInfo info) {
        if (this.playerOrigin != null)
            tag.putUuid("io.github.flemmli97.flan.Flan:PlayerOrigin", this.playerOrigin);
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
