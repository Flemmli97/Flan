package io.github.flemmli97.flan.gui;

import io.github.flemmli97.flan.api.permission.PermissionRegistry;
import io.github.flemmli97.flan.claim.PermHelper;
import io.github.flemmli97.flan.config.ConfigHandler;
import io.github.flemmli97.flan.gui.inv.SeparateInv;
import io.github.flemmli97.flan.player.PlayerClaimData;
import net.minecraft.ChatFormatting;
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

import java.util.ArrayList;
import java.util.List;

public class PersonalGroupScreenHandler extends ServerOnlyScreenHandler<Object> {

    private boolean removeMode;

    private PersonalGroupScreenHandler(int syncId, Inventory playerInventory) {
        super(syncId, playerInventory, 6, null);
    }

    public static void openGroupMenu(Player player) {
        MenuProvider fac = new MenuProvider() {
            @Override
            public AbstractContainerMenu createMenu(int syncId, Inventory inv, Player player) {
                return new PersonalGroupScreenHandler(syncId, inv);
            }

            @Override
            public Component getDisplayName() {
                return PermHelper.simpleColoredText(ConfigHandler.langManager.get("screenPersonalGroups"));
            }
        };
        player.openMenu(fac);
    }

    @Override
    protected void fillInventoryWith(Player player, SeparateInv inv, Object additionalData) {
        if (!(player instanceof ServerPlayer))
            return;
        for (int i = 0; i < 54; i++) {
            if (i == 0) {
                ItemStack close = new ItemStack(Items.TNT);
                close.setHoverName(ServerScreenHelper.coloredGuiText(ConfigHandler.langManager.get("screenBack"), ChatFormatting.DARK_RED));
                inv.updateStack(i, close);
            } else if (i == 3) {
                ItemStack stack = new ItemStack(Items.ANVIL);
                stack.setHoverName(ServerScreenHelper.coloredGuiText(ConfigHandler.langManager.get("screenAdd"), ChatFormatting.DARK_GREEN));
                inv.updateStack(i, stack);
            } else if (i == 4) {
                ItemStack stack = new ItemStack(Items.REDSTONE_BLOCK);
                stack.setHoverName(ServerScreenHelper.coloredGuiText(String.format(ConfigHandler.langManager.get("screenRemoveMode"), this.removeMode ? ConfigHandler.langManager.get("screenTrue") : ConfigHandler.langManager.get("screenFalse")), ChatFormatting.DARK_RED));
                inv.updateStack(i, stack);
            } else if (i < 9 || i > 44 || i % 9 == 0 || i % 9 == 8)
                inv.updateStack(i, ServerScreenHelper.emptyFiller());
            else {
                List<String> groups = new ArrayList<>(PlayerClaimData.get((ServerPlayer) player).playerDefaultGroups().keySet());
                groups.sort(null);
                int row = i / 9 - 1;
                int id = (i % 9) + row * 7 - 1;
                if (id < groups.size()) {
                    ItemStack group = new ItemStack(Items.PAPER);
                    group.setHoverName(ServerScreenHelper.coloredGuiText(String.format(ConfigHandler.langManager.get("screenGroupName"), groups.get(id)), ChatFormatting.DARK_BLUE));
                    inv.updateStack(i, group);
                }
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
            ServerScreenHelper.playSongToPlayer(player, SoundEvents.UI_BUTTON_CLICK, 1, 1f);
            return true;
        }
        if (index == 3) {
            player.closeContainer();
            player.getServer().execute(() -> StringResultScreenHandler.createNewStringResult(player, (s) -> {
                PlayerClaimData.get(player).editDefaultPerms(s, PermissionRegistry.EDITPERMS, -1);
                player.closeContainer();
                player.getServer().execute(() -> PersonalGroupScreenHandler.openGroupMenu(player));
                ServerScreenHelper.playSongToPlayer(player, SoundEvents.ANVIL_USE, 1, 1f);
            }, () -> {
                player.closeContainer();
                player.getServer().execute(() -> PersonalGroupScreenHandler.openGroupMenu(player));
                ServerScreenHelper.playSongToPlayer(player, SoundEvents.VILLAGER_NO, 1, 1f);
            }));
            ServerScreenHelper.playSongToPlayer(player, SoundEvents.UI_BUTTON_CLICK, 1, 1f);
            return true;
        }
        if (index == 4) {
            this.removeMode = !this.removeMode;
            ItemStack stack = new ItemStack(Items.REDSTONE_BLOCK);
            stack.setHoverName(ServerScreenHelper.coloredGuiText(String.format(ConfigHandler.langManager.get("screenRemoveMode"), this.removeMode ? ConfigHandler.langManager.get("screenTrue") : ConfigHandler.langManager.get("screenFalse")), ChatFormatting.DARK_RED));
            slot.set(stack);
            ServerScreenHelper.playSongToPlayer(player, SoundEvents.UI_BUTTON_CLICK, 1, 1f);
            return true;
        }
        ItemStack stack = slot.getItem();
        if (!stack.isEmpty()) {
            String name = stack.getHoverName().getString();
            if (this.removeMode) {
                PlayerClaimData.get(player).playerDefaultGroups().remove(name);
                slot.set(ItemStack.EMPTY);
                ServerScreenHelper.playSongToPlayer(player, SoundEvents.BAT_DEATH, 1, 1f);
            } else {
                player.closeContainer();
                player.getServer().execute(() -> PersonalPermissionScreenHandler.openClaimMenu(player, name));
                ServerScreenHelper.playSongToPlayer(player, SoundEvents.UI_BUTTON_CLICK, 1, 1f);
            }
        }
        return false;
    }
}