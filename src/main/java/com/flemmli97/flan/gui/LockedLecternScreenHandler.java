package com.flemmli97.flan.gui;

import com.flemmli97.flan.claim.Claim;
import com.flemmli97.flan.claim.PermHelper;
import com.flemmli97.flan.config.ConfigHandler;
import com.flemmli97.flan.mixin.ILecternBlockValues;
import net.minecraft.block.LecternBlock;
import net.minecraft.block.entity.LecternBlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.screen.LecternScreenHandler;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.PropertyDelegate;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class LockedLecternScreenHandler extends LecternScreenHandler {

    public LockedLecternScreenHandler(int syncId, Inventory inventory, PropertyDelegate propertyDelegate) {
        super(syncId, inventory, propertyDelegate);
    }

    public static void create(ServerPlayerEntity player, LecternBlockEntity lectern) {
        NamedScreenHandlerFactory fac = new NamedScreenHandlerFactory() {
            @Override
            public ScreenHandler createMenu(int syncId, PlayerInventory inv, PlayerEntity player) {
                return new LockedLecternScreenHandler(syncId, ((ILecternBlockValues)lectern).getInv(), ((ILecternBlockValues)lectern).getProp());
            }

            @Override
            public Text getDisplayName() {
                return lectern.getDisplayName();
            }
        };
        player.openHandledScreen(fac);
    }

    @Override
    public boolean onButtonClick(PlayerEntity player, int id) {
        if(id==3) {
            player.sendMessage(PermHelper.simpleColoredText(ConfigHandler.lang.noPermissionSimple, Formatting.DARK_RED), false);
            return false;
        }
        return super.onButtonClick(player, id);
    }
}
