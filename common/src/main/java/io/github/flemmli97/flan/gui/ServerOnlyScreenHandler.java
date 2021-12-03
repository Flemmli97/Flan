package io.github.flemmli97.flan.gui;

import io.github.flemmli97.flan.gui.inv.SeparateInv;
import io.github.flemmli97.flan.gui.inv.SeparateInvImpl;
import io.github.flemmli97.flan.gui.inv.SlotDelegate;
import io.github.flemmli97.flan.mixin.AbstractContainerAccessor;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.ContainerListener;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public abstract class ServerOnlyScreenHandler<T> extends AbstractContainerMenu {

    private final SeparateInvImpl inventory;
    private boolean update = true;

    protected ServerOnlyScreenHandler(int syncId, Inventory playerInventory, int rows, T additionalData) {
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
                    public boolean mayPlace(ItemStack stack) {
                        return false;
                    }

                    @Override
                    public boolean mayPickup(Player playerEntity) {
                        return false;
                    }
                });
            }
        }

        for (n = 0; n < 9; ++n) {
            this.addSlot(new Slot(playerInventory, n, 8 + n * 18, 161 + i) {
                @Override
                public boolean mayPlace(ItemStack stack) {
                    return false;
                }

                @Override
                public boolean mayPickup(Player playerEntity) {
                    return false;
                }
            });
        }
    }

    private static MenuType<ChestMenu> fromRows(int rows) {
        switch (rows) {
            case 2:
                return MenuType.GENERIC_9x2;
            case 3:
                return MenuType.GENERIC_9x3;
            case 4:
                return MenuType.GENERIC_9x4;
            case 5:
                return MenuType.GENERIC_9x5;
            case 6:
                return MenuType.GENERIC_9x6;
        }
        return MenuType.GENERIC_9x1;
    }

    protected abstract void fillInventoryWith(Player player, SeparateInv inv, T additionalData);

    @Override
    public boolean stillValid(Player player) {
        return true;
    }

    @Override
    public void clicked(int i, int j, ClickType actionType, Player playerEntity) {
        if (i < 0)
            return;
        Slot slot = this.slots.get(i);
        if (this.isRightSlot(i)) {
            if (((AbstractContainerAccessor) this).containerSync() != null)
                ((AbstractContainerAccessor) this).containerSync().sendCarriedChange(this, this.getCarried().copy());
            this.handleSlotClicked((ServerPlayer) playerEntity, i, slot, j);
        }
        ItemStack stack = slot.getItem().copy();
        for (ContainerListener listener : ((AbstractContainerAccessor) this).listeners())
            listener.slotChanged(this, i, stack);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        if (index < 0)
            return ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        if (this.isRightSlot(index))
            this.handleSlotClicked((ServerPlayer) player, index, slot, 0);
        return slot.getItem().copy();
    }

    /*@Override
    public void addSlotListener(ContainerListener listener) {
        this.update = false;
        super.addSlotListener(listener);
        if (!this.listeners.contains(listener)) {
            this.listeners.add(listener);
            listener.refreshContainer(this, this.getItems());
            this.update = true;
            this.broadcastChanges();
        }
    }*/

    @Override
    public void broadcastChanges() {
        //if (this.update)
        super.broadcastChanges();
    }

    protected abstract boolean isRightSlot(int slot);

    /**
     * @param clickType 0 for left click, 1 for right click
     */
    protected abstract boolean handleSlotClicked(ServerPlayer player, int index, Slot slot, int clickType);
}
