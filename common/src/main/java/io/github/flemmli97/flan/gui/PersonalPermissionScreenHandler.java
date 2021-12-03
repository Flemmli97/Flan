package io.github.flemmli97.flan.gui;

import io.github.flemmli97.flan.api.permission.ClaimPermission;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PersonalPermissionScreenHandler extends ServerOnlyScreenHandler<String> {

    private final String group;
    private int page;
    private final Player player;

    private PersonalPermissionScreenHandler(int syncId, Inventory playerInventory, String group) {
        super(syncId, playerInventory, 6, group);
        this.group = group;
        this.player = playerInventory.player;
    }

    public static void openClaimMenu(Player player, String group) {
        MenuProvider fac = new MenuProvider() {
            @Override
            public AbstractContainerMenu createMenu(int syncId, Inventory inv, Player player) {
                return new PersonalPermissionScreenHandler(syncId, inv, group);
            }

            @Override
            public Component getDisplayName() {
                return PermHelper.simpleColoredText(String.format(ConfigHandler.lang.screenPersonalPermissions, group));
            }
        };
        player.openMenu(fac);
    }

    @Override
    protected void fillInventoryWith(Player player, SeparateInv inv, String group) {
        if (!(player instanceof ServerPlayer))
            return;
        List<ClaimPermission> perms = new ArrayList<>(PermissionRegistry.getPerms());
        if (this.group != null)
            perms.removeAll(PermissionRegistry.globalPerms());
        for (int i = 0; i < 54; i++) {
            int page = 0;
            if (i == 0) {
                ItemStack close = new ItemStack(Items.TNT);
                close.setHoverName(ServerScreenHelper.coloredGuiText(ConfigHandler.lang.screenBack, ChatFormatting.DARK_RED));
                inv.updateStack(i, close);
            } else if (page == 1 && i == 47) {
                ItemStack close = new ItemStack(Items.ARROW);
                close.setHoverName(ServerScreenHelper.coloredGuiText(ConfigHandler.lang.screenPrevious, ChatFormatting.WHITE));
                inv.updateStack(i, close);
            } else if (page == 0 && i == 51) {
                ItemStack close = new ItemStack(Items.ARROW);
                close.setHoverName(ServerScreenHelper.coloredGuiText(ConfigHandler.lang.screenNext, ChatFormatting.WHITE));
                inv.updateStack(i, close);
            } else if (i < 9 || i > 44 || i % 9 == 0 || i % 9 == 8)
                inv.updateStack(i, ServerScreenHelper.emptyFiller());
            else {
                int row = i / 9 - 1;
                int id = (i % 9) + row * 7 - 1 + page * 28;
                if (id < perms.size())
                    inv.updateStack(i, ServerScreenHelper.getFromPersonal((ServerPlayer) player, perms.get(id), group));
            }
        }
    }

    private void flipPage() {
        if (!(this.player instanceof ServerPlayer))
            return;
        List<ClaimPermission> perms = new ArrayList<>(PermissionRegistry.getPerms());
        if (this.group != null)
            perms.removeAll(PermissionRegistry.globalPerms());
        int maxPages = perms.size() / 28;
        for (int i = 0; i < 54; i++) {
            if (i == 0) {
                ItemStack close = new ItemStack(Items.TNT);
                close.setHoverName(ServerScreenHelper.coloredGuiText(ConfigHandler.lang.screenBack, ChatFormatting.DARK_RED));
                this.slots.get(i).set(close);
            } else if (i == 47) {
                ItemStack stack = ServerScreenHelper.emptyFiller();
                if (this.page >= 1) {
                    stack = new ItemStack(Items.ARROW);
                    stack.setHoverName(ServerScreenHelper.coloredGuiText(ConfigHandler.lang.screenPrevious, ChatFormatting.WHITE));
                }
                this.slots.get(i).set(stack);
            } else if (i == 51) {
                ItemStack stack = ServerScreenHelper.emptyFiller();
                if (this.page < maxPages) {
                    stack = new ItemStack(Items.ARROW);
                    stack.setHoverName(ServerScreenHelper.coloredGuiText(ConfigHandler.lang.screenNext, ChatFormatting.WHITE));
                }
                this.slots.get(i).set(stack);
            } else if (i < 9 || i > 44 || i % 9 == 0 || i % 9 == 8)
                this.slots.get(i).set(ServerScreenHelper.emptyFiller());
            else {
                int row = i / 9 - 1;
                int id = (i % 9) + row * 7 - 1 + this.page * 28;
                if (id < perms.size()) {
                    this.slots.get(i).set(ServerScreenHelper.getFromPersonal((ServerPlayer) this.player, perms.get(id), this.group));
                } else
                    this.slots.get(i).set(ItemStack.EMPTY);
            }
        }
        this.broadcastChanges();
    }

    @Override
    protected boolean handleSlotClicked(ServerPlayer player, int index, Slot slot, int clickType) {
        if (index == 0) {
            player.closeContainer();
            player.getServer().execute(() -> PersonalGroupScreenHandler.openGroupMenu(player));
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
        ItemStack stack = slot.getItem();
        String name = stack.getHoverName().getContents();
        ClaimPermission perm;
        try {
            perm = PermissionRegistry.get(name);
        } catch (NullPointerException e) {
            return false;
        }
        PlayerClaimData data = PlayerClaimData.get(player);
        Map<ClaimPermission, Boolean> perms = data.playerDefaultGroups().getOrDefault(this.group, new HashMap<>());
        boolean success = data.editDefaultPerms(this.group, perm, (perms.containsKey(perm) ? perms.get(perm) ? 1 : 0 : -1) + 1);
        slot.set(ServerScreenHelper.getFromPersonal(player, perm, this.group));
        if (success)
            ServerScreenHelper.playSongToPlayer(player, SoundEvents.NOTE_BLOCK_PLING, 1, 1.2f);
        else
            ServerScreenHelper.playSongToPlayer(player, SoundEvents.VILLAGER_NO, 1, 1f);
        return true;
    }

    @Override
    protected boolean isRightSlot(int slot) {
        return slot == 0 || (this.page == 1 && slot == 47) || (this.page == 0 && slot == 51) || (slot < 45 && slot > 8 && slot % 9 != 0 && slot % 9 != 8);
    }
}
