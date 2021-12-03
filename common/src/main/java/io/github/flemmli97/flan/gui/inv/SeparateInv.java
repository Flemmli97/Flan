package io.github.flemmli97.flan.gui.inv;

import net.minecraft.world.item.ItemStack;

/**
 * This ensures that other mods will never be able to modify and get items from the inventory.
 * For inventory management mods that also do things on the server (e.g. Quark)
 */
public interface SeparateInv {

    void updateStack(int slot, ItemStack stack);

    ItemStack getActualStack(int slot);
}
