package io.github.flemmli97.flan.gui;

import io.github.flemmli97.flan.claim.Claim;
import io.github.flemmli97.flan.claim.ClaimUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomData;

import java.util.List;
import java.util.UUID;

public class FakePlayerScreenHandler extends PagedServerOnlyScreenHandler<Claim> {

    private boolean removeMode;

    private FakePlayerScreenHandler(int syncId, Inventory playerInventory, Claim claim) {
        super(syncId, playerInventory, 6, claim);
    }

    public static void open(Player player, Claim claim) {
        MenuProvider fac = new MenuProvider() {
            @Override
            public AbstractContainerMenu createMenu(int syncId, Inventory inv, Player player) {
                return new FakePlayerScreenHandler(syncId, inv, claim);
            }

            @Override
            public Component getDisplayName() {
                return ClaimUtils.translatedText("flan.screenMenuFakePlayers");
            }
        };
        player.openMenu(fac);
    }

    @Override
    protected void fillInventoryWith() {
        List<String> players = this.data.getAllowedFakePlayerUUID();
        for (int i = 0; i < 54; i++) {
            if (i == 0) {
                ItemStack stack = ServerScreenHelper.createStack(Items.TNT,
                        ServerScreenHelper.coloredGuiText("flan.screenBack", ChatFormatting.DARK_RED));
                this.slots.get(i).set(stack);
            } else if (i == 3) {
                ItemStack stack = ServerScreenHelper.createStack(Items.ANVIL,
                        ServerScreenHelper.coloredGuiText("flan.screenAdd", ChatFormatting.DARK_GREEN));
                this.slots.get(i).set(stack);
            } else if (i == 4) {
                ItemStack stack = ServerScreenHelper.createStack(Items.REDSTONE_BLOCK,
                        ServerScreenHelper.coloredGuiText("flan.screenRemoveMode", this.removeMode, ChatFormatting.DARK_RED));
                this.slots.get(i).set(stack);
            } else if (i < 9 || i > 44 || i % 9 == 0 || i % 9 == 8)
                this.slots.get(i).set(ServerScreenHelper.emptyFiller());
            else {
                int row = i / 9 - 1;
                int id = (i % 9) + row * 7 - 1 + this.getPage() * 28;
                if (id < players.size()) {
                    ItemStack stack = ServerScreenHelper.createStack(Items.ZOMBIE_HEAD, null);
                    CustomData.update(DataComponents.CUSTOM_DATA, stack, t -> t.putString("FlanFakePlayer", players.get(id)));
                    stack.set(DataComponents.CUSTOM_NAME, ServerScreenHelper.coloredGuiText("flan.screenFakePlayerNameUUID", players.get(id), ChatFormatting.YELLOW));
                    this.slots.get(i).set(stack);
                } else
                    this.slots.get(i).set(ItemStack.EMPTY);
            }
        }
    }

    @Override
    protected boolean isRightSlot(int slot) {
        return slot == 0 || slot == 3 || slot == 4 || (slot < 45 && slot > 8 && slot % 9 != 0 && slot % 9 != 8);
    }

    @Override
    protected boolean handleSlotClicked(ServerPlayer player, int index, Slot slot, int clickType) {
        if (index == 0) {
            player.closeContainer();
            player.getServer().execute(() -> ClaimMenuScreenHandler.openClaimMenu(player, this.data));
            ServerScreenHelper.playSongToPlayer(player, SoundEvents.UI_BUTTON_CLICK, 1, 1f);
            return true;
        }
        if (index == 3) {
            player.closeContainer();
            player.getServer().execute(() -> StringResultScreenHandler.createNewStringResult(player, (s) -> {
                boolean fl = player.getServer().getProfileCache().get(s).map(prof -> this.data.modifyFakePlayerUUID(prof.getId(), false)).orElse(true);
                player.closeContainer();
                player.getServer().execute(() -> FakePlayerScreenHandler.open(player, this.data));
                if (fl)
                    ServerScreenHelper.playSongToPlayer(player, SoundEvents.ANVIL_USE, 1, 1f);
                else {
                    player.displayClientMessage(ClaimUtils.translatedText("flan.playerGroupAddFail", ChatFormatting.RED), false);
                    ServerScreenHelper.playSongToPlayer(player, SoundEvents.VILLAGER_NO, 1, 1f);
                }
            }, () -> {
                player.closeContainer();
                player.getServer().execute(() -> FakePlayerScreenHandler.open(player, this.data));
                ServerScreenHelper.playSongToPlayer(player, SoundEvents.VILLAGER_NO, 1, 1f);
            }));
            ServerScreenHelper.playSongToPlayer(player, SoundEvents.UI_BUTTON_CLICK, 1, 1f);
            return true;
        }
        if (index == 4) {
            this.removeMode = !this.removeMode;
            ItemStack stack = ServerScreenHelper.createStack(Items.REDSTONE_BLOCK,
                    ServerScreenHelper.coloredGuiText("flan.screenRemoveMode", this.removeMode, ChatFormatting.DARK_RED));
            slot.set(stack);
            ServerScreenHelper.playSongToPlayer(player, SoundEvents.UI_BUTTON_CLICK, 1, 1f);
            return true;
        }
        ItemStack stack = slot.getItem();
        if (!stack.isEmpty()) {
            UUID uuid = null;
            try {
                uuid = UUID.fromString(stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY)
                        .copyTag().getString("FlanFakePlayer"));
            } catch (IllegalArgumentException ignored) {
            }
            if (this.removeMode && uuid != null) {
                this.data.modifyFakePlayerUUID(uuid, true);
                slot.set(ItemStack.EMPTY);
                ServerScreenHelper.playSongToPlayer(player, SoundEvents.BAT_DEATH, 1, 1f);
            }
        }
        return false;
    }

    @Override
    protected PagedServerOnlyScreenHandler.PageSettings pageSettings() {
        return new PagedServerOnlyScreenHandler.PageSettings((this.data.getAllowedFakePlayerUUID().size() - 1) / 28, 47, 51);
    }
}
