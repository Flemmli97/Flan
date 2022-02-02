package io.github.flemmli97.flan.gui;

import io.github.flemmli97.flan.api.permission.ClaimPermission;
import io.github.flemmli97.flan.api.permission.PermissionRegistry;
import io.github.flemmli97.flan.claim.Claim;
import io.github.flemmli97.flan.claim.PermHelper;
import io.github.flemmli97.flan.config.ConfigHandler;
import io.github.flemmli97.flan.gui.inv.SeparateInv;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.function.Consumer;

public class ClaimTextHandler extends ServerOnlyScreenHandler<Claim> {

    private final Claim claim;

    private ClaimTextHandler(int syncId, PlayerInventory playerInventory, Claim claim) {
        super(syncId, playerInventory, 1, claim);
        this.claim = claim;
    }

    public static void openClaimMenu(ServerPlayerEntity player, Claim claim) {
        NamedScreenHandlerFactory fac = new NamedScreenHandlerFactory() {
            @Override
            public ScreenHandler createMenu(int syncId, PlayerInventory inv, PlayerEntity player) {
                return new ClaimTextHandler(syncId, inv, claim);
            }

            @Override
            public Text getDisplayName() {
                return PermHelper.simpleColoredText(claim.parentClaim() != null ? ConfigHandler.langManager.get("screenTitleEditorSub") : ConfigHandler.langManager.get("screenTitleEditor"));
            }
        };
        player.openHandledScreen(fac);
    }

    @Override
    protected void fillInventoryWith(PlayerEntity player, SeparateInv inv, Claim claim) {
        for (int i = 0; i < 9; i++) {
            switch (i) {
                case 0:
                    ItemStack close = new ItemStack(Items.TNT);
                    close.setCustomName(ServerScreenHelper.coloredGuiText(ConfigHandler.langManager.get("screenBack"), Formatting.DARK_RED));
                    inv.updateStack(i, close);
                    break;
                case 2:
                    ItemStack stack = new ItemStack(Items.OAK_SIGN);
                    stack.setCustomName(ServerScreenHelper.coloredGuiText(ConfigHandler.langManager.get("screenEnterText"), Formatting.GOLD));
                    if (claim.enterTitle != null)
                        ServerScreenHelper.addLore(stack, claim.enterTitle);
                    inv.updateStack(i, stack);
                    break;
                case 3:
                    ItemStack stack2 = new ItemStack(Items.OAK_SIGN);
                    stack2.setCustomName(ServerScreenHelper.coloredGuiText(ConfigHandler.langManager.get("screenEnterSubText"), Formatting.GOLD));
                    if (claim.enterSubtitle != null)
                        ServerScreenHelper.addLore(stack2, claim.enterSubtitle);
                    inv.updateStack(i, stack2);
                    break;
                case 4:
                    ItemStack stack3 = new ItemStack(Items.OAK_SIGN);
                    stack3.setCustomName(ServerScreenHelper.coloredGuiText(ConfigHandler.langManager.get("screenLeaveText"), Formatting.GOLD));
                    if (claim.leaveTitle != null)
                        ServerScreenHelper.addLore(stack3, claim.leaveTitle);
                    inv.updateStack(i, stack3);
                    break;
                case 5:
                    ItemStack stack4 = new ItemStack(Items.OAK_SIGN);
                    stack4.setCustomName(ServerScreenHelper.coloredGuiText(ConfigHandler.langManager.get("screenLeaveSubText"), Formatting.GOLD));
                    if (claim.leaveSubtitle != null)
                        ServerScreenHelper.addLore(stack4, claim.leaveSubtitle);
                    inv.updateStack(i, stack4);
                    break;
                default:
                    inv.updateStack(i, ServerScreenHelper.emptyFiller());
            }
        }
    }

    @Override
    protected boolean isRightSlot(int slot) {
        return slot == 0 || slot == 2 || slot == 3 || slot == 4 || slot == 5 || slot == 8;
    }

    @Override
    protected boolean handleSlotClicked(ServerPlayerEntity player, int index, Slot slot, int clickType) {
        if (index == 0) {
            player.closeHandledScreen();
            player.getServer().execute(() -> ClaimMenuScreenHandler.openClaimMenu(player, this.claim));
            ServerScreenHelper.playSongToPlayer(player, SoundEvents.UI_BUTTON_CLICK, 1, 1f);
        } else {
            Consumer<Text> cons = null;
            switch (index) {
                case 2:
                    cons = text -> this.claim.setEnterTitle(text, this.claim.enterSubtitle);
                    break;
                case 3:
                    cons = text -> this.claim.setEnterTitle(this.claim.enterTitle, text);
                    break;
                case 4:
                    cons = text -> this.claim.setLeaveTitle(text, this.claim.leaveSubtitle);
                    break;
                case 5:
                    cons = text -> this.claim.setLeaveTitle(this.claim.leaveTitle, text);
                    break;
            }
            if (cons != null) {
                player.closeHandledScreen();
                Consumer<Text> finalCons = cons;
                if (clickType == 0) {
                    player.getServer().execute(() -> StringResultScreenHandler.createNewStringResult(player, (s) -> {
                        player.closeHandledScreen();
                        finalCons.accept(new LiteralText(s).fillStyle(Style.EMPTY.withItalic(false).withFormatting(Formatting.WHITE)));
                        player.getServer().execute(() -> ClaimTextHandler.openClaimMenu(player, this.claim));
                        ServerScreenHelper.playSongToPlayer(player, SoundEvents.BLOCK_ANVIL_USE, 1, 1f);
                    }, () -> {
                        player.closeHandledScreen();
                        player.getServer().execute(() -> ClaimTextHandler.openClaimMenu(player, this.claim));
                        ServerScreenHelper.playSongToPlayer(player, SoundEvents.ENTITY_VILLAGER_NO, 1, 1f);
                    }));
                } else {
                    LiteralText text = new LiteralText(ConfigHandler.langManager.get("chatClaimTextEdit"));
                    String command = "/flan claimMessage" + (index == 2 || index == 3 ? " enter" : " leave")
                            + (index == 2 || index == 4 ? " title" : " subtitle") + " text ";
                    text.fillStyle(Style.EMPTY.withClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, command)));
                    player.sendMessage(text, false);
                }
                ServerScreenHelper.playSongToPlayer(player, SoundEvents.UI_BUTTON_CLICK, 1, 1f);
            }
        }
        return true;
    }

    private boolean hasEditPerm(Claim claim, ServerPlayerEntity player) {
        return ((claim.parentClaim() != null && claim.parentClaim().canInteract(player, PermissionRegistry.EDITPERMS, player.getBlockPos()))
                || claim.canInteract(player, PermissionRegistry.EDITPERMS, player.getBlockPos()));
    }

    private boolean hasPerm(Claim claim, ServerPlayerEntity player, ClaimPermission perm) {
        if (claim.parentClaim() != null)
            return claim.parentClaim().canInteract(player, perm, player.getBlockPos());
        return claim.canInteract(player, perm, player.getBlockPos());
    }
}
