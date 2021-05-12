package com.flemmli97.flan.gui;

import com.flemmli97.flan.claim.Claim;
import com.flemmli97.flan.claim.PermHelper;
import com.flemmli97.flan.config.ConfigHandler;
import com.mojang.authlib.GameProfile;
import net.minecraft.block.entity.SkullBlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.List;

public class GroupPlayerScreenHandler extends ServerOnlyScreenHandler {

    private final Claim claim;
    private final String group;
    private boolean removeMode;

    private GroupPlayerScreenHandler(int syncId, PlayerInventory playerInventory, Claim claim, String group) {
        super(syncId, playerInventory, 6, claim, group);
        this.claim = claim;
        this.group = group;
    }

    public static void openPlayerGroupMenu(PlayerEntity player, Claim claim, String group) {
        NamedScreenHandlerFactory fac = new NamedScreenHandlerFactory() {
            @Override
            public ScreenHandler createMenu(int syncId, PlayerInventory inv, PlayerEntity player) {
                return new GroupPlayerScreenHandler(syncId, inv, claim, group);
            }

            @Override
            public Text getDisplayName() {
                return PermHelper.simpleColoredText(String.format(ConfigHandler.lang.screenGroupPlayers, group));
            }
        };
        player.openHandledScreen(fac);
    }

    @Override
    protected void fillInventoryWith(PlayerEntity player, Inventory inv, Object... additionalData) {
        if (additionalData == null || additionalData.length < 2)
            return;
        Claim claim = (Claim) additionalData[0];
        List<String> players = claim.playersFromGroup(player.getServer(), (String) additionalData[1]);
        for (int i = 0; i < 54; i++) {
            if (i == 0) {
                ItemStack close = new ItemStack(Items.TNT);
                close.setCustomName(ServerScreenHelper.coloredGuiText(ConfigHandler.lang.screenBack, Formatting.DARK_RED));
                inv.setStack(i, close);
            } else if (i == 3) {
                ItemStack stack = new ItemStack(Items.ANVIL);
                stack.setCustomName(ServerScreenHelper.coloredGuiText(ConfigHandler.lang.screenAdd, Formatting.DARK_GREEN));
                inv.setStack(i, stack);
            } else if (i == 4) {
                ItemStack stack = new ItemStack(Items.REDSTONE_BLOCK);
                stack.setCustomName(ServerScreenHelper.coloredGuiText(String.format(ConfigHandler.lang.screenRemoveMode, this.removeMode), Formatting.DARK_RED));
                inv.setStack(i, stack);
            } else if (i < 9 || i > 44 || i % 9 == 0 || i % 9 == 8)
                inv.setStack(i, ServerScreenHelper.emptyFiller());
            else {
                int row = i / 9 - 1;
                int id = (i % 9) + row * 7 - 1;
                if (id < players.size()) {
                    ItemStack group = new ItemStack(Items.PLAYER_HEAD);
                    GameProfile gameProfile = new GameProfile(null, players.get(id));
                    gameProfile = SkullBlockEntity.loadProperties(gameProfile);
                    group.getOrCreateTag().put("SkullOwner", NbtHelper.writeGameProfile(new NbtCompound(), gameProfile));
                    inv.setStack(i, group);
                }
            }
        }
    }

    @Override
    protected boolean isRightSlot(int slot) {
        return slot == 0 || slot == 3 || slot == 4 || (slot < 45 && slot > 8 && slot % 9 != 0 && slot % 9 != 8);
    }

    @Override
    protected boolean handleSlotClicked(ServerPlayerEntity player, int index, Slot slot, int clickType) {
        if (index == 0) {
            player.closeHandledScreen();
            player.getServer().execute(() -> GroupScreenHandler.openGroupMenu(player, this.claim));
            ServerScreenHelper.playSongToPlayer(player, SoundEvents.UI_BUTTON_CLICK, 1, 1f);
            return true;
        }
        if (index == 3) {
            player.closeHandledScreen();
            player.getServer().execute(() -> StringResultScreenHandler.createNewStringResult(player, (s) -> {
                GameProfile prof = player.getServer().getUserCache().findByName(s);
                boolean fl = prof == null || this.claim.setPlayerGroup(prof.getId(), this.group, false);
                player.closeHandledScreen();
                player.getServer().execute(() -> GroupPlayerScreenHandler.openPlayerGroupMenu(player, this.claim, this.group));
                if (fl)
                    ServerScreenHelper.playSongToPlayer(player, SoundEvents.BLOCK_ANVIL_USE, 1, 1f);
                else {
                    player.sendMessage(PermHelper.simpleColoredText(ConfigHandler.lang.playerGroupAddFail, Formatting.RED), false);
                    ServerScreenHelper.playSongToPlayer(player, SoundEvents.ENTITY_VILLAGER_NO, 1, 1f);
                }
            }, () -> {
                player.closeHandledScreen();
                player.getServer().execute(() -> GroupPlayerScreenHandler.openPlayerGroupMenu(player, this.claim, this.group));
                ServerScreenHelper.playSongToPlayer(player, SoundEvents.ENTITY_VILLAGER_NO, 1, 1f);
            }));
            ServerScreenHelper.playSongToPlayer(player, SoundEvents.UI_BUTTON_CLICK, 1, 1f);
            return true;
        }
        if (index == 4) {
            this.removeMode = !this.removeMode;
            ItemStack stack = new ItemStack(Items.REDSTONE_BLOCK);
            stack.setCustomName(ServerScreenHelper.coloredGuiText(String.format(ConfigHandler.lang.screenRemoveMode, this.removeMode), Formatting.DARK_RED));
            slot.setStack(stack);
            ServerScreenHelper.playSongToPlayer(player, SoundEvents.UI_BUTTON_CLICK, 1, 1f);
            return true;
        }
        ItemStack stack = slot.getStack();
        if (!stack.isEmpty()) {
            NbtCompound tag = stack.getOrCreateSubTag("SkullOwner");
            if (this.removeMode && tag.contains("Id")) {
                this.claim.setPlayerGroup(tag.getUuid("Id"), null, false);
                slot.setStack(ItemStack.EMPTY);
                ServerScreenHelper.playSongToPlayer(player, SoundEvents.ENTITY_BAT_DEATH, 1, 1f);
            }
        }
        return false;
    }
}
