package io.github.flemmli97.flan.gui.inv;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class SlotDelegate extends Slot {

    private final int index;

    public SlotDelegate(SeparateInvImpl inventory, int index, int x, int y) {
        super(inventory, index, x, y);
        this.index = index;
    }

    @Override
    public void set(ItemStack stack) {
        ((SeparateInvImpl) this.container).updateStack(this.index, stack);
    }

    @Override
    public ItemStack getItem() {
        return ((SeparateInvImpl) this.container).getActualStack(this.index);
    }

    @Override
    public boolean mayPlace(ItemStack stack) {
        return false;
    }

    @Override
    public boolean mayPickup(Player playerEntity) {
        return false;
    }
}
