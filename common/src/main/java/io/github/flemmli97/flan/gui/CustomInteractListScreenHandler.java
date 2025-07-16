package io.github.flemmli97.flan.gui;

import io.github.flemmli97.flan.claim.Claim;
import io.github.flemmli97.flan.claim.ClaimUtils;
import io.github.flemmli97.flan.claim.attachment.ClaimAllowListKey;
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

public class CustomInteractListScreenHandler extends PagedServerOnlyScreenHandler<CustomInteractListScreenHandler.Data> {

    private boolean removeMode;

    private CustomInteractListScreenHandler(int syncId, Inventory playerInventory, Data data) {
        super(syncId, playerInventory, 6, data);
    }

    public static void openMenu(Player player, ClaimAllowListKey<?> key, Claim claim) {
        MenuProvider fac = new MenuProvider() {
            @Override
            public AbstractContainerMenu createMenu(int syncId, Inventory inv, Player player) {
                return new CustomInteractListScreenHandler(syncId, inv, new Data(claim, key));
            }

            @Override
            public Component getDisplayName() {
                return ClaimUtils.translatedText(key.translationKey());
            }
        };
        player.openMenu(fac);
    }

    @Override
    protected void fillInventoryWith() {
        for (int i = 0; i < 54; i++) {
            if (i == 0) {
                ItemStack stack = ServerScreenHelper.createStack(Items.TNT,
                        ServerScreenHelper.coloredGuiText("flan.screenBack", ChatFormatting.DARK_RED));
                this.slots.get(i).set(stack);
            } else if (i == 3) {
                ItemStack stack = ServerScreenHelper.createStack(Items.ANVIL, ServerScreenHelper.coloredGuiText("flan.screenAdd", ChatFormatting.DARK_GREEN));
                this.slots.get(i).set(stack);
            } else if (i == 4) {
                ItemStack stack = ServerScreenHelper.createStack(Items.REDSTONE_BLOCK,
                        ServerScreenHelper.coloredGuiText("flan.screenRemoveMode", this.removeMode, ChatFormatting.DARK_RED));
                this.slots.get(i).set(stack);
            } else if (i < 9 || i > 44 || i % 9 == 0 || i % 9 == 8)
                this.slots.get(i).set(ServerScreenHelper.emptyFiller());
            else {
                List<ItemStack> stacks = this.data.claim.allowedEntries.get(this.data.key).asStacks();
                int row = i / 9 - 1;
                int id = (i % 9) + row * 7 - 1 + this.getPage() * 28;
                if (id < stacks.size()) {
                    ItemStack stack = stacks.get(id);
                    CustomData.update(DataComponents.CUSTOM_DATA, stack, tag -> tag.putInt("Index", id));
                    this.slots.get(i).set(stack);
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
            player.getServer().execute(() -> ClaimAllowListEntryScreenHandler.openScreen(player, this.data.claim));
            ServerScreenHelper.playSongToPlayer(player, SoundEvents.UI_BUTTON_CLICK, 1, 1f);
            return true;
        }
        if (index == 3) {
            player.closeContainer();
            player.getServer().execute(() -> StringResultScreenHandler.createNewStringResult(player, (s) -> {
                this.data.claim.allowedEntries.get(this.data.key).addAllowedItem(s);
                player.closeContainer();
                player.getServer().execute(() -> CustomInteractListScreenHandler.openMenu(player, this.data.key, this.data.claim));
                ServerScreenHelper.playSongToPlayer(player, SoundEvents.ANVIL_USE, 1, 1f);
            }, () -> {
                player.closeContainer();
                player.getServer().execute(() -> CustomInteractListScreenHandler.openMenu(player, this.data.key, this.data.claim));
                ServerScreenHelper.playSongToPlayer(player, SoundEvents.VILLAGER_NO, 1, 1f);
            }));
            ServerScreenHelper.playSongToPlayer(player, SoundEvents.UI_BUTTON_CLICK, 1, 1f);
            return true;
        }
        if (index == 4) {
            this.removeMode = !this.removeMode;
            ItemStack stack = ServerScreenHelper.createStack(Items.REDSTONE_BLOCK,
                    ServerScreenHelper.coloredGuiText("flan.screenRemoveMode", this.removeMode, ChatFormatting.DARK_RED));
            slot.set(stack);
            ServerScreenHelper.playSongToPlayer(player, SoundEvents.UI_BUTTON_CLICK, 1, 1f);
            return true;
        }
        ItemStack stack = slot.getItem();
        if (!stack.isEmpty()) {
            CustomData nbt = stack.get(DataComponents.CUSTOM_DATA);
            int idx = nbt != null ? nbt.copyTag().getIntOr("Index", 0) : 0;
            if (this.removeMode) {
                this.data.claim.allowedEntries.get(this.data.key).removeAllowedItem(idx);
                slot.set(ItemStack.EMPTY);
                ServerScreenHelper.playSongToPlayer(player, SoundEvents.BAT_DEATH, 1, 1f);
            }
        }
        return false;
    }

    @Override
    protected PageSettings pageSettings() {
        int size = this.data.claim.allowedEntries.get(this.data.key).size();
        return new PageSettings((size - 1) / 28, 47, 51);
    }

    public record Data(Claim claim, ClaimAllowListKey<?> key) {

    }
}