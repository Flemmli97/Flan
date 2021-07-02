package io.github.flemmli97.flan.gui.inv;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;

public class SlotDelegate extends Slot {

    private final int index;

    public SlotDelegate(SeparateInvImpl inventory, int index, int x, int y) {
        super(inventory, index, x, y);
        this.index = index;
    }

    @Override
    public void setStack(ItemStack stack) {
        ((SeparateInvImpl) this.inventory).updateStack(this.index, stack);
    }

    @Override
    public ItemStack getStack() {
        return ((SeparateInvImpl) this.inventory).getActualStack(this.index);
    }

    @Override
    public boolean canInsert(ItemStack stack) {
        return false;
    }

    @Override
    public boolean canTakeItems(PlayerEntity playerEntity) {
        return false;
    }
}
