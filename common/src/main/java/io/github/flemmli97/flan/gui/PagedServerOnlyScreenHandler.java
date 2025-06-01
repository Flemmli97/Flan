package io.github.flemmli97.flan.gui;

import io.github.flemmli97.flan.mixin.AbstractContainerAccessor;
import net.minecraft.ChatFormatting;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.ContainerListener;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public abstract class PagedServerOnlyScreenHandler<T> extends ServerOnlyScreenHandler<T> {

    private int page;

    protected PagedServerOnlyScreenHandler(int syncId, Inventory playerInventory, int rows, T additionalData) {
        super(syncId, playerInventory, rows, additionalData);
    }

    @Override
    protected final void setupGui() {
        super.setupGui();
        PageSettings settings = this.pageSettings();
        if (this.page >= 1 && settings.previousSlot() != -1) {
            ItemStack stack = ServerScreenHelper.createStack(Items.ARROW,
                    ServerScreenHelper.coloredGuiText("flan.screenPrevious", ChatFormatting.WHITE));
            this.slots.get(settings.previousSlot()).set(stack);
        }
        if (this.page < settings.maxPages() && settings.nextSlot() != -1) {
            ItemStack stack = ServerScreenHelper.createStack(Items.ARROW,
                    ServerScreenHelper.coloredGuiText("flan.screenNext", ChatFormatting.WHITE));
            this.slots.get(settings.nextSlot()).set(stack);
        }
    }

    @Override
    public void clicked(int i, int j, ClickType clickType, Player player) {
        if (this.handlePageFlip(i)) {
            for (ContainerListener listener : ((AbstractContainerAccessor) this).listeners())
                listener.slotChanged(this, i, this.slots.get(i).getItem().copy());
            return;
        }
        super.clicked(i, j, clickType, player);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        if (index < 0)
            return ItemStack.EMPTY;
        if (this.handlePageFlip(index))
            return this.slots.get(index).getItem().copy();
        return super.quickMoveStack(player, index);
    }

    private boolean handlePageFlip(int slot) {
        PageSettings settings = this.pageSettings();
        boolean previous = this.page > 0 && settings.previousSlot() != -1 && slot == settings.previousSlot();
        boolean next = this.page < settings.maxPages() && settings.nextSlot() != -1 && slot == settings.nextSlot();
        if (previous || next) {
            if (previous)
                this.flipPrevious();
            else {
                this.flipNext();
            }
            return true;
        }
        return false;
    }

    protected abstract PageSettings pageSettings();

    public int getPage() {
        return this.page;
    }

    public void flipNext() {
        this.page = Math.min(this.pageSettings().maxPages(), ++this.page);
        ServerScreenHelper.playSongToPlayer(this.player, SoundEvents.UI_BUTTON_CLICK, 1, 1f);

        this.setupGui();
        this.broadcastChanges();
    }

    public void flipPrevious() {
        this.page = Math.max(0, --this.page);
        ServerScreenHelper.playSongToPlayer(this.player, SoundEvents.UI_BUTTON_CLICK, 1, 1f);
        this.setupGui();
        this.broadcastChanges();
    }

    public record PageSettings(int maxPages, int previousSlot, int nextSlot) {
    }
}
