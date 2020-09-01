package com.flemmli97.flan.gui;

import com.flemmli97.flan.claim.Claim;
import com.flemmli97.flan.claim.EnumPermission;
import com.flemmli97.flan.claim.PermHelper;
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

public class PermissionScreenHandler extends ServerOnlyScreenHandler {

    private final Claim claim;
    private final String group;
    private int page;

    private PermissionScreenHandler(int syncId, PlayerInventory playerInventory, Claim claim, String group, int page) {
        super(syncId, playerInventory, 6, claim, group, page);
        this.claim = claim;
        this.group = group;
        this.page = page;
    }

    public static void openClaimMenu(PlayerEntity player, Claim claim, String group) {
        NamedScreenHandlerFactory fac = new NamedScreenHandlerFactory() {
            @Override
            public ScreenHandler createMenu(int syncId, PlayerInventory inv, PlayerEntity player) {
                return new PermissionScreenHandler(syncId, inv, claim, group, 0);
            }

            @Override
            public Text getDisplayName() {
                return PermHelper.simpleColoredText(group == null ? "Global-Permissions" : String.format("%s-Permissions", group));
            }
        };
        player.openHandledScreen(fac);
    }

    private static void openClaimMenu(PlayerEntity player, Claim claim, String group, int page) {
        NamedScreenHandlerFactory fac = new NamedScreenHandlerFactory() {
            @Override
            public ScreenHandler createMenu(int syncId, PlayerInventory inv, PlayerEntity player) {
                return new PermissionScreenHandler(syncId, inv, claim, group, page);
            }

            @Override
            public Text getDisplayName() {
                return PermHelper.simpleColoredText(group == null ? "Global-Permissions" : String.format("%s-Permissions", group));
            }
        };
        player.openHandledScreen(fac);
    }

    @Override
    protected void fillInventoryWith(PlayerEntity player, Inventory inv, Object... additionalData) {
        for (int i = 0; i < 54; i++) {
            int page = (int) additionalData[2];
            if (i == 0) {
                ItemStack close = new ItemStack(Items.TNT);
                close.setCustomName(new LiteralText("Back").setStyle(Style.EMPTY.withFormatting(Formatting.DARK_RED)));
                inv.setStack(i, close);
            } else if (page == 1 && i == 47) {
                ItemStack close = new ItemStack(Items.ARROW);
                close.setCustomName(new LiteralText("Prev").setStyle(Style.EMPTY.withFormatting(Formatting.WHITE)));
                inv.setStack(i, close);
            } else if (page == 0 && i == 51) {
                ItemStack close = new ItemStack(Items.ARROW);
                close.setCustomName(new LiteralText("Next").setStyle(Style.EMPTY.withFormatting(Formatting.WHITE)));
                inv.setStack(i, close);
            } else if (i < 9 || i > 44 || i % 9 == 0 || i % 9 == 8)
                inv.setStack(i, ServerScreenHelper.emptyFiller());
            else {
                int row = i / 9 - 1;
                int id = (i % 9) + row * 7 - 1 + page * 28;
                int length = EnumPermission.values().length;
                if(group!=null)
                    length-=EnumPermission.alwaysGlobalLength();
                if (id < length)
                    inv.setStack(i, ServerScreenHelper.fromPermission((Claim) additionalData[0], EnumPermission.values()[id], additionalData[1] == null ? null : additionalData[1].toString()));
            }
        }
    }

    private void flipPage() {
        for (int i = 0; i < 54; i++) {
            if (i == 0) {
                ItemStack close = new ItemStack(Items.TNT);
                close.setCustomName(new LiteralText("Back").setStyle(Style.EMPTY.withFormatting(Formatting.DARK_RED)));
                this.slots.get(i).setStack(close);
            } else if (i == 47) {
                ItemStack stack = ItemStack.EMPTY;
                if (this.page == 1) {
                    stack = new ItemStack(Items.ARROW);
                    stack.setCustomName(new LiteralText("Prev").setStyle(Style.EMPTY.withFormatting(Formatting.WHITE)));
                }
                this.slots.get(i).setStack(stack);
            } else if (i == 51) {
                ItemStack stack = ItemStack.EMPTY;
                if (this.page == 0) {
                    stack = new ItemStack(Items.ARROW);
                    stack.setCustomName(new LiteralText("Next").setStyle(Style.EMPTY.withFormatting(Formatting.WHITE)));
                }
                this.slots.get(i).setStack(stack);
            } else if (i < 9 || i > 44 || i % 9 == 0 || i % 9 == 8)
                this.slots.get(i).setStack(ServerScreenHelper.emptyFiller());
            else {
                int row = i / 9 - 1;
                int id = (i % 9) + row * 7 - 1 + this.page * 28;
                int length = EnumPermission.values().length;
                if(group!=null)
                    length-=EnumPermission.alwaysGlobalLength();
                if (id < length) {
                    this.slots.get(i).setStack(ServerScreenHelper.fromPermission(this.claim, EnumPermission.values()[id], this.group));
                }
                else
                    this.slots.get(i).setStack(ItemStack.EMPTY);
            }
        }
        this.sendContentUpdates();
    }

    @Override
    protected boolean handleSlotClicked(ServerPlayerEntity player, int index, Slot slot, int clickType) {
        if (index == 0) {
            if (this.group == null) {
                player.closeHandledScreen();
                player.getServer().execute(() -> ClaimMenuScreenHandler.openClaimMenu(player, this.claim));
            } else {
                player.closeHandledScreen();
                player.getServer().execute(() -> GroupScreenHandler.openGroupMenu(player, this.claim));
            }
            ServerScreenHelper.playSongToPlayer(player, SoundEvents.UI_BUTTON_CLICK, 1, 1f);
            return true;
        }
        if (index == 47) {
            this.page = 0;
            this.flipPage();
            ServerScreenHelper.playSongToPlayer(player, SoundEvents.UI_BUTTON_CLICK, 1, 1f);
        }
        if (index == 51) {
            this.page = 1;
            this.flipPage();
            ServerScreenHelper.playSongToPlayer(player, SoundEvents.UI_BUTTON_CLICK, 1, 1f);
        }
        ItemStack stack = slot.getStack();
        String name = stack.getName().asString();
        EnumPermission perm;
        try {
            perm = EnumPermission.valueOf(name);
        } catch (IllegalArgumentException e) {
            return false;
        }
        if (this.group == null) {
            int mode;
            if (this.claim.parentClaim() == null)
                mode = this.claim.permEnabled(perm) == 1 ? -1 : 1;
            else
                mode = this.claim.permEnabled(perm) + 1;
            this.claim.editGlobalPerms(perm, mode);
        } else
            this.claim.editPerms(player, this.group, perm, this.claim.groupHasPerm(this.group, perm) + 1);
        slot.setStack(ServerScreenHelper.fromPermission(this.claim, perm, this.group));
        ServerScreenHelper.playSongToPlayer(player, SoundEvents.BLOCK_NOTE_BLOCK_PLING, 1, 1.2f);
        return true;
    }

    @Override
    protected boolean isRightSlot(int slot) {
        return slot == 0 || (this.page == 1 && slot == 47) || (this.page == 0 && slot == 51) || (slot < 45 && slot > 8 && slot % 9 != 0 && slot % 9 != 8);
    }
}
