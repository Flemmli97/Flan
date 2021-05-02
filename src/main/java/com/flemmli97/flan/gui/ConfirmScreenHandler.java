package com.flemmli97.flan.gui;

import com.flemmli97.flan.claim.PermHelper;
import com.flemmli97.flan.config.ConfigHandler;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.function.Consumer;

public class ConfirmScreenHandler extends ServerOnlyScreenHandler {

    private final Consumer<Boolean> cons;

    private ConfirmScreenHandler(int syncId, PlayerInventory playerInventory, Consumer<Boolean> cons) {
        super(syncId, playerInventory, 1);
        this.cons = cons;
    }

    public static void openConfirmScreen(ServerPlayerEntity player, Consumer<Boolean> process) {
        NamedScreenHandlerFactory fac = new NamedScreenHandlerFactory() {
            @Override
            public ScreenHandler createMenu(int syncId, PlayerInventory inv, PlayerEntity player) {
                return new ConfirmScreenHandler(syncId, inv, process);
            }

            @Override
            public Text getDisplayName() {
                return PermHelper.simpleColoredText(ConfigHandler.lang.screenConfirm);
            }
        };
        player.openHandledScreen(fac);
    }


    @Override
    protected void fillInventoryWith(PlayerEntity player, Inventory inv, Object... additionalData) {
        for (int i = 0; i < 9; i++) {
            switch (i) {
                case 3:
                    ItemStack yes = new ItemStack(Items.GREEN_WOOL);
                    yes.setCustomName(new LiteralText(ConfigHandler.lang.screenYes).setStyle(Style.EMPTY.withFormatting(Formatting.GREEN)));
                    inv.setStack(i, yes);
                    break;
                case 5:
                    ItemStack no = new ItemStack(Items.RED_WOOL);
                    no.setCustomName(new LiteralText(ConfigHandler.lang.screenNo).setStyle(Style.EMPTY.withFormatting(Formatting.RED)));
                    inv.setStack(i, no);
                    break;
                default:
                    inv.setStack(i, ServerScreenHelper.emptyFiller());
            }
        }
    }

    @Override
    protected boolean isRightSlot(int slot) {
        return slot == 3 || slot == 5;
    }

    @Override
    protected boolean handleSlotClicked(ServerPlayerEntity player, int index, Slot slot, int clickType) {
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
