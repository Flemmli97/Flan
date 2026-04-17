package io.github.flemmli97.flan.gui;

import io.github.flemmli97.flan.api.permission.BuiltinPermission;
import io.github.flemmli97.flan.claim.Claim;
import io.github.flemmli97.flan.claim.ClaimStorage;
import io.github.flemmli97.flan.claim.ClaimUtils;
import io.github.flemmli97.flan.player.PlayerClaimData;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class ClaimMenuScreenHandler extends ServerOnlyScreenHandler<Claim> {

    private ClaimMenuScreenHandler(int syncId, Inventory playerInventory, Claim claim) {
        super(syncId, playerInventory, 2, claim);
    }

    public static void openClaimMenu(ServerPlayer player, Claim claim) {
        MenuProvider fac = new MenuProvider() {
            @Override
            public AbstractContainerMenu createMenu(int syncId, Inventory inv, Player player) {
                return new ClaimMenuScreenHandler(syncId, inv, claim);
            }

            @Override
            public Component getDisplayName() {
                return ClaimUtils.translatedText(claim.parentClaim() != null ? "flan.screenMenuSub" : "flan.screenMenu");
            }
        };
        player.openMenu(fac);
    }

    @Override
    protected void fillInventoryWith() {
        for (int i = 0; i < 18; i++) {
            switch (i) {
                case 0 -> {
                    ItemStack stack = ServerScreenHelper.createStack(Items.TNT, ServerScreenHelper.coloredGuiText("flan.screenClose", ChatFormatting.DARK_RED));
                    this.slots.get(i).set(stack);
                }
                case 2 -> {
                    ItemStack stack = ServerScreenHelper.createStack(Items.BEACON,
                            ServerScreenHelper.coloredGuiText("flan.screenMenuGlobal", ChatFormatting.GOLD));
                    if (!this.hasEditPerm(this.data, this.player))
                        ServerScreenHelper.addLore(stack, ServerScreenHelper.coloredGuiText("flan.screenNoPerm", ChatFormatting.DARK_RED));
                    this.slots.get(i).set(stack);
                }
                case 3 -> {
                    ItemStack stack = ServerScreenHelper.createStack(Items.WRITABLE_BOOK,
                            ServerScreenHelper.coloredGuiText("flan.screenMenuGroup", ChatFormatting.GOLD));
                    if (!this.hasEditPerm(this.data, this.player))
                        ServerScreenHelper.addLore(stack, ServerScreenHelper.coloredGuiText("flan.screenNoPerm", ChatFormatting.DARK_RED));
                    this.slots.get(i).set(stack);
                }
                case 4 -> {
                    ItemStack stack = ServerScreenHelper.createStack(Items.POTION,
                            ServerScreenHelper.coloredGuiText("flan.screenMenuPotion", ChatFormatting.GOLD));
                    if (!this.hasPerm(this.data, this.player, BuiltinPermission.EDITPOTIONS))
                        ServerScreenHelper.addLore(stack, ServerScreenHelper.coloredGuiText("flan.screenNoPerm", ChatFormatting.DARK_RED));
                    this.slots.get(i).set(stack);
                }
                case 5 -> {
                    ItemStack stack = ServerScreenHelper.createStack(Items.OAK_SIGN,
                            ServerScreenHelper.coloredGuiText("flan.screenMenuClaimText", ChatFormatting.GOLD));
                    if (!this.hasPerm(this.data, this.player, BuiltinPermission.CLAIMMESSAGE))
                        ServerScreenHelper.addLore(stack, ServerScreenHelper.coloredGuiText("flan.screenNoPerm", ChatFormatting.DARK_RED));
                    this.slots.get(i).set(stack);
                }
                case 6 -> {
                    ItemStack stack = ServerScreenHelper.createStack(Items.ZOMBIE_HEAD,
                            ServerScreenHelper.coloredGuiText("flan.screenMenuFakePlayers", ChatFormatting.GOLD));
                    if (!this.hasPerm(this.data, this.player, BuiltinPermission.EDITPERMS))
                        ServerScreenHelper.addLore(stack, ServerScreenHelper.coloredGuiText("flan.screenNoPerm", ChatFormatting.DARK_RED));
                    this.slots.get(i).set(stack);
                }
                case 8 -> {
                    ItemStack stack = ServerScreenHelper.createStack(Items.BARRIER,
                            ServerScreenHelper.coloredGuiText("flan.screenMenuDelete", ChatFormatting.RED));
                    if (!this.hasPerm(this.data, this.player, BuiltinPermission.EDITCLAIM))
                        ServerScreenHelper.addLore(stack, ServerScreenHelper.coloredGuiText("flan.screenNoPerm", ChatFormatting.DARK_RED));
                    this.slots.get(i).set(stack);
                }
                case 11 -> {
                    ItemStack stack = ServerScreenHelper.createStack(Items.BOOKSHELF,
                            ServerScreenHelper.coloredGuiText("flan.screenAllowList", ChatFormatting.GOLD));
                    if (!this.hasPerm(this.data, this.player, BuiltinPermission.EDITCLAIM))
                        ServerScreenHelper.addLore(stack, ServerScreenHelper.coloredGuiText("flan.screenNoPerm", ChatFormatting.DARK_RED));
                    this.slots.get(i).set(stack);
                }
                default -> this.slots.get(i).set(ServerScreenHelper.emptyFiller());
            }
        }
    }

    @Override
    protected boolean isRightSlot(int slot) {
        return slot == 0 || slot == 2 || slot == 3 || slot == 4 || slot == 5 || slot == 6 || slot == 8 || (slot >= 11 && slot <= 15);
    }

    @Override
    protected boolean handleSlotClicked(ServerPlayer player, int index, Slot slot, int clickType) {
        switch (index) {
            case 0:
                player.closeContainer();
                ServerScreenHelper.playSongToPlayer(player, SoundEvents.UI_BUTTON_CLICK, 1, 1f);
                break;
            case 2:
                if (this.hasEditPerm(this.data, player)) {
                    player.closeContainer();
                    player.level().getServer().execute(() -> PermissionScreenHandler.openClaimMenu(player, this.data, null));
                    ServerScreenHelper.playSongToPlayer(player, SoundEvents.UI_BUTTON_CLICK, 1, 1f);
                } else
                    ServerScreenHelper.playSongToPlayer(player, SoundEvents.VILLAGER_NO, 1, 1f);
                break;
            case 3:
                if (this.hasEditPerm(this.data, player)) {
                    player.closeContainer();
                    player.level().getServer().execute(() -> GroupScreenHandler.openGroupMenu(player, this.data));
                    ServerScreenHelper.playSongToPlayer(player, SoundEvents.UI_BUTTON_CLICK, 1, 1f);
                } else
                    ServerScreenHelper.playSongToPlayer(player, SoundEvents.VILLAGER_NO, 1, 1f);
                break;
            case 4:
                if (this.hasPerm(this.data, player, BuiltinPermission.EDITPOTIONS)) {
                    player.closeContainer();
                    player.level().getServer().execute(() -> PotionEditScreenHandler.openPotionMenu(player, this.data));
                    ServerScreenHelper.playSongToPlayer(player, SoundEvents.UI_BUTTON_CLICK, 1, 1f);
                } else
                    ServerScreenHelper.playSongToPlayer(player, SoundEvents.VILLAGER_NO, 1, 1f);
                break;
            case 5:
                if (this.hasPerm(this.data, player, BuiltinPermission.CLAIMMESSAGE)) {
                    player.closeContainer();
                    player.level().getServer().execute(() -> ClaimTextHandler.openClaimMenu(player, this.data));
                    ServerScreenHelper.playSongToPlayer(player, SoundEvents.UI_BUTTON_CLICK, 1, 1f);
                } else
                    ServerScreenHelper.playSongToPlayer(player, SoundEvents.VILLAGER_NO, 1, 1f);
                break;
            case 6:
                if (this.hasPerm(this.data, player, BuiltinPermission.EDITPERMS)) {
                    player.closeContainer();
                    player.level().getServer().execute(() -> FakePlayerScreenHandler.open(player, this.data));
                    ServerScreenHelper.playSongToPlayer(player, SoundEvents.UI_BUTTON_CLICK, 1, 1f);
                } else
                    ServerScreenHelper.playSongToPlayer(player, SoundEvents.VILLAGER_NO, 1, 1f);
                break;
            case 8:
                if (this.hasPerm(this.data, player, BuiltinPermission.EDITCLAIM)) {
                    player.closeContainer();
                    player.level().getServer().execute(() -> ConfirmScreenHandler.openConfirmScreen(player, (bool) -> {
                        if (bool) {
                            ClaimStorage storage = ClaimStorage.get(player.level());
                            storage.deleteClaim(this.data, true, PlayerClaimData.get(player).getClaimMode(), player.level());
                            player.closeContainer();
                            player.sendSystemMessage(ClaimUtils.translatedText("flan.deleteClaim", ChatFormatting.RED));
                            ServerScreenHelper.playSongToPlayer(player, SoundEvents.ANVIL_PLACE, 1, 1f);
                        } else {
                            player.closeContainer();
                            player.level().getServer().execute(() -> ClaimMenuScreenHandler.openClaimMenu(player, this.data));
                            ServerScreenHelper.playSongToPlayer(player, SoundEvents.VILLAGER_NO, 1, 1f);
                        }
                    }));
                } else
                    ServerScreenHelper.playSongToPlayer(player, SoundEvents.VILLAGER_NO, 1, 1f);
                break;
            case 11:
                if (this.hasPerm(this.data, player, BuiltinPermission.EDITPERMS)) {
                    player.closeContainer();
                    player.level().getServer().execute(() -> ClaimAllowListEntryScreenHandler.openScreen(player, this.data));
                    ServerScreenHelper.playSongToPlayer(player, SoundEvents.UI_BUTTON_CLICK, 1, 1f);
                } else
                    ServerScreenHelper.playSongToPlayer(player, SoundEvents.VILLAGER_NO, 1, 1f);
                break;
        }
        return true;
    }

    private boolean hasEditPerm(Claim claim, ServerPlayer player) {
        return ((claim.parentClaim() != null && claim.parentClaim().canInteract(player, BuiltinPermission.EDITPERMS, player.blockPosition()))
                || claim.canInteract(player, BuiltinPermission.EDITPERMS, player.blockPosition()));
    }

    private boolean hasPerm(Claim claim, ServerPlayer player, Identifier perm) {
        if (claim.parentClaim() != null)
            return claim.parentClaim().canInteract(player, perm, player.blockPosition());
        return claim.canInteract(player, perm, player.blockPosition());
    }
}
