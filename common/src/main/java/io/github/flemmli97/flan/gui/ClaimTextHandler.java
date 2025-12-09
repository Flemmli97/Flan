package io.github.flemmli97.flan.gui;

import io.github.flemmli97.flan.claim.Claim;
import io.github.flemmli97.flan.claim.ClaimUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class ClaimTextHandler extends ServerOnlyScreenHandler<Claim> {

    private ClaimTextHandler(int syncId, Inventory playerInventory, Claim claim) {
        super(syncId, playerInventory, 1, claim);
    }

    public static void openClaimMenu(ServerPlayer player, Claim claim) {
        MenuProvider fac = new MenuProvider() {
            @Override
            public AbstractContainerMenu createMenu(int syncId, Inventory inv, Player player) {
                return new ClaimTextHandler(syncId, inv, claim);
            }

            @Override
            public Component getDisplayName() {
                return ClaimUtils.translatedText(claim.parentClaim() != null ? "flan.screenTitleEditorSub" : "flan.screenTitleEditor");
            }
        };
        player.openMenu(fac);
    }

    @Override
    protected void fillInventoryWith() {
        for (int i = 0; i < 9; i++) {
            switch (i) {
                case 0 -> {
                    ItemStack stack = ServerScreenHelper.createStack(Items.TNT,
                            ServerScreenHelper.coloredGuiText("flan.screenBack", ChatFormatting.DARK_RED));
                    this.slots.get(i).set(stack);
                }
                case 2 -> {
                    ItemStack stack = ServerScreenHelper.createStack(Items.OAK_SIGN,
                            ServerScreenHelper.coloredGuiText("flan.screenEnterText", ChatFormatting.GOLD));
                    List<Component> lore = new ArrayList<>();
                    lore.add(ServerScreenHelper.coloredGuiText("flan.screenTextJson", ChatFormatting.GOLD));
                    lore.add(ServerScreenHelper.coloredGuiText("flan.screenDelete", ChatFormatting.DARK_RED));
                    if (this.data.enterTitle != null)
                        lore.add(this.data.enterTitle);
                    ServerScreenHelper.addLore(stack, lore);
                    this.slots.get(i).set(stack);
                }
                case 3 -> {
                    ItemStack stack = ServerScreenHelper.createStack(Items.OAK_SIGN,
                            ServerScreenHelper.coloredGuiText("flan.screenEnterSubText", ChatFormatting.GOLD));
                    List<Component> lore = new ArrayList<>();
                    lore.add(ServerScreenHelper.coloredGuiText("flan.screenTextJson", ChatFormatting.GOLD));
                    lore.add(ServerScreenHelper.coloredGuiText("flan.screenDelete", ChatFormatting.DARK_RED));
                    if (this.data.enterSubtitle != null)
                        lore.add(this.data.enterSubtitle);
                    ServerScreenHelper.addLore(stack, lore);
                    this.slots.get(i).set(stack);
                }
                case 4 -> {
                    ItemStack stack = ServerScreenHelper.createStack(Items.OAK_SIGN,
                            ServerScreenHelper.coloredGuiText("flan.screenLeaveText", ChatFormatting.GOLD));
                    List<Component> lore = new ArrayList<>();
                    lore.add(ServerScreenHelper.coloredGuiText("flan.screenTextJson", ChatFormatting.GOLD));
                    lore.add(ServerScreenHelper.coloredGuiText("flan.screenDelete", ChatFormatting.DARK_RED));
                    if (this.data.leaveTitle != null)
                        lore.add(this.data.leaveTitle);
                    ServerScreenHelper.addLore(stack, lore);
                    this.slots.get(i).set(stack);
                }
                case 5 -> {
                    ItemStack stack = ServerScreenHelper.createStack(Items.OAK_SIGN,
                            ServerScreenHelper.coloredGuiText("flan.screenLeaveSubText", ChatFormatting.GOLD));
                    List<Component> lore = new ArrayList<>();
                    lore.add(ServerScreenHelper.coloredGuiText("flan.screenTextJson", ChatFormatting.GOLD));
                    lore.add(ServerScreenHelper.coloredGuiText("flan.screenDelete", ChatFormatting.DARK_RED));
                    if (this.data.leaveSubtitle != null)
                        lore.add(this.data.leaveSubtitle);
                    ServerScreenHelper.addLore(stack, lore);
                    this.slots.get(i).set(stack);
                }
                default -> this.slots.get(i).set(ServerScreenHelper.emptyFiller());
            }
        }
    }

    @Override
    protected boolean isRightSlot(int slot) {
        return slot == 0 || slot == 2 || slot == 3 || slot == 4 || slot == 5 || slot == 8;
    }

    @Override
    protected boolean handleSlotClicked(ServerPlayer player, int index, Slot slot, int clickType) {
        if (index == 0) {
            player.closeContainer();
            player.level().getServer().execute(() -> ClaimMenuScreenHandler.openClaimMenu(player, this.data));
            ServerScreenHelper.playSongToPlayer(player, SoundEvents.UI_BUTTON_CLICK, 1, 1f);
        } else {
            Consumer<Component> cons = switch (index) {
                case 2 -> text -> this.data.setEnterTitle(text, this.data.enterSubtitle);
                case 3 -> text -> this.data.setEnterTitle(this.data.enterTitle, text);
                case 4 -> text -> this.data.setLeaveTitle(text, this.data.leaveSubtitle);
                case 5 -> text -> this.data.setLeaveTitle(this.data.leaveTitle, text);
                default -> null;
            };
            if (cons != null) {
                player.closeContainer();
                if (clickType == 0) {
                    player.level().getServer().execute(() -> StringResultScreenHandler.createNewStringResult(player, (s) -> {
                        player.closeContainer();
                        cons.accept(Component.literal(s).withStyle(Style.EMPTY.withItalic(false).applyFormat(ChatFormatting.WHITE)));
                        player.level().getServer().execute(() -> ClaimTextHandler.openClaimMenu(player, this.data));
                        ServerScreenHelper.playSongToPlayer(player, SoundEvents.ANVIL_USE, 1, 1f);
                    }, () -> {
                        player.closeContainer();
                        player.level().getServer().execute(() -> ClaimTextHandler.openClaimMenu(player, this.data));
                        ServerScreenHelper.playSongToPlayer(player, SoundEvents.VILLAGER_NO, 1, 1f);
                    }));
                } else {
                    MutableComponent text = ClaimUtils.translatedText("flan.chatClaimTextEdit");
                    String command = "/flan claimMessage" + (index == 2 || index == 3 ? " enter" : " leave")
                            + (index == 2 || index == 4 ? " title" : " subtitle") + " text ";
                    text.withStyle(Style.EMPTY.withClickEvent(new ClickEvent.SuggestCommand(command)));
                    player.displayClientMessage(text, false);
                }
                ServerScreenHelper.playSongToPlayer(player, SoundEvents.UI_BUTTON_CLICK, 1, 1f);
            }
        }
        return true;
    }
}
