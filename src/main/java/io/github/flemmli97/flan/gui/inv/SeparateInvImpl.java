package io.github.flemmli97.flan.gui.inv;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class SeparateInvImpl extends SimpleInventory implements SeparateInv {

    public SeparateInvImpl(int size) {
        super(size);
    }

    @Override
    public ItemStack getStack(int slot) {
        return ItemStack.EMPTY;
    }

    @Override
    public List<ItemStack> clearToList() {
        return new ArrayList<>();
    }

    @Override
    public ItemStack removeStack(int slot, int amount) {
        return ItemStack.EMPTY;
    }

    @Override
    public ItemStack removeItem(Item item, int count) {
        return ItemStack.EMPTY;
    }

    @Override
    public ItemStack addStack(ItemStack stack) {
        return stack;
    }

    @Override
    public boolean canInsert(ItemStack stack) {
        return false;
    }

    @Override
    public ItemStack removeStack(int slot) {
        return ItemStack.EMPTY;
    }

    @Override
    public void setStack(int slot, ItemStack stack) {

    }

    @Override
    public boolean canPlayerUse(PlayerEntity player) {
        return false;
    }

    @Override
    public void updateStack(int slot, ItemStack stack) {
        super.setStack(slot, stack);
    }

    @Override
    public ItemStack getActualStack(int slot) {
        return super.getStack(slot);
    }
}
