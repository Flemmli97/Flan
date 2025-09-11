package io.github.flemmli97.flan.gui;

import io.github.flemmli97.flan.api.permission.BuiltinPermission;
import io.github.flemmli97.flan.claim.Claim;
import io.github.flemmli97.flan.claim.ClaimStorage;
import io.github.flemmli97.flan.claim.ClaimUtils;
import io.github.flemmli97.flan.player.PlayerClaimData;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;

public class ClaimMenuScreenHandler extends ServerOnlyScreenHandler<Claim> {

    private ClaimMenuScreenHandler(int syncId, Inventory playerInventory, Claim claim) {
        super(syncId, playerInventory, 3, claim);
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
        for (int i = 0; i < 27; i++) {
            switch (i) {
                case 0 -> {
                    ItemStack close = new ItemStack(Items.TNT);
                    close.setHoverName(ServerScreenHelper.coloredGuiText("flan.screenClose", ChatFormatting.DARK_RED));
                    this.slots.get(i).set(close);
                }
                case 2 -> {
                    ItemStack perm = new ItemStack(Items.BEACON);
                    perm.setHoverName(ServerScreenHelper.coloredGuiText("flan.screenMenuGlobal", ChatFormatting.GOLD));
                    if (!this.hasEditPerm(this.data, this.player))
                        ServerScreenHelper.addLore(perm, ServerScreenHelper.coloredGuiText("flan.screenNoPerm", ChatFormatting.DARK_RED));
                    this.slots.get(i).set(perm);
                }
                case 3 -> {
                    ItemStack group = new ItemStack(Items.WRITABLE_BOOK);
                    group.setHoverName(ServerScreenHelper.coloredGuiText("flan.screenMenuGroup", ChatFormatting.GOLD));
                    if (!this.hasEditPerm(this.data, this.player))
                        ServerScreenHelper.addLore(group, ServerScreenHelper.coloredGuiText("flan.screenNoPerm", ChatFormatting.DARK_RED));
                    this.slots.get(i).set(group);
                }
                case 4 -> {
                    ItemStack potions = new ItemStack(Items.POTION);
                    potions.setHoverName(ServerScreenHelper.coloredGuiText("flan.screenMenuPotion", ChatFormatting.GOLD));
                    if (!this.hasPerm(this.data, this.player, BuiltinPermission.EDITPOTIONS))
                        ServerScreenHelper.addLore(potions, ServerScreenHelper.coloredGuiText("flan.screenNoPerm", ChatFormatting.DARK_RED));
                    this.slots.get(i).set(potions);
                }
                case 5 -> {
                    ItemStack sign = new ItemStack(Items.OAK_SIGN);
                    sign.setHoverName(ServerScreenHelper.coloredGuiText("flan.screenMenuClaimText", ChatFormatting.GOLD));
                    if (!this.hasPerm(this.data, this.player, BuiltinPermission.EDITCLAIM))
                        ServerScreenHelper.addLore(sign, ServerScreenHelper.coloredGuiText("flan.screenNoPerm", ChatFormatting.DARK_RED));
                    this.slots.get(i).set(sign);
                }
                case 6 -> {
                    ItemStack head = new ItemStack(Items.ZOMBIE_HEAD);
                    head.setHoverName(ServerScreenHelper.coloredGuiText("flan.screenMenuFakePlayers", ChatFormatting.GOLD));
                    if (!this.hasPerm(this.data, this.player, BuiltinPermission.EDITPERMS))
                        ServerScreenHelper.addLore(head, ServerScreenHelper.coloredGuiText("flan.screenNoPerm", ChatFormatting.DARK_RED));
                    this.slots.get(i).set(head);
                }
                case 8 -> {
                    ItemStack delete = new ItemStack(Items.BARRIER);
                    delete.setHoverName(ServerScreenHelper.coloredGuiText("flan.screenMenuDelete", ChatFormatting.RED));
                    if (!this.hasPerm(this.data, this.player, BuiltinPermission.EDITCLAIM))
                        ServerScreenHelper.addLore(delete, ServerScreenHelper.coloredGuiText("flan.screenNoPerm", ChatFormatting.DARK_RED));
                    this.slots.get(i).set(delete);
                }
                case 11 -> {
                    ItemStack stack = new ItemStack(Items.POTION);
                    PotionUtils.setPotion(stack, Potions.WATER);
                    stack.setHoverName(ServerScreenHelper.coloredGuiText(CustomInteractListScreenHandler.Type.ITEM.translationKey, ChatFormatting.GOLD));
                    if (!this.hasPerm(this.data, this.player, BuiltinPermission.EDITCLAIM))
                        ServerScreenHelper.addLore(stack, ServerScreenHelper.coloredGuiText("flan.screenNoPerm", ChatFormatting.DARK_RED));
                    this.slots.get(i).set(stack);
                }
                case 12 -> {
                    ItemStack stack = new ItemStack(Items.DIAMOND_PICKAXE);
                    stack.setHoverName(ServerScreenHelper.coloredGuiText(CustomInteractListScreenHandler.Type.BLOCKBREAK.translationKey, ChatFormatting.GOLD));
                    if (!this.hasPerm(this.data, this.player, BuiltinPermission.EDITCLAIM))
                        ServerScreenHelper.addLore(stack, ServerScreenHelper.coloredGuiText("flan.screenNoPerm", ChatFormatting.DARK_RED));
                    this.slots.get(i).set(stack);
                }
                case 13 -> {
                    ItemStack stack = new ItemStack(Items.DIAMOND_SHOVEL);
                    stack.setHoverName(ServerScreenHelper.coloredGuiText(CustomInteractListScreenHandler.Type.BLOCKPLACE.translationKey, ChatFormatting.GOLD));
                    if (!this.hasPerm(this.data, this.player, BuiltinPermission.EDITCLAIM))
                        ServerScreenHelper.addLore(stack, ServerScreenHelper.coloredGuiText("flan.screenNoPerm", ChatFormatting.DARK_RED));
                    this.slots.get(i).set(stack);
                }
                case 14 -> {
                    ItemStack stack = new ItemStack(Items.RED_BANNER);
                    stack.setHoverName(ServerScreenHelper.coloredGuiText(CustomInteractListScreenHandler.Type.BLOCKUSE.translationKey, ChatFormatting.GOLD));
                    if (!this.hasPerm(this.data, this.player, BuiltinPermission.EDITCLAIM))
                        ServerScreenHelper.addLore(stack, ServerScreenHelper.coloredGuiText("flan.screenNoPerm", ChatFormatting.DARK_RED));
                    this.slots.get(i).set(stack);
                }
                case 15 -> {
                    ItemStack stack = new ItemStack(Items.DIAMOND_SWORD);
                    stack.setHoverName(ServerScreenHelper.coloredGuiText(CustomInteractListScreenHandler.Type.ENTITYATTACK.translationKey, ChatFormatting.GOLD));
                    if (!this.hasPerm(this.data, this.player, BuiltinPermission.EDITCLAIM))
                        ServerScreenHelper.addLore(stack, ServerScreenHelper.coloredGuiText("flan.screenNoPerm", ChatFormatting.DARK_RED));
                    this.slots.get(i).set(stack);
                }
                case 20 -> {
                    ItemStack stack = new ItemStack(Items.SHEARS);
                    stack.setHoverName(ServerScreenHelper.coloredGuiText(CustomInteractListScreenHandler.Type.ENTITYUSE.translationKey, ChatFormatting.GOLD));
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
        return slot == 0 || slot == 2 || slot == 3 || slot == 4 || slot == 5 || slot == 6 || slot == 8 || (slot >= 11 && slot <= 15) || slot == 20;
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
                    player.getServer().execute(() -> PermissionScreenHandler.openClaimMenu(player, this.data, null));
                    ServerScreenHelper.playSongToPlayer(player, SoundEvents.UI_BUTTON_CLICK, 1, 1f);
                } else
                    ServerScreenHelper.playSongToPlayer(player, SoundEvents.VILLAGER_NO, 1, 1f);
                break;
            case 3:
                if (this.hasEditPerm(this.data, player)) {
                    player.closeContainer();
                    player.getServer().execute(() -> GroupScreenHandler.openGroupMenu(player, this.data));
                    ServerScreenHelper.playSongToPlayer(player, SoundEvents.UI_BUTTON_CLICK, 1, 1f);
                } else
                    ServerScreenHelper.playSongToPlayer(player, SoundEvents.VILLAGER_NO, 1, 1f);
                break;
            case 4:
                if (this.hasPerm(this.data, player, BuiltinPermission.EDITPOTIONS)) {
                    player.closeContainer();
                    player.getServer().execute(() -> PotionEditScreenHandler.openPotionMenu(player, this.data));
                    ServerScreenHelper.playSongToPlayer(player, SoundEvents.UI_BUTTON_CLICK, 1, 1f);
                } else
                    ServerScreenHelper.playSongToPlayer(player, SoundEvents.VILLAGER_NO, 1, 1f);
                break;
            case 5:
                if (this.hasPerm(this.data, player, BuiltinPermission.CLAIMMESSAGE)) {
                    player.closeContainer();
                    player.getServer().execute(() -> ClaimTextHandler.openClaimMenu(player, this.data));
                    ServerScreenHelper.playSongToPlayer(player, SoundEvents.UI_BUTTON_CLICK, 1, 1f);
                } else
                    ServerScreenHelper.playSongToPlayer(player, SoundEvents.VILLAGER_NO, 1, 1f);
                break;
            case 6:
                if (this.hasPerm(this.data, player, BuiltinPermission.EDITPERMS)) {
                    player.closeContainer();
                    player.getServer().execute(() -> FakePlayerScreenHandler.open(player, this.data));
                    ServerScreenHelper.playSongToPlayer(player, SoundEvents.UI_BUTTON_CLICK, 1, 1f);
                } else
                    ServerScreenHelper.playSongToPlayer(player, SoundEvents.VILLAGER_NO, 1, 1f);
                break;
            case 8:
                if (this.hasPerm(this.data, player, BuiltinPermission.EDITCLAIM)) {
                    player.closeContainer();
                    player.getServer().execute(() -> ConfirmScreenHandler.openConfirmScreen(player, (bool) -> {
                        if (bool) {
                            ClaimStorage storage = ClaimStorage.get(player.serverLevel());
                            storage.deleteClaim(this.data, true, PlayerClaimData.get(player).getClaimMode(), player.serverLevel());
                            player.closeContainer();
                            player.displayClientMessage(ClaimUtils.translatedText("flan.deleteClaim", ChatFormatting.RED), false);
                            ServerScreenHelper.playSongToPlayer(player, SoundEvents.ANVIL_PLACE, 1, 1f);
                        } else {
                            player.closeContainer();
                            player.getServer().execute(() -> ClaimMenuScreenHandler.openClaimMenu(player, this.data));
                            ServerScreenHelper.playSongToPlayer(player, SoundEvents.VILLAGER_NO, 1, 1f);
                        }
                    }));
                } else
                    ServerScreenHelper.playSongToPlayer(player, SoundEvents.VILLAGER_NO, 1, 1f);
                break;
            case 11:
                if (this.hasPerm(this.data, player, BuiltinPermission.EDITPERMS)) {
                    player.closeContainer();
                    player.getServer().execute(() -> CustomInteractListScreenHandler.openMenu(player, CustomInteractListScreenHandler.Type.ITEM, this.data));
                    ServerScreenHelper.playSongToPlayer(player, SoundEvents.UI_BUTTON_CLICK, 1, 1f);
                } else
                    ServerScreenHelper.playSongToPlayer(player, SoundEvents.VILLAGER_NO, 1, 1f);
                break;
            case 12:
                if (this.hasPerm(this.data, player, BuiltinPermission.EDITPERMS)) {
                    player.closeContainer();
                    player.getServer().execute(() -> CustomInteractListScreenHandler.openMenu(player, CustomInteractListScreenHandler.Type.BLOCKBREAK, this.data));
                    ServerScreenHelper.playSongToPlayer(player, SoundEvents.UI_BUTTON_CLICK, 1, 1f);
                } else
                    ServerScreenHelper.playSongToPlayer(player, SoundEvents.VILLAGER_NO, 1, 1f);
                break;
            case 13:
                if (this.hasPerm(this.data, player, BuiltinPermission.EDITPERMS)) {
                    player.closeContainer();
                    player.getServer().execute(() -> CustomInteractListScreenHandler.openMenu(player, CustomInteractListScreenHandler.Type.BLOCKPLACE, this.data));
                    ServerScreenHelper.playSongToPlayer(player, SoundEvents.UI_BUTTON_CLICK, 1, 1f);
                } else
                    ServerScreenHelper.playSongToPlayer(player, SoundEvents.VILLAGER_NO, 1, 1f);
                break;
            case 14:
                if (this.hasPerm(this.data, player, BuiltinPermission.EDITPERMS)) {
                    player.closeContainer();
                    player.getServer().execute(() -> CustomInteractListScreenHandler.openMenu(player, CustomInteractListScreenHandler.Type.BLOCKUSE, this.data));
                    ServerScreenHelper.playSongToPlayer(player, SoundEvents.UI_BUTTON_CLICK, 1, 1f);
                } else
                    ServerScreenHelper.playSongToPlayer(player, SoundEvents.VILLAGER_NO, 1, 1f);
                break;
            case 15:
                if (this.hasPerm(this.data, player, BuiltinPermission.EDITPERMS)) {
                    player.closeContainer();
                    player.getServer().execute(() -> CustomInteractListScreenHandler.openMenu(player, CustomInteractListScreenHandler.Type.ENTITYATTACK, this.data));
                    ServerScreenHelper.playSongToPlayer(player, SoundEvents.UI_BUTTON_CLICK, 1, 1f);
                } else
                    ServerScreenHelper.playSongToPlayer(player, SoundEvents.VILLAGER_NO, 1, 1f);
                break;
            case 20:
                if (this.hasPerm(this.data, player, BuiltinPermission.EDITPERMS)) {
                    player.closeContainer();
                    player.getServer().execute(() -> CustomInteractListScreenHandler.openMenu(player, CustomInteractListScreenHandler.Type.ENTITYUSE, this.data));
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

    private boolean hasPerm(Claim claim, ServerPlayer player, ResourceLocation perm) {
        if (claim.parentClaim() != null)
            return claim.parentClaim().canInteract(player, perm, player.blockPosition());
        return claim.canInteract(player, perm, player.blockPosition());
    }
}
