package com.flemmli97.flan.gui;

import com.flemmli97.flan.claim.Claim;
import com.flemmli97.flan.claim.ClaimStorage;
import com.flemmli97.flan.claim.PermHelper;
import com.flemmli97.flan.config.ConfigHandler;
import com.flemmli97.flan.player.PlayerClaimData;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class ClaimMenuScreenHandler extends ServerOnlyScreenHandler {

    private final Claim claim;

    private ClaimMenuScreenHandler(int syncId, PlayerInventory playerInventory, Claim claim) {
        super(syncId, playerInventory, 1);
        this.claim = claim;
    }

    public static void openClaimMenu(ServerPlayerEntity player, Claim claim) {
        NamedScreenHandlerFactory fac = new NamedScreenHandlerFactory() {
            @Override
            public ScreenHandler createMenu(int syncId, PlayerInventory inv, PlayerEntity player) {
                return new ClaimMenuScreenHandler(syncId, inv, claim);
            }

            @Override
            public Text getDisplayName() {
                return PermHelper.simpleColoredText(claim.parentClaim() != null ? "SubClaim-Menu" : "Claim-Menu");
            }
        };
        player.openHandledScreen(fac);
    }

    @Override
    protected void fillInventoryWith(PlayerEntity player, Inventory inv, Object... additionalData) {
        for (int i = 0; i < 9; i++) {
            switch (i) {
                case 0:
                    ItemStack close = new ItemStack(Items.TNT);
                    close.setCustomName(new LiteralText("Close").setStyle(Style.EMPTY.withFormatting(Formatting.DARK_RED)));
                    inv.setStack(i, close);
                    break;
                case 2:
                    ItemStack perm = new ItemStack(Items.BEACON);
                    perm.setCustomName(new LiteralText("Edit Global Permissions").setStyle(Style.EMPTY.withFormatting(Formatting.GOLD)));
                    inv.setStack(i, perm);
                    break;
                case 3:
                    ItemStack group = new ItemStack(Items.WRITABLE_BOOK);
                    group.setCustomName(new LiteralText("Edit Permissiongroups").setStyle(Style.EMPTY.withFormatting(Formatting.GOLD)));
                    inv.setStack(i, group);
                    break;
                case 8:
                    ItemStack delete = new ItemStack(Items.BARRIER);
                    delete.setCustomName(new LiteralText("Delete Claim").setStyle(Style.EMPTY.withFormatting(Formatting.RED)));
                    inv.setStack(i, delete);
                    break;
                default:
                    inv.setStack(i, ServerScreenHelper.emptyFiller());
            }
        }
    }

    @Override
    protected boolean isRightSlot(int slot) {
        return slot == 0 || slot == 2 || slot == 3 || slot == 8;
    }

    @Override
    protected boolean handleSlotClicked(ServerPlayerEntity player, int index, Slot slot, int clickType) {
        switch (index) {
            case 0:
                player.closeHandledScreen();
                ServerScreenHelper.playSongToPlayer(player, SoundEvents.UI_BUTTON_CLICK, 1, 1f);
                break;
            case 2:
                player.closeHandledScreen();
                player.getServer().execute(() -> PermissionScreenHandler.openClaimMenu(player, this.claim, null));
                ServerScreenHelper.playSongToPlayer(player, SoundEvents.UI_BUTTON_CLICK, 1, 1f);
                break;
            case 3:
                player.closeHandledScreen();
                player.getServer().execute(() -> GroupScreenHandler.openGroupMenu(player, this.claim));
                ServerScreenHelper.playSongToPlayer(player, SoundEvents.UI_BUTTON_CLICK, 1, 1f);
                break;
            case 8:
                player.closeHandledScreen();
                player.getServer().execute(() -> ConfirmScreenHandler.openConfirmScreen(player, (bool) -> {
                    if (bool) {
                        ClaimStorage storage = ClaimStorage.get(player.getServerWorld());
                        storage.deleteClaim(this.claim, true, PlayerClaimData.get(player).getEditMode(), player.getServerWorld());
                        player.closeHandledScreen();
                        player.sendMessage(PermHelper.simpleColoredText(ConfigHandler.lang.deleteClaim, Formatting.RED), false);
                        ServerScreenHelper.playSongToPlayer(player, SoundEvents.BLOCK_ANVIL_PLACE, 1, 1f);
                    } else {
                        player.closeHandledScreen();
                        player.getServer().execute(() -> ClaimMenuScreenHandler.openClaimMenu(player, this.claim));
                        ServerScreenHelper.playSongToPlayer(player, SoundEvents.ENTITY_VILLAGER_NO, 1, 1f);
                    }
                }));
                break;
        }
        return true;
    }
}
