package io.github.flemmli97.flan.gui;

import io.github.flemmli97.flan.claim.PermHelper;
import io.github.flemmli97.flan.config.ConfigHandler;
import io.github.flemmli97.flan.gui.inv.SeparateInv;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.function.Consumer;

public class ConfirmScreenHandler extends ServerOnlyScreenHandler<Object> {

    private final Consumer<Boolean> cons;

    private ConfirmScreenHandler(int syncId, Inventory playerInventory, Consumer<Boolean> cons) {
        super(syncId, playerInventory, 1, null);
        this.cons = cons;
    }

    public static void openConfirmScreen(ServerPlayer player, Consumer<Boolean> process) {
        MenuProvider fac = new MenuProvider() {
            @Override
            public AbstractContainerMenu createMenu(int syncId, Inventory inv, Player player) {
                return new ConfirmScreenHandler(syncId, inv, process);
            }

            @Override
            public Component getDisplayName() {
                return PermHelper.simpleColoredText(ConfigHandler.lang.screenConfirm);
            }
        };
        player.openMenu(fac);
    }


    @Override
    protected void fillInventoryWith(Player player, SeparateInv inv, Object additionalData) {
        for (int i = 0; i < 9; i++) {
            switch (i) {
                case 3:
                    ItemStack yes = new ItemStack(Items.GREEN_WOOL);
                    yes.setHoverName(ServerScreenHelper.coloredGuiText(ConfigHandler.lang.screenYes, ChatFormatting.GREEN));
                    inv.updateStack(i, yes);
                    break;
                case 5:
                    ItemStack no = new ItemStack(Items.RED_WOOL);
                    no.setHoverName(ServerScreenHelper.coloredGuiText(ConfigHandler.lang.screenNo, ChatFormatting.RED));
                    inv.updateStack(i, no);
                    break;
                default:
                    inv.updateStack(i, ServerScreenHelper.emptyFiller());
            }
        }
    }

    @Override
    protected boolean isRightSlot(int slot) {
        return slot == 3 || slot == 5;
    }

    @Override
    protected boolean handleSlotClicked(ServerPlayer player, int index, Slot slot, int clickType) {
        switch (index) {
            case 3:
                this.cons.accept(true);
                break;
            case 5:
                this.cons.accept(false);
                break;
        }
        return true;
    }
}
