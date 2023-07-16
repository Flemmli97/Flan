package io.github.flemmli97.flan.gui;

import io.github.flemmli97.flan.claim.PermHelper;
import io.github.flemmli97.flan.config.ConfigHandler;
import io.github.flemmli97.flan.mixin.AbstractContainerAccessor;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetExperiencePacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.AnvilMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.ContainerListener;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class StringResultScreenHandler extends AnvilMenu {

    private final List<ContainerListener> listeners = new ArrayList<>();

    private final Consumer<String> cons;
    private final Runnable ret;

    private boolean init;
    private String name;

    private StringResultScreenHandler(int syncId, Inventory playerInventory, Consumer<String> cons, Runnable ret) {
        super(syncId, playerInventory);
        ItemStack stack = new ItemStack(Items.PAPER);
        stack.setHoverName(PermHelper.simpleColoredText(""));
        this.inputSlots.setItem(0, stack);
        ItemStack out = new ItemStack(Items.BOOK);
        out.setHoverName(ServerScreenHelper.coloredGuiText(ConfigHandler.langManager.get("stringScreenReturn")));
        this.resultSlots.setItem(0, out);
        this.cons = cons;
        this.ret = ret;
    }

    public static void createNewStringResult(Player player, Consumer<String> cons, Runnable ret) {
        MenuProvider fac = new MenuProvider() {
            @Override
            public AbstractContainerMenu createMenu(int syncId, Inventory inv, Player player) {
                return new StringResultScreenHandler(syncId, inv, cons, ret);
            }

            @Override
            public Component getDisplayName() {
                return PermHelper.simpleColoredText("");
            }
        };
        player.openMenu(fac);
    }

    @Override
    public boolean stillValid(Player player) {
        return true;
    }

    @Override
    protected boolean mayPickup(Player player, boolean present) {
        return true;
    }

    @Override
    public void clicked(int i, int j, ClickType actionType, Player playerEntity) {
        if (i < 0 || !(playerEntity instanceof ServerPlayer player))
            return;
        Slot slot = this.slots.get(i);
        if (((AbstractContainerAccessor) this).containerSync() != null)
            ((AbstractContainerAccessor) this).containerSync().sendCarriedChange(this, this.getCarried().copy());
        if (i == 0)
            this.ret.run();
        else if (i == 2) {
            String s = slot.getItem().hasCustomHoverName() ? slot.getItem().getHoverName().getString() : "";
            if (!s.isEmpty() && !s.equals(ConfigHandler.langManager.get("stringScreenReturn"))) {
                this.cons.accept(s);
            }
            player.connection.send(new ClientboundSetExperiencePacket(player.experienceProgress, player.totalExperience, player.experienceLevel));
        }
        this.broadcastChanges();
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        if (!(player instanceof ServerPlayer))
            return ItemStack.EMPTY;
        if (index == 0)
            this.ret.run();
        else if (index == 2) {
            Slot slot = this.slots.get(index);
            String s = slot.getItem().hasCustomHoverName() ? slot.getItem().getHoverName().getString() : "";
            if (!s.isEmpty() && !s.equals(ConfigHandler.langManager.get("stringScreenReturn")))
                this.cons.accept(s);
            ((ServerPlayer) player).connection.send(new ClientboundSetExperiencePacket(player.experienceProgress, player.totalExperience, player.experienceLevel));
        }
        this.broadcastChanges();
        return ItemStack.EMPTY;
    }

    @Override
    public void broadcastChanges() {
        int j;
        for (j = 0; j < this.slots.size(); ++j) {
            ItemStack itemStack = this.slots.get(j).getItem();

            for (ContainerListener screenHandlerListener : this.listeners) {
                screenHandlerListener.slotChanged(this, j, itemStack.copy());
            }
        }
    }

    @Override
    public void createResult() {
        if (!this.init)
            this.init = true;
        else {
            ItemStack out = this.slots.get(2).getItem();
            if (StringUtils.isBlank(this.name))
                out.resetHoverName();
            else if (!this.name.equals(out.getHoverName().getString())) {
                out.setHoverName(ServerScreenHelper.coloredGuiText(this.name));
            }
        }
        this.broadcastChanges();
    }

    @Override
    public boolean setItemName(String string) {
        this.name = string;
        this.createResult();
        return true;
    }
}
