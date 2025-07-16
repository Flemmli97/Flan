package io.github.flemmli97.flan.gui;

import io.github.flemmli97.flan.api.permission.BuiltinPermission;
import io.github.flemmli97.flan.claim.Claim;
import io.github.flemmli97.flan.claim.ClaimUtils;
import io.github.flemmli97.flan.claim.attachment.ClaimAllowListKey;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.HashMap;
import java.util.Map;

public class ClaimAllowListEntryScreenHandler extends ServerOnlyScreenHandler<Claim> {

    private static final Map<Integer, ClaimAllowListKey<?>> UI_VALUES = withEntries();

    private ClaimAllowListEntryScreenHandler(int syncId, Inventory playerInventory, Claim claim) {
        super(syncId, playerInventory, 1 + (UI_VALUES.size() / 5), claim);
    }

    private static Map<Integer, ClaimAllowListKey<?>> withEntries() {
        Map<Integer, ClaimAllowListKey<?>> values = new HashMap<>();
        int idx = 2;
        for (ClaimAllowListKey<?> val : ClaimAllowListKey.keys().values()) {
            values.put(idx, val);
            if (++idx % 9 > 6)
                idx += 4;
        }
        return values;
    }

    public static void openScreen(ServerPlayer player, Claim claim) {
        MenuProvider fac = new MenuProvider() {
            @Override
            public AbstractContainerMenu createMenu(int syncId, Inventory inv, Player player) {
                return new ClaimAllowListEntryScreenHandler(syncId, inv, claim);
            }

            @Override
            public Component getDisplayName() {
                return ClaimUtils.translatedText(claim.parentClaim() != null ? "flan.screenMenuSub" : "flan.screenMenu");
            }
        };
        player.openMenu(fac);
    }

    @Override
    protected void fillInventoryWith() {
        for (int i = 0; i < 18; i++) {
            ClaimAllowListKey<?> val = UI_VALUES.get(i);
            if (val != null) {
                ItemStack stack = ServerScreenHelper.createStack(val.guiIcon().get(),
                        ServerScreenHelper.coloredGuiText(val.translationKey(), ChatFormatting.GOLD));
                if (!this.hasPerm(this.data, this.player, BuiltinPermission.EDITCLAIM))
                    ServerScreenHelper.addLore(stack, ServerScreenHelper.coloredGuiText("flan.screenNoPerm", ChatFormatting.DARK_RED));
                this.slots.get(i).set(stack);
            } else if (i == 0) {
                ItemStack stack = ServerScreenHelper.createStack(Items.TNT, ServerScreenHelper.coloredGuiText("flan.screenBack", ChatFormatting.DARK_RED));
                this.slots.get(i).set(stack);
            } else {
                this.slots.get(i).set(ServerScreenHelper.emptyFiller());
            }
        }
    }

    @Override
    protected boolean isRightSlot(int slot) {
        return slot == 0 || UI_VALUES.containsKey(slot);
    }

    @Override
    protected boolean handleSlotClicked(ServerPlayer player, int index, Slot slot, int clickType) {
        ClaimAllowListKey<?> val = UI_VALUES.get(index);
        if (val != null) {
            if (this.hasPerm(this.data, player, BuiltinPermission.EDITPERMS)) {
                player.closeContainer();
                player.getServer().execute(() -> CustomInteractListScreenHandler.openMenu(player, val, this.data));
                ServerScreenHelper.playSongToPlayer(player, SoundEvents.UI_BUTTON_CLICK, 1, 1f);
            } else
                ServerScreenHelper.playSongToPlayer(player, SoundEvents.VILLAGER_NO, 1, 1f);
        } else if (index == 0) {
            player.closeContainer();
            player.getServer().execute(() -> ClaimMenuScreenHandler.openClaimMenu(player, this.data));
            ServerScreenHelper.playSongToPlayer(player, SoundEvents.UI_BUTTON_CLICK, 1, 1f);
        }
        return true;
    }

    private boolean hasPerm(Claim claim, ServerPlayer player, ResourceLocation perm) {
        if (claim.parentClaim() != null)
            return claim.parentClaim().canInteract(player, perm, player.blockPosition());
        return claim.canInteract(player, perm, player.blockPosition());
    }
}
