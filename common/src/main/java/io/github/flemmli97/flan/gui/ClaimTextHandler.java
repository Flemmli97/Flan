package io.github.flemmli97.flan.gui;

import io.github.flemmli97.flan.api.permission.ClaimPermission;
import io.github.flemmli97.flan.api.permission.PermissionRegistry;
import io.github.flemmli97.flan.claim.Claim;
import io.github.flemmli97.flan.claim.PermHelper;
import io.github.flemmli97.flan.config.ConfigHandler;
import io.github.flemmli97.flan.gui.inv.SeparateInv;
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

    private final Claim claim;

    private ClaimTextHandler(int syncId, Inventory playerInventory, Claim claim) {
        super(syncId, playerInventory, 1, claim);
        this.claim = claim;
    }

    public static void openClaimMenu(ServerPlayer player, Claim claim) {
        MenuProvider fac = new MenuProvider() {
            @Override
            public AbstractContainerMenu createMenu(int syncId, Inventory inv, Player player) {
                return new ClaimTextHandler(syncId, inv, claim);
            }

            @Override
            public Component getDisplayName() {
                return PermHelper.simpleColoredText(claim.parentClaim() != null ? ConfigHandler.langManager.get("screenTitleEditorSub") : ConfigHandler.langManager.get("screenTitleEditor"));
            }
        };
        player.openMenu(fac);
    }

    @Override
    protected void fillInventoryWith(Player player, SeparateInv inv, Claim claim) {
        for (int i = 0; i < 9; i++) {
            switch (i) {
                case 0 -> {
                    ItemStack close = new ItemStack(Items.TNT);
                    close.setHoverName(ServerScreenHelper.coloredGuiText(ConfigHandler.langManager.get("screenBack"), ChatFormatting.DARK_RED));
                    inv.updateStack(i, close);
                }
                case 2 -> {
                    ItemStack stack = new ItemStack(Items.OAK_SIGN);
                    stack.setHoverName(ServerScreenHelper.coloredGuiText(ConfigHandler.langManager.get("screenEnterText"), ChatFormatting.GOLD));
                    List<Component> lore = new ArrayList<>();
                    lore.add(ServerScreenHelper.coloredGuiText(ConfigHandler.langManager.get("screenTextJson"), ChatFormatting.GOLD));
                    lore.add(ServerScreenHelper.coloredGuiText(ConfigHandler.langManager.get("screenDelete"), ChatFormatting.DARK_RED));
                    if (claim.enterTitle != null)
                        lore.add(claim.enterTitle);
                    ServerScreenHelper.addLore(stack, lore);
                    inv.updateStack(i, stack);
                }
                case 3 -> {
                    ItemStack stack2 = new ItemStack(Items.OAK_SIGN);
                    stack2.setHoverName(ServerScreenHelper.coloredGuiText(ConfigHandler.langManager.get("screenEnterSubText"), ChatFormatting.GOLD));
                    List<Component> lore = new ArrayList<>();
                    lore.add(ServerScreenHelper.coloredGuiText(ConfigHandler.langManager.get("screenTextJson"), ChatFormatting.GOLD));
                    lore.add(ServerScreenHelper.coloredGuiText(ConfigHandler.langManager.get("screenDelete"), ChatFormatting.DARK_RED));
                    if (claim.enterSubtitle != null)
                        lore.add(claim.enterSubtitle);
                    ServerScreenHelper.addLore(stack2, lore);
                    inv.updateStack(i, stack2);
                }
                case 4 -> {
                    ItemStack stack3 = new ItemStack(Items.OAK_SIGN);
                    stack3.setHoverName(ServerScreenHelper.coloredGuiText(ConfigHandler.langManager.get("screenLeaveText"), ChatFormatting.GOLD));
                    List<Component> lore = new ArrayList<>();
                    lore.add(ServerScreenHelper.coloredGuiText(ConfigHandler.langManager.get("screenTextJson"), ChatFormatting.GOLD));
                    lore.add(ServerScreenHelper.coloredGuiText(ConfigHandler.langManager.get("screenDelete"), ChatFormatting.DARK_RED));
                    if (claim.leaveTitle != null)
                        lore.add(claim.leaveTitle);
                    ServerScreenHelper.addLore(stack3, lore);
                    inv.updateStack(i, stack3);
                }
                case 5 -> {
                    ItemStack stack4 = new ItemStack(Items.OAK_SIGN);
                    stack4.setHoverName(ServerScreenHelper.coloredGuiText(ConfigHandler.langManager.get("screenLeaveSubText"), ChatFormatting.GOLD));
                    List<Component> lore = new ArrayList<>();
                    lore.add(ServerScreenHelper.coloredGuiText(ConfigHandler.langManager.get("screenTextJson"), ChatFormatting.GOLD));
                    lore.add(ServerScreenHelper.coloredGuiText(ConfigHandler.langManager.get("screenDelete"), ChatFormatting.DARK_RED));
                    if (claim.leaveSubtitle != null)
                        lore.add(claim.leaveSubtitle);
                    ServerScreenHelper.addLore(stack4, lore);
                    inv.updateStack(i, stack4);
                }
                default -> inv.updateStack(i, ServerScreenHelper.emptyFiller());
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
            player.getServer().execute(() -> ClaimMenuScreenHandler.openClaimMenu(player, this.claim));
            ServerScreenHelper.playSongToPlayer(player, SoundEvents.UI_BUTTON_CLICK, 1, 1f);
        } else {
            Consumer<Component> cons = switch (index) {
                case 2 -> text -> this.claim.setEnterTitle(text, this.claim.enterSubtitle);
                case 3 -> text -> this.claim.setEnterTitle(this.claim.enterTitle, text);
                case 4 -> text -> this.claim.setLeaveTitle(text, this.claim.leaveSubtitle);
                case 5 -> text -> this.claim.setLeaveTitle(this.claim.leaveTitle, text);
                default -> null;
            };
            if (cons != null) {
                player.closeContainer();
                Consumer<Component> finalCons = cons;
                if (clickType == 0) {
                    player.getServer().execute(() -> StringResultScreenHandler.createNewStringResult(player, (s) -> {
                        player.closeContainer();
                        finalCons.accept(Component.literal(s).withStyle(Style.EMPTY.withItalic(false).applyFormat(ChatFormatting.WHITE)));
                        player.getServer().execute(() -> ClaimTextHandler.openClaimMenu(player, this.claim));
                        ServerScreenHelper.playSongToPlayer(player, SoundEvents.ANVIL_USE, 1, 1f);
                    }, () -> {
                        player.closeContainer();
                        player.getServer().execute(() -> ClaimTextHandler.openClaimMenu(player, this.claim));
                        ServerScreenHelper.playSongToPlayer(player, SoundEvents.VILLAGER_NO, 1, 1f);
                    }));
                } else {
                    MutableComponent text = Component.literal(ConfigHandler.langManager.get("chatClaimTextEdit"));
                    String command = "/flan claimMessage" + (index == 2 || index == 3 ? " enter" : " leave")
                            + (index == 2 || index == 4 ? " title" : " subtitle") + " text ";
                    text.withStyle(Style.EMPTY.withClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, command)));
                    player.displayClientMessage(text, false);
                }
                ServerScreenHelper.playSongToPlayer(player, SoundEvents.UI_BUTTON_CLICK, 1, 1f);
            }
        }
        return true;
    }

    private boolean hasEditPerm(Claim claim, ServerPlayer player) {
        return ((claim.parentClaim() != null && claim.parentClaim().canInteract(player, PermissionRegistry.EDITPERMS, player.blockPosition()))
                || claim.canInteract(player, PermissionRegistry.EDITPERMS, player.blockPosition()));
    }

    private boolean hasPerm(Claim claim, ServerPlayer player, ClaimPermission perm) {
        if (claim.parentClaim() != null)
            return claim.parentClaim().canInteract(player, perm, player.blockPosition());
        return claim.canInteract(player, perm, player.blockPosition());
    }
}
