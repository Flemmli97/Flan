package io.github.flemmli97.flan.gui;

import io.github.flemmli97.flan.api.ClaimPermission;
import io.github.flemmli97.flan.api.PermissionRegistry;
import io.github.flemmli97.flan.claim.Claim;
import io.github.flemmli97.flan.claim.PermHelper;
import io.github.flemmli97.flan.config.ConfigHandler;
import io.github.flemmli97.flan.gui.inv.SeparateInv;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.ArrayList;
import java.util.List;

public class PermissionScreenHandler extends ServerOnlyScreenHandler<ClaimGroup> {

    private final Claim claim;
    private final String group;
    private int page, maxPages;
    private List<ClaimPermission> perms;

    private PermissionScreenHandler(int syncId, PlayerInventory playerInventory, Claim claim, String group) {
        super(syncId, playerInventory, 6, new ClaimGroup() {
            @Override
            public Claim getClaim() {
                return claim;
            }

            @Override
            public String getGroup() {
                return group;
            }
        });
        this.claim = claim;
        this.group = group;
    }

    public static void openClaimMenu(PlayerEntity player, Claim claim, String group) {
        NamedScreenHandlerFactory fac = new NamedScreenHandlerFactory() {
            @Override
            public ScreenHandler createMenu(int syncId, PlayerInventory inv, PlayerEntity player) {
                return new PermissionScreenHandler(syncId, inv, claim, group);
            }

            @Override
            public Text getDisplayName() {
                return PermHelper.simpleColoredText(group == null ? "Global-Permissions" : String.format("%s-Permissions", group));
            }
        };
        player.openHandledScreen(fac);
    }

    @Override
    protected void fillInventoryWith(PlayerEntity player, SeparateInv inv, ClaimGroup additionalData) {
        this.perms = new ArrayList<>(PermissionRegistry.getPerms());
        if (additionalData.getGroup() != null)
            this.perms.removeAll(PermissionRegistry.globalPerms());
        this.maxPages = (this.perms.size() - 1) / 28;
        for (int i = 0; i < 54; i++) {
            if (i == 0) {
                ItemStack close = new ItemStack(Items.TNT);
                close.setCustomName(ServerScreenHelper.coloredGuiText(ConfigHandler.lang.screenBack, Formatting.DARK_RED));
                inv.updateStack(i, close);
            } else if (i == 51) {
                ItemStack close = new ItemStack(Items.ARROW);
                close.setCustomName(ServerScreenHelper.coloredGuiText(ConfigHandler.lang.screenNext, Formatting.WHITE));
                inv.updateStack(i, close);
            } else if (i < 9 || i > 44 || i % 9 == 0 || i % 9 == 8)
                inv.updateStack(i, ServerScreenHelper.emptyFiller());
            else {
                int row = i / 9 - 1;
                int id = (i % 9) + row * 7 - 1;
                if (id < this.perms.size())
                    inv.updateStack(i, ServerScreenHelper.fromPermission(additionalData.getClaim(), this.perms.get(id), additionalData.getGroup() == null ? null : additionalData.getGroup()));
            }
        }
    }

    private void flipPage() {
        for (int i = 0; i < 54; i++) {
            if (i == 0) {
                ItemStack close = new ItemStack(Items.TNT);
                close.setCustomName(ServerScreenHelper.coloredGuiText(ConfigHandler.lang.screenBack, Formatting.DARK_RED));
                this.slots.get(i).setStack(close);
            } else if (i == 47) {
                ItemStack stack = ServerScreenHelper.emptyFiller();
                if (this.page >= 1) {
                    stack = new ItemStack(Items.ARROW);
                    stack.setCustomName(ServerScreenHelper.coloredGuiText(ConfigHandler.lang.screenPrevious, Formatting.WHITE));
                }
                this.slots.get(i).setStack(stack);
            } else if (i == 51) {
                ItemStack stack = ServerScreenHelper.emptyFiller();
                if (this.page < this.maxPages) {
                    stack = new ItemStack(Items.ARROW);
                    stack.setCustomName(ServerScreenHelper.coloredGuiText(ConfigHandler.lang.screenNext, Formatting.WHITE));
                }
                this.slots.get(i).setStack(stack);
            } else if (i < 9 || i > 44 || i % 9 == 0 || i % 9 == 8)
                this.slots.get(i).setStack(ServerScreenHelper.emptyFiller());
            else {
                int row = i / 9 - 1;
                int id = (i % 9) + row * 7 - 1 + this.page * 28;
                if (id < this.perms.size()) {
                    this.slots.get(i).setStack(ServerScreenHelper.fromPermission(this.claim, this.perms.get(id), this.group));
                } else
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
            this.page--;
            this.flipPage();
            ServerScreenHelper.playSongToPlayer(player, SoundEvents.UI_BUTTON_CLICK, 1, 1f);
        }
        if (index == 51) {
            this.page++;
            this.flipPage();
            ServerScreenHelper.playSongToPlayer(player, SoundEvents.UI_BUTTON_CLICK, 1, 1f);
        }
        ItemStack stack = slot.getStack();
        String name = stack.getName().asString();
        ClaimPermission perm;
        try {
            perm = PermissionRegistry.get(name);
        } catch (NullPointerException e) {
            return false;
        }
        boolean success;
        if (this.group == null) {
            int mode;
            if (this.claim.parentClaim() == null)
                mode = this.claim.permEnabled(perm) == 1 ? -1 : 1;
            else
                mode = this.claim.permEnabled(perm) + 1;
            success = this.claim.editGlobalPerms(player, perm, mode);
        } else
            success = this.claim.editPerms(player, this.group, perm, this.claim.groupHasPerm(this.group, perm) + 1);
        slot.setStack(ServerScreenHelper.fromPermission(this.claim, perm, this.group));
        if (success)
            ServerScreenHelper.playSongToPlayer(player, SoundEvents.BLOCK_NOTE_BLOCK_PLING, 1, 1.2f);
        else
            ServerScreenHelper.playSongToPlayer(player, SoundEvents.ENTITY_VILLAGER_NO, 1, 1f);
        return true;
    }

    @Override
    protected boolean isRightSlot(int slot) {
        return slot == 0 || (this.page > 0 && slot == 47) || (this.page < this.maxPages && slot == 51) || (slot < 45 && slot > 8 && slot % 9 != 0 && slot % 9 != 8);
    }
}
