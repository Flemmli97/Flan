package io.github.flemmli97.flan.gui;

import io.github.flemmli97.flan.api.permission.BuiltinPermission;
import io.github.flemmli97.flan.claim.Claim;
import io.github.flemmli97.flan.claim.ClaimUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomData;

import java.util.List;

public class GroupScreenHandler extends PagedServerOnlyScreenHandler<Claim> {

    private boolean removeMode;

    private GroupScreenHandler(int syncId, Inventory playerInventory, Claim claim) {
        super(syncId, playerInventory, 6, claim);
    }

    public static void openGroupMenu(Player player, Claim claim) {
        MenuProvider fac = new MenuProvider() {
            @Override
            public AbstractContainerMenu createMenu(int syncId, Inventory inv, Player player) {
                return new GroupScreenHandler(syncId, inv, claim);
            }

            @Override
            public Component getDisplayName() {
                return ClaimUtils.translatedText("flan.screenGroups");
            }
        };
        player.openMenu(fac);
    }

    @Override
    protected void fillInventoryWith() {
        for (int i = 0; i < 54; i++) {
            if (i == 0) {
                ItemStack stack = new ItemStack(Items.TNT);
                stack.set(DataComponents.CUSTOM_NAME, ServerScreenHelper.coloredGuiText("flan.screenBack", ChatFormatting.DARK_RED));
                this.slots.get(i).set(stack);
            } else if (i == 3) {
                ItemStack stack = new ItemStack(Items.ANVIL);
                stack.set(DataComponents.CUSTOM_NAME, ServerScreenHelper.coloredGuiText("flan.screenAdd", ChatFormatting.DARK_GREEN));
                this.slots.get(i).set(stack);
            } else if (i == 4) {
                ItemStack stack = new ItemStack(Items.REDSTONE_BLOCK);
                stack.set(DataComponents.CUSTOM_NAME, ServerScreenHelper.coloredGuiText("flan.screenRemoveMode", this.removeMode ? ServerScreenHelper.coloredGuiText("flan.screenTrue") : ServerScreenHelper.coloredGuiText("flan.screenFalse"), ChatFormatting.DARK_RED));
                this.slots.get(i).set(stack);
            } else if (i < 9 || i > 44 || i % 9 == 0 || i % 9 == 8)
                this.slots.get(i).set(ServerScreenHelper.emptyFiller());
            else {
                List<String> groups = this.data.groups();
                int row = i / 9 - 1;
                int id = (i % 9) + row * 7 - 1 + this.getPage() * 28;
                if (id < groups.size()) {
                    ItemStack group = new ItemStack(Items.PAPER);
                    CustomData.update(DataComponents.CUSTOM_DATA, group, t -> t.putString("FlanGroup", groups.get(id)));
                    group.set(DataComponents.CUSTOM_NAME, ServerScreenHelper.coloredGuiText("flan.screenGroupName", groups.get(id), ChatFormatting.DARK_BLUE));
                    this.slots.get(i).set(group);
                } else
                    this.slots.get(i).set(ItemStack.EMPTY);
            }
        }
    }

    @Override
    protected boolean isRightSlot(int slot) {
        return slot == 0 || slot == 3 || slot == 4 || (slot < 45 && slot > 8 && slot % 9 != 0 && slot % 9 != 8);
    }

    @Override
    protected boolean handleSlotClicked(ServerPlayer player, int index, Slot slot, int clickType) {
        if (index == 0) {
            player.closeContainer();
            player.getServer().execute(() -> ClaimMenuScreenHandler.openClaimMenu(player, this.data));
            ServerScreenHelper.playSongToPlayer(player, SoundEvents.UI_BUTTON_CLICK, 1, 1f);
            return true;
        }
        if (index == 3) {
            player.closeContainer();
            player.getServer().execute(() -> StringResultScreenHandler.createNewStringResult(player, (s) -> {
                this.data.editPerms(player, s, BuiltinPermission.EDITPERMS, -1);
                player.closeContainer();
                player.getServer().execute(() -> GroupScreenHandler.openGroupMenu(player, this.data));
                ServerScreenHelper.playSongToPlayer(player, SoundEvents.ANVIL_USE, 1, 1f);
            }, () -> {
                player.closeContainer();
                player.getServer().execute(() -> GroupScreenHandler.openGroupMenu(player, this.data));
                ServerScreenHelper.playSongToPlayer(player, SoundEvents.VILLAGER_NO, 1, 1f);
            }));
            ServerScreenHelper.playSongToPlayer(player, SoundEvents.UI_BUTTON_CLICK, 1, 1f);
            return true;
        }
        if (index == 4) {
            this.removeMode = !this.removeMode;
            ItemStack stack = new ItemStack(Items.REDSTONE_BLOCK);
            stack.set(DataComponents.CUSTOM_NAME, ServerScreenHelper.coloredGuiText("flan.screenRemoveMode", this.removeMode ? ServerScreenHelper.coloredGuiText("flan.screenTrue") : ServerScreenHelper.coloredGuiText("flan.screenFalse"), ChatFormatting.DARK_RED));
            slot.set(stack);
            ServerScreenHelper.playSongToPlayer(player, SoundEvents.UI_BUTTON_CLICK, 1, 1f);
            return true;
        }
        ItemStack stack = slot.getItem();
        if (!stack.isEmpty()) {
            String name = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY)
                    .copyTag().getString("FlanGroup");
            if (this.removeMode) {
                this.data.removePermGroup(player, name);
                slot.set(ItemStack.EMPTY);
                ServerScreenHelper.playSongToPlayer(player, SoundEvents.BAT_DEATH, 1, 1f);
            } else {
                if (clickType == 1) {
                    player.closeContainer();
                    player.getServer().execute(() -> PermissionScreenHandler.openClaimMenu(player, this.data, name));
                } else {
                    player.closeContainer();
                    player.getServer().execute(() -> GroupPlayerScreenHandler.openPlayerGroupMenu(player, this.data, name));
                }
                ServerScreenHelper.playSongToPlayer(player, SoundEvents.UI_BUTTON_CLICK, 1, 1f);
            }
        }
        return false;
    }

    @Override
    protected PageSettings pageSettings() {
        return new PageSettings((this.data.groups().size() - 1) / 28, 47, 51);
    }
}
