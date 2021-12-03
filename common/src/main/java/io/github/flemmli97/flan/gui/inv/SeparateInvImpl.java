package io.github.flemmli97.flan.gui.inv;

import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class SeparateInvImpl extends SimpleContainer implements SeparateInv {

    public SeparateInvImpl(int size) {
        super(size);
    }

    @Override
    public ItemStack getItem(int slot) {
        return ItemStack.EMPTY;
    }

    @Override
    public List<ItemStack> removeAllItems() {
        return new ArrayList<>();
    }

    @Override
    public ItemStack removeItem(int slot, int amount) {
        return ItemStack.EMPTY;
    }

    @Override
    public ItemStack removeItemType(Item item, int count) {
        return ItemStack.EMPTY;
    }

    @Override
    public ItemStack addItem(ItemStack stack) {
        return stack;
    }

    @Override
    public boolean canAddItem(ItemStack stack) {
        return false;
    }

    @Override
    public ItemStack removeItemNoUpdate(int slot) {
        return ItemStack.EMPTY;
    }

    @Override
    public void setItem(int slot, ItemStack stack) {

    }

    @Override
    public boolean stillValid(Player player) {
        return false;
    }

    @Override
    public void updateStack(int slot, ItemStack stack) {
        super.setItem(slot, stack);
    }

    @Override
    public ItemStack getActualStack(int slot) {
        return super.getItem(slot);
    }
}
