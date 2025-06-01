package io.github.flemmli97.flan.gui;

import io.github.flemmli97.flan.api.permission.ClaimPermission;
import io.github.flemmli97.flan.api.permission.PermissionManager;
import io.github.flemmli97.flan.claim.ClaimUtils;
import io.github.flemmli97.flan.player.PlayerClaimData;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
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
import net.minecraft.world.item.component.CustomData;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PersonalPermissionScreenHandler extends PagedServerOnlyScreenHandler<String> {

    private List<ClaimPermission> perms;

    private PersonalPermissionScreenHandler(int syncId, Inventory playerInventory, String group) {
        super(syncId, playerInventory, 6, group);
    }

    public static void openClaimMenu(Player player, String group) {
        MenuProvider fac = new MenuProvider() {
            @Override
            public AbstractContainerMenu createMenu(int syncId, Inventory inv, Player player) {
                return new PersonalPermissionScreenHandler(syncId, inv, group);
            }

            @Override
            public Component getDisplayName() {
                return ClaimUtils.translatedText("flan.screenPersonalPermissions", group);
            }
        };
        player.openMenu(fac);
    }

    @Override
    protected void fillInventoryWith() {
        this.perms = new ArrayList<>(PermissionManager.getInstance().getAll());
        if (this.data != null)
            this.perms.removeIf(p -> p.global);
        for (int i = 0; i < 54; i++) {
            if (i == 0) {
                ItemStack stack = ServerScreenHelper.createStack(Items.TNT,
                        ServerScreenHelper.coloredGuiText("flan.screenBack", ChatFormatting.DARK_RED));
                this.slots.get(i).set(stack);
            } else if (i < 9 || i > 44 || i % 9 == 0 || i % 9 == 8)
                this.slots.get(i).set(ServerScreenHelper.emptyFiller());
            else {
                int row = i / 9 - 1;
                int id = (i % 9) + row * 7 - 1 + this.getPage() * 28;
                if (id < this.perms.size())
                    this.slots.get(i).set(ServerScreenHelper.getFromPersonal(this.player, this.perms.get(id), this.data));
                else
                    this.slots.get(i).set(ItemStack.EMPTY);
            }
        }
    }

    @Override
    protected boolean handleSlotClicked(ServerPlayer player, int index, Slot slot, int clickType) {
        if (index == 0) {
            player.closeContainer();
            player.getServer().execute(() -> PersonalGroupScreenHandler.openGroupMenu(player));
            ServerScreenHelper.playSongToPlayer(player, SoundEvents.UI_BUTTON_CLICK, 1, 1f);
            return true;
        }
        ItemStack stack = slot.getItem();
        ClaimPermission perm;
        try {
            perm = PermissionManager.getInstance().get(ResourceLocation.parse(stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY)
                    .copyTag().getStringOr(ServerScreenHelper.PERMISSION_KEY, "")));
            if (perm == null)
                return false;
        } catch (NullPointerException e) {
            return false;
        }
        PlayerClaimData data = PlayerClaimData.get(player);
        Map<ResourceLocation, Boolean> perms = data.playerDefaultGroups().getOrDefault(this.data, new HashMap<>());
        boolean success = data.editDefaultPerms(this.data, perm.getId(), (perms.containsKey(perm.getId()) ? perms.get(perm.getId()) ? 1 : 0 : -1) + 1);
        slot.set(ServerScreenHelper.getFromPersonal(player, perm, this.data));
        if (success)
            ServerScreenHelper.playSongToPlayer(player, SoundEvents.NOTE_BLOCK_PLING, 1, 1.2f);
        else
            ServerScreenHelper.playSongToPlayer(player, SoundEvents.VILLAGER_NO, 1, 1f);
        return true;
    }

    @Override
    protected boolean isRightSlot(int slot) {
        return slot == 0 || (slot < 45 && slot > 8 && slot % 9 != 0 && slot % 9 != 8);
    }

    @Override
    protected PagedServerOnlyScreenHandler.PageSettings pageSettings() {
        return new PagedServerOnlyScreenHandler.PageSettings((this.perms.size() - 1) / 28, 47, 51);
    }
}
