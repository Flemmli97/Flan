package io.github.flemmli97.flan.mixin;

import io.github.flemmli97.flan.api.permission.PermissionRegistry;
import io.github.flemmli97.flan.claim.Claim;
import io.github.flemmli97.flan.claim.ClaimStorage;
import io.github.flemmli97.flan.utils.IOwnedItem;
import net.minecraft.world.entity.animal.allay.Allay;
import net.minecraft.world.entity.animal.allay.AllayAi;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Allay.class)
public abstract class AllayMixin {

    @Inject(method = "wantsToPickUp", at = @At("HEAD"), cancellable = true)
    private void onWantingPickup(ItemStack stack, CallbackInfoReturnable<Boolean> info) {
        Allay allay = (Allay) (Object) this;
        if (AllayAi.getLikedPlayer(allay).map(p -> {
            Claim claim = ClaimStorage.get(p.serverLevel()).getClaimAt(allay.blockPosition());
            return claim != null && (!claim.canInteract(p, PermissionRegistry.PICKUP, allay.blockPosition(), false));
        }).orElse(false))
            info.setReturnValue(false);
    }

    @Inject(method = "pickUpItem", at = @At("HEAD"), cancellable = true)
    private void onPickupItem(ItemEntity itemEntity, CallbackInfo info) {
        if (AllayAi.getLikedPlayer((Allay) (Object) this).map(p -> {
            IOwnedItem ownedItem = (IOwnedItem) itemEntity;
            if (p.getUUID().equals(ownedItem.getPlayerOrigin()))
                return true;
            Claim claim = ClaimStorage.get(p.serverLevel()).getClaimAt(itemEntity.blockPosition());
            return claim != null && !claim.canInteract(p, PermissionRegistry.PICKUP, itemEntity.blockPosition(), false);
        }).orElse(false))
            info.cancel();
    }
}
