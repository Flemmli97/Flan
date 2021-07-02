package io.github.flemmli97.flan.gui;

import io.github.flemmli97.flan.gui.inv.SeparateInv;
import io.github.flemmli97.flan.gui.inv.SeparateInvImpl;
import io.github.flemmli97.flan.gui.inv.SlotDelegate;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerListener;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.ArrayList;
import java.util.List;

public abstract class ServerOnlyScreenHandler<T> extends ScreenHandler {

    private final SeparateInvImpl inventory;
    private final List<ScreenHandlerListener> listeners = new ArrayList<>();
    private boolean update = true;

    protected ServerOnlyScreenHandler(int syncId, PlayerInventory playerInventory, int rows, T additionalData) {
        super(fromRows(rows), syncId);
        int i = (rows - 4) * 18;
        this.inventory = new SeparateInvImpl(rows * 9);
        this.fillInventoryWith(playerInventory.player, this.inventory, additionalData);
        int n;
        int m;
        for (n = 0; n < rows; ++n) {
            for (m = 0; m < 9; ++m) {
                this.addSlot(new SlotDelegate(this.inventory, m + n * 9, 8 + m * 18, 18 + n * 18));
            }
        }

        for (n = 0; n < 3; ++n) {
            for (m = 0; m < 9; ++m) {
                this.addSlot(new Slot(playerInventory, m + n * 9 + 9, 8 + m * 18, 103 + n * 18 + i) {
                    @Override
                    public boolean canInsert(ItemStack stack) {
                        return false;
                    }

                    @Override
                    public boolean canTakeItems(PlayerEntity playerEntity) {
                        return false;
                    }
                });
            }
        }

        for (n = 0; n < 9; ++n) {
            this.addSlot(new Slot(playerInventory, n, 8 + n * 18, 161 + i) {
                @Override
                public boolean canInsert(ItemStack stack) {
                    return false;
                }

                @Override
                public boolean canTakeItems(PlayerEntity playerEntity) {
                    return false;
                }
            });
        }
    }

    private static ScreenHandlerType<GenericContainerScreenHandler> fromRows(int rows) {
        switch (rows) {
            case 2:
                return ScreenHandlerType.GENERIC_9X2;
            case 3:
                return ScreenHandlerType.GENERIC_9X3;
            case 4:
                return ScreenHandlerType.GENERIC_9X4;
            case 5:
                return ScreenHandlerType.GENERIC_9X5;
            case 6:
                return ScreenHandlerType.GENERIC_9X6;
        }
        return ScreenHandlerType.GENERIC_9X1;
    }

    protected abstract void fillInventoryWith(PlayerEntity player, SeparateInv inv, T additionalData);

    @Override
    public boolean canUse(PlayerEntity player) {
        return true;
    }

    @Override
    public ItemStack onSlotClick(int i, int j, SlotActionType actionType, PlayerEntity playerEntity) {
        if (i < 0)
            return ItemStack.EMPTY;
        Slot slot = this.slots.get(i);
        if (this.isRightSlot(i))
            this.handleSlotClicked((ServerPlayerEntity) playerEntity, i, slot, j);
        ItemStack stack = slot.getStack().copy();
        for (ScreenHandlerListener listener : this.listeners)
            listener.onSlotUpdate(this, i, stack);
        ((ServerPlayerEntity) playerEntity).updateCursorStack();
        return stack;
    }

    @Override
    public ItemStack transferSlot(PlayerEntity player, int index) {
        if (index < 0)
            return ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        if (this.isRightSlot(index))
            this.handleSlotClicked((ServerPlayerEntity) player, index, slot, 0);
        return slot.getStack().copy();
    }

    @Override
    public void addListener(ScreenHandlerListener listener) {
        this.update = false;
        super.addListener(listener);
        if (!this.listeners.contains(listener)) {
            this.listeners.add(listener);
            listener.onHandlerRegistered(this, this.getStacks());
            this.update = true;
            this.sendContentUpdates();
        }
    }

    @Override
    public void sendContentUpdates() {
        if (this.update)
            super.sendContentUpdates();
    }

    protected abstract boolean isRightSlot(int slot);

    /**
     * @param clickType 0 for left click, 1 for right click
     */
    protected abstract boolean handleSlotClicked(ServerPlayerEntity player, int index, Slot slot, int clickType);
}
