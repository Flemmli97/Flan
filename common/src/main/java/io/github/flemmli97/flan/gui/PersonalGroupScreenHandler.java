package io.github.flemmli97.flan.gui;

import io.github.flemmli97.flan.api.permission.PermissionRegistry;
import io.github.flemmli97.flan.claim.PermHelper;
import io.github.flemmli97.flan.config.ConfigHandler;
import io.github.flemmli97.flan.gui.inv.SeparateInv;
import io.github.flemmli97.flan.player.PlayerClaimData;
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

public class PersonalGroupScreenHandler extends ServerOnlyScreenHandler<Object> {

    private boolean removeMode;

    private PersonalGroupScreenHandler(int syncId, PlayerInventory playerInventory) {
        super(syncId, playerInventory, 6, null);
    }

    public static void openGroupMenu(PlayerEntity player) {
        NamedScreenHandlerFactory fac = new NamedScreenHandlerFactory() {
            @Override
            public ScreenHandler createMenu(int syncId, PlayerInventory inv, PlayerEntity player) {
                return new PersonalGroupScreenHandler(syncId, inv);
            }

            @Override
            public Text getDisplayName() {
                return PermHelper.simpleColoredText(ConfigHandler.lang.screenPersonalGroups);
            }
        };
        player.openHandledScreen(fac);
    }

    @Override
    protected void fillInventoryWith(PlayerEntity player, SeparateInv inv, Object additionalData) {
        if (!(player instanceof ServerPlayerEntity))
            return;
        for (int i = 0; i < 54; i++) {
            if (i == 0) {
                ItemStack close = new ItemStack(Items.TNT);
                close.setCustomName(ServerScreenHelper.coloredGuiText(ConfigHandler.lang.screenBack, Formatting.DARK_RED));
                inv.updateStack(i, close);
            } else if (i == 3) {
                ItemStack stack = new ItemStack(Items.ANVIL);
                stack.setCustomName(ServerScreenHelper.coloredGuiText(ConfigHandler.lang.screenAdd, Formatting.DARK_GREEN));
                inv.updateStack(i, stack);
            } else if (i == 4) {
                ItemStack stack = new ItemStack(Items.REDSTONE_BLOCK);
                stack.setCustomName(ServerScreenHelper.coloredGuiText(String.format(ConfigHandler.lang.screenRemoveMode, this.removeMode), Formatting.DARK_RED));
                inv.updateStack(i, stack);
            } else if (i < 9 || i > 44 || i % 9 == 0 || i % 9 == 8)
                inv.updateStack(i, ServerScreenHelper.emptyFiller());
            else {
                List<String> groups = new ArrayList<>(PlayerClaimData.get((ServerPlayerEntity) player).playerDefaultGroups().keySet());
                groups.sort(null);
                int row = i / 9 - 1;
                int id = (i % 9) + row * 7 - 1;
                if (id < groups.size()) {
                    ItemStack group = new ItemStack(Items.PAPER);
                    group.setCustomName(ServerScreenHelper.coloredGuiText(groups.get(id), Formatting.DARK_BLUE));
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
    protected boolean handleSlotClicked(ServerPlayerEntity player, int index, Slot slot, int clickType) {
        if (index == 0) {
            player.closeHandledScreen();
            ServerScreenHelper.playSongToPlayer(player, SoundEvents.UI_BUTTON_CLICK, 1, 1f);
            return true;
        }
        if (index == 3) {
            player.closeHandledScreen();
            player.getServer().execute(() -> StringResultScreenHandler.createNewStringResult(player, (s) -> {
                PlayerClaimData.get(player).editDefaultPerms(s, PermissionRegistry.EDITPERMS, -1);
                player.closeHandledScreen();
                player.getServer().execute(() -> PersonalGroupScreenHandler.openGroupMenu(player));
                ServerScreenHelper.playSongToPlayer(player, SoundEvents.BLOCK_ANVIL_USE, 1, 1f);
            }, () -> {
                player.closeHandledScreen();
                player.getServer().execute(() -> PersonalGroupScreenHandler.openGroupMenu(player));
                ServerScreenHelper.playSongToPlayer(player, SoundEvents.ENTITY_VILLAGER_NO, 1, 1f);
            }));
            ServerScreenHelper.playSongToPlayer(player, SoundEvents.UI_BUTTON_CLICK, 1, 1f);
            return true;
        }
        if (index == 4) {
            this.removeMode = !this.removeMode;
            ItemStack stack = new ItemStack(Items.REDSTONE_BLOCK);
            stack.setCustomName(ServerScreenHelper.coloredGuiText(String.format(ConfigHandler.lang.screenRemoveMode, this.removeMode), Formatting.DARK_RED));
            slot.setStack(stack);
            ServerScreenHelper.playSongToPlayer(player, SoundEvents.UI_BUTTON_CLICK, 1, 1f);
            return true;
        }
        ItemStack stack = slot.getStack();
        if (!stack.isEmpty()) {
            String name = stack.getName().asString();
            if (this.removeMode) {
                PlayerClaimData.get(player).playerDefaultGroups().remove(name);
                slot.setStack(ItemStack.EMPTY);
                ServerScreenHelper.playSongToPlayer(player, SoundEvents.ENTITY_BAT_DEATH, 1, 1f);
            } else {
                player.closeHandledScreen();
                player.getServer().execute(() -> PersonalPermissionScreenHandler.openClaimMenu(player, name));
                ServerScreenHelper.playSongToPlayer(player, SoundEvents.UI_BUTTON_CLICK, 1, 1f);
            }
        }
        return false;
    }
}