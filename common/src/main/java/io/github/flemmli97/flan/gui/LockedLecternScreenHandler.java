package io.github.flemmli97.flan.gui;

import io.github.flemmli97.flan.claim.PermHelper;
import io.github.flemmli97.flan.config.ConfigHandler;
import io.github.flemmli97.flan.mixin.ILecternBlockValues;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.LecternMenu;
import net.minecraft.world.level.block.entity.LecternBlockEntity;

public class LockedLecternScreenHandler extends LecternMenu {

    public LockedLecternScreenHandler(int syncId, Container inventory, ContainerData propertyDelegate) {
        super(syncId, inventory, propertyDelegate);
    }

    public static void create(ServerPlayer player, LecternBlockEntity lectern) {
        MenuProvider fac = new MenuProvider() {
            @Override
            public AbstractContainerMenu createMenu(int syncId, Inventory inv, Player player) {
                return new LockedLecternScreenHandler(syncId, ((ILecternBlockValues) lectern).getInv(), ((ILecternBlockValues) lectern).getProp());
            }

            @Override
            public Component getDisplayName() {
                return lectern.getDisplayName();
            }
        };
        player.openMenu(fac);
    }

    @Override
    public boolean clickMenuButton(Player player, int id) {
        if (id == 3) {
            player.displayClientMessage(PermHelper.simpleColoredText(ConfigHandler.langManager.get("noPermissionSimple"), ChatFormatting.DARK_RED), false);
            return false;
        }
        return super.clickMenuButton(player, id);
    }
}
