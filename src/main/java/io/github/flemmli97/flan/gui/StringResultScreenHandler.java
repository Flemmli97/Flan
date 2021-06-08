package io.github.flemmli97.flan.gui;

import io.github.flemmli97.flan.claim.PermHelper;
import io.github.flemmli97.flan.config.ConfigHandler;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.AnvilScreenHandler;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerListener;
import net.minecraft.screen.ScreenHandlerSyncHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.text.Text;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class StringResultScreenHandler extends AnvilScreenHandler {

    private final List<ScreenHandlerListener> listeners = new ArrayList<>();

    private final Consumer<String> cons;
    private final Runnable ret;

    private boolean init;
    private String name;
    private ScreenHandlerSyncHandler syncHandler;

    private StringResultScreenHandler(int syncId, PlayerInventory playerInventory, Consumer<String> cons, Runnable ret) {
        super(syncId, playerInventory);
        ItemStack stack = new ItemStack(Items.PAPER);
        stack.setCustomName(PermHelper.simpleColoredText(""));
        this.input.setStack(0, stack);
        ItemStack out = new ItemStack(Items.BOOK);
        out.setCustomName(ServerScreenHelper.coloredGuiText(ConfigHandler.lang.stringScreenReturn));
        this.output.setStack(0, out);
        this.cons = cons;
        this.ret = ret;

    }

    public static void createNewStringResult(PlayerEntity player, Consumer<String> cons, Runnable ret) {
        NamedScreenHandlerFactory fac = new NamedScreenHandlerFactory() {
            @Override
            public ScreenHandler createMenu(int syncId, PlayerInventory inv, PlayerEntity player) {
                return new StringResultScreenHandler(syncId, inv, cons, ret);
            }

            @Override
            public Text getDisplayName() {
                return PermHelper.simpleColoredText("");
            }
        };
        player.openHandledScreen(fac);
    }

    @Override
    public boolean canUse(PlayerEntity player) {
        return true;
    }

    @Override
    protected boolean canTakeOutput(PlayerEntity player, boolean present) {
        return true;
    }

    @Override
    public void onSlotClick(int i, int j, SlotActionType actionType, PlayerEntity playerEntity) {
        if (i < 0)
            return;
        Slot slot = this.slots.get(i);
        if (this.syncHandler != null)
            this.syncHandler.updateCursorStack(this, this.getCursorStack().copy());
        if (i == 0)
            this.ret.run();
        else if (i == 2) {
            String s = slot.getStack().hasCustomName() ? slot.getStack().getName().asString() : "";
            if (!s.isEmpty() && !s.equals(ConfigHandler.lang.stringScreenReturn))
                this.cons.accept(s);
        }
        this.sendContentUpdates();
    }

    @Override
    public ItemStack transferSlot(PlayerEntity player, int index) {
        if (index == 0)
            this.ret.run();
        else if (index == 2) {
            Slot slot = this.slots.get(index);
            String s = slot.getStack().hasCustomName() ? slot.getStack().getName().asString() : "";
            if (!s.isEmpty() && !s.equals(ConfigHandler.lang.stringScreenReturn))
                this.cons.accept(s);
        }
        this.sendContentUpdates();
        return ItemStack.EMPTY;
    }

    @Override
    public void updateSyncHandler(ScreenHandlerSyncHandler handler) {
        super.updateSyncHandler(handler);
        this.syncHandler = handler;
    }

    @Override
    public void sendContentUpdates() {
        int j;
        for (j = 0; j < this.slots.size(); ++j) {
            ItemStack itemStack = this.slots.get(j).getStack();

            for (ScreenHandlerListener screenHandlerListener : this.listeners) {
                screenHandlerListener.onSlotUpdate(this, j, itemStack.copy());
            }
        }
    }

    @Override
    public void updateResult() {
        if (!this.init)
            this.init = true;
        else {
            ItemStack out = this.slots.get(2).getStack();
            if (StringUtils.isBlank(this.name))
                out.removeCustomName();
            else if (!this.name.equals(out.getName().getString())) {
                out.setCustomName(ServerScreenHelper.coloredGuiText(this.name));
            }
        }
        this.sendContentUpdates();
    }

    @Override
    public void setNewItemName(String string) {
        this.name = string;
        this.updateResult();
    }
}
