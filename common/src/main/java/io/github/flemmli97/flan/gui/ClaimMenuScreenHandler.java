package io.github.flemmli97.flan.gui;

import io.github.flemmli97.flan.api.permission.BuiltinPermission;
import io.github.flemmli97.flan.claim.Claim;
import io.github.flemmli97.flan.claim.ClaimStorage;
import io.github.flemmli97.flan.claim.ClaimUtils;
import io.github.flemmli97.flan.gui.inv.SeparateInv;
import io.github.flemmli97.flan.player.PlayerClaimData;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
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
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;

public class ClaimMenuScreenHandler extends ServerOnlyScreenHandler<Claim> {

    private final Claim claim;

    private ClaimMenuScreenHandler(int syncId, Inventory playerInventory, Claim claim) {
        super(syncId, playerInventory, 2, claim);
        this.claim = claim;
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
    protected void fillInventoryWith(Player player, SeparateInv inv, Claim claim) {
        for (int i = 0; i < 18; i++) {
            switch (i) {
                case 0 -> {
                    ItemStack close = new ItemStack(Items.TNT);
                    close.set(DataComponents.CUSTOM_NAME, ServerScreenHelper.coloredGuiText("flan.screenClose", ChatFormatting.DARK_RED));
                    inv.updateStack(i, close);
                }
                case 2 -> {
                    ItemStack perm = new ItemStack(Items.BEACON);
                    perm.set(DataComponents.CUSTOM_NAME, ServerScreenHelper.coloredGuiText("flan.screenMenuGlobal", ChatFormatting.GOLD));
                    if (player instanceof ServerPlayer && !this.hasEditPerm(claim, (ServerPlayer) player))
                        ServerScreenHelper.addLore(perm, ServerScreenHelper.coloredGuiText("flan.screenNoPerm", ChatFormatting.DARK_RED));
                    inv.updateStack(i, perm);
                }
                case 3 -> {
                    ItemStack group = new ItemStack(Items.WRITABLE_BOOK);
                    group.set(DataComponents.CUSTOM_NAME, ServerScreenHelper.coloredGuiText("flan.screenMenuGroup", ChatFormatting.GOLD));
                    if (player instanceof ServerPlayer && !this.hasEditPerm(claim, (ServerPlayer) player))
                        ServerScreenHelper.addLore(group, ServerScreenHelper.coloredGuiText("flan.screenNoPerm", ChatFormatting.DARK_RED));
                    inv.updateStack(i, group);
                }
                case 4 -> {
                    ItemStack potions = new ItemStack(Items.POTION);
                    potions.set(DataComponents.CUSTOM_NAME, ServerScreenHelper.coloredGuiText("flan.screenMenuPotion", ChatFormatting.GOLD));
                    if (player instanceof ServerPlayer && !this.hasPerm(claim, (ServerPlayer) player, BuiltinPermission.EDITPOTIONS))
                        ServerScreenHelper.addLore(potions, ServerScreenHelper.coloredGuiText("flan.screenNoPerm", ChatFormatting.DARK_RED));
                    inv.updateStack(i, potions);
                }
                case 5 -> {
                    ItemStack sign = new ItemStack(Items.OAK_SIGN);
                    sign.set(DataComponents.CUSTOM_NAME, ServerScreenHelper.coloredGuiText("flan.screenMenuClaimText", ChatFormatting.GOLD));
                    if (player instanceof ServerPlayer && !this.hasPerm(claim, (ServerPlayer) player, BuiltinPermission.EDITCLAIM))
                        ServerScreenHelper.addLore(sign, ServerScreenHelper.coloredGuiText("flan.screenNoPerm", ChatFormatting.DARK_RED));
                    inv.updateStack(i, sign);
                }
                case 6 -> {
                    ItemStack head = new ItemStack(Items.ZOMBIE_HEAD);
                    head.set(DataComponents.CUSTOM_NAME, ServerScreenHelper.coloredGuiText("flan.screenMenuFakePlayers", ChatFormatting.GOLD));
                    if (player instanceof ServerPlayer && !this.hasPerm(claim, (ServerPlayer) player, BuiltinPermission.EDITPERMS))
                        ServerScreenHelper.addLore(head, ServerScreenHelper.coloredGuiText("flan.screenNoPerm", ChatFormatting.DARK_RED));
                    inv.updateStack(i, head);
                }
                case 8 -> {
                    ItemStack delete = new ItemStack(Items.BARRIER);
                    delete.set(DataComponents.CUSTOM_NAME, ServerScreenHelper.coloredGuiText("flan.screenMenuDelete", ChatFormatting.RED));
                    if (player instanceof ServerPlayer && !this.hasPerm(claim, (ServerPlayer) player, BuiltinPermission.EDITCLAIM))
                        ServerScreenHelper.addLore(delete, ServerScreenHelper.coloredGuiText("flan.screenNoPerm", ChatFormatting.DARK_RED));
                    inv.updateStack(i, delete);
                }
                case 11 -> {
                    ItemStack stack = PotionContents.createItemStack(Items.POTION, Potions.WATER);
                    stack.set(DataComponents.CUSTOM_NAME, ServerScreenHelper.coloredGuiText(CustomInteractListScreenHandler.Type.ITEM.translationKey, ChatFormatting.GOLD));
                    if (player instanceof ServerPlayer && !this.hasPerm(claim, (ServerPlayer) player, BuiltinPermission.EDITCLAIM))
                        ServerScreenHelper.addLore(stack, ServerScreenHelper.coloredGuiText("flan.screenNoPerm", ChatFormatting.DARK_RED));
                    inv.updateStack(i, stack);
                }
                case 12 -> {
                    ItemStack stack = new ItemStack(Items.DIAMOND_PICKAXE);
                    stack.set(DataComponents.CUSTOM_NAME, ServerScreenHelper.coloredGuiText(CustomInteractListScreenHandler.Type.BLOCKBREAK.translationKey, ChatFormatting.GOLD));
                    if (player instanceof ServerPlayer && !this.hasPerm(claim, (ServerPlayer) player, BuiltinPermission.EDITCLAIM))
                        ServerScreenHelper.addLore(stack, ServerScreenHelper.coloredGuiText("flan.screenNoPerm", ChatFormatting.DARK_RED));
                    inv.updateStack(i, stack);
                }
                case 13 -> {
                    ItemStack stack = new ItemStack(Items.RED_BANNER);
                    stack.set(DataComponents.CUSTOM_NAME, ServerScreenHelper.coloredGuiText(CustomInteractListScreenHandler.Type.BLOCKUSE.translationKey, ChatFormatting.GOLD));
                    if (player instanceof ServerPlayer && !this.hasPerm(claim, (ServerPlayer) player, BuiltinPermission.EDITCLAIM))
                        ServerScreenHelper.addLore(stack, ServerScreenHelper.coloredGuiText("flan.screenNoPerm", ChatFormatting.DARK_RED));
                    inv.updateStack(i, stack);
                }
                case 14 -> {
                    ItemStack stack = new ItemStack(Items.DIAMOND_SWORD);
                    stack.set(DataComponents.CUSTOM_NAME, ServerScreenHelper.coloredGuiText(CustomInteractListScreenHandler.Type.ENTITYATTACK.translationKey, ChatFormatting.GOLD));
                    if (player instanceof ServerPlayer && !this.hasPerm(claim, (ServerPlayer) player, BuiltinPermission.EDITCLAIM))
                        ServerScreenHelper.addLore(stack, ServerScreenHelper.coloredGuiText("flan.screenNoPerm", ChatFormatting.DARK_RED));
                    inv.updateStack(i, stack);
                }
                case 15 -> {
                    ItemStack stack = new ItemStack(Items.SHEARS);
                    stack.set(DataComponents.CUSTOM_NAME, ServerScreenHelper.coloredGuiText(CustomInteractListScreenHandler.Type.ENTITYUSE.translationKey, ChatFormatting.GOLD));
                    if (player instanceof ServerPlayer && !this.hasPerm(claim, (ServerPlayer) player, BuiltinPermission.EDITCLAIM))
                        ServerScreenHelper.addLore(stack, ServerScreenHelper.coloredGuiText("flan.screenNoPerm", ChatFormatting.DARK_RED));
                    inv.updateStack(i, stack);
                }
                default -> inv.updateStack(i, ServerScreenHelper.emptyFiller());
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
                if (this.hasEditPerm(this.claim, player)) {
                    player.closeContainer();
                    player.getServer().execute(() -> PermissionScreenHandler.openClaimMenu(player, this.claim, null));
                    ServerScreenHelper.playSongToPlayer(player, SoundEvents.UI_BUTTON_CLICK, 1, 1f);
                } else
                    ServerScreenHelper.playSongToPlayer(player, SoundEvents.VILLAGER_NO, 1, 1f);
                break;
            case 3:
                if (this.hasEditPerm(this.claim, player)) {
                    player.closeContainer();
                    player.getServer().execute(() -> GroupScreenHandler.openGroupMenu(player, this.claim));
                    ServerScreenHelper.playSongToPlayer(player, SoundEvents.UI_BUTTON_CLICK, 1, 1f);
                } else
                    ServerScreenHelper.playSongToPlayer(player, SoundEvents.VILLAGER_NO, 1, 1f);
                break;
            case 4:
                if (this.hasPerm(this.claim, player, BuiltinPermission.EDITPOTIONS)) {
                    player.closeContainer();
                    player.getServer().execute(() -> PotionEditScreenHandler.openPotionMenu(player, this.claim));
                    ServerScreenHelper.playSongToPlayer(player, SoundEvents.UI_BUTTON_CLICK, 1, 1f);
                } else
                    ServerScreenHelper.playSongToPlayer(player, SoundEvents.VILLAGER_NO, 1, 1f);
                break;
            case 5:
                if (this.hasPerm(this.claim, player, BuiltinPermission.CLAIMMESSAGE)) {
                    player.closeContainer();
                    player.getServer().execute(() -> ClaimTextHandler.openClaimMenu(player, this.claim));
                    ServerScreenHelper.playSongToPlayer(player, SoundEvents.UI_BUTTON_CLICK, 1, 1f);
                } else
                    ServerScreenHelper.playSongToPlayer(player, SoundEvents.VILLAGER_NO, 1, 1f);
                break;
            case 6:
                if (this.hasPerm(this.claim, player, BuiltinPermission.EDITPERMS)) {
                    player.closeContainer();
                    player.getServer().execute(() -> FakePlayerScreenHandler.open(player, this.claim));
                    ServerScreenHelper.playSongToPlayer(player, SoundEvents.UI_BUTTON_CLICK, 1, 1f);
                } else
                    ServerScreenHelper.playSongToPlayer(player, SoundEvents.VILLAGER_NO, 1, 1f);
                break;
            case 8:
                if (this.hasPerm(this.claim, player, BuiltinPermission.EDITCLAIM)) {
                    player.closeContainer();
                    player.getServer().execute(() -> ConfirmScreenHandler.openConfirmScreen(player, (bool) -> {
                        if (bool) {
                            ClaimStorage storage = ClaimStorage.get(player.serverLevel());
                            storage.deleteClaim(this.claim, true, PlayerClaimData.get(player).getClaimMode(), player.serverLevel());
                            player.closeContainer();
                            player.displayClientMessage(ClaimUtils.translatedText("flan.deleteClaim", ChatFormatting.RED), false);
                            ServerScreenHelper.playSongToPlayer(player, SoundEvents.ANVIL_PLACE, 1, 1f);
                        } else {
                            player.closeContainer();
                            player.getServer().execute(() -> ClaimMenuScreenHandler.openClaimMenu(player, this.claim));
                            ServerScreenHelper.playSongToPlayer(player, SoundEvents.VILLAGER_NO, 1, 1f);
                        }
                    }));
                } else
                    ServerScreenHelper.playSongToPlayer(player, SoundEvents.VILLAGER_NO, 1, 1f);
                break;
            case 11:
                if (this.hasPerm(this.claim, player, BuiltinPermission.EDITPERMS)) {
                    player.closeContainer();
                    player.getServer().execute(() -> CustomInteractListScreenHandler.openMenu(player, CustomInteractListScreenHandler.Type.ITEM, this.claim));
                    ServerScreenHelper.playSongToPlayer(player, SoundEvents.UI_BUTTON_CLICK, 1, 1f);
                } else
                    ServerScreenHelper.playSongToPlayer(player, SoundEvents.VILLAGER_NO, 1, 1f);
                break;
            case 12:
                if (this.hasPerm(this.claim, player, BuiltinPermission.EDITPERMS)) {
                    player.closeContainer();
                    player.getServer().execute(() -> CustomInteractListScreenHandler.openMenu(player, CustomInteractListScreenHandler.Type.BLOCKBREAK, this.claim));
                    ServerScreenHelper.playSongToPlayer(player, SoundEvents.UI_BUTTON_CLICK, 1, 1f);
                } else
                    ServerScreenHelper.playSongToPlayer(player, SoundEvents.VILLAGER_NO, 1, 1f);
                break;
            case 13:
                if (this.hasPerm(this.claim, player, BuiltinPermission.EDITPERMS)) {
                    player.closeContainer();
                    player.getServer().execute(() -> CustomInteractListScreenHandler.openMenu(player, CustomInteractListScreenHandler.Type.BLOCKUSE, this.claim));
                    ServerScreenHelper.playSongToPlayer(player, SoundEvents.UI_BUTTON_CLICK, 1, 1f);
                } else
                    ServerScreenHelper.playSongToPlayer(player, SoundEvents.VILLAGER_NO, 1, 1f);
                break;
            case 14:
                if (this.hasPerm(this.claim, player, BuiltinPermission.EDITPERMS)) {
                    player.closeContainer();
                    player.getServer().execute(() -> CustomInteractListScreenHandler.openMenu(player, CustomInteractListScreenHandler.Type.ENTITYATTACK, this.claim));
                    ServerScreenHelper.playSongToPlayer(player, SoundEvents.UI_BUTTON_CLICK, 1, 1f);
                } else
                    ServerScreenHelper.playSongToPlayer(player, SoundEvents.VILLAGER_NO, 1, 1f);
                break;
            case 15:
                if (this.hasPerm(this.claim, player, BuiltinPermission.EDITPERMS)) {
                    player.closeContainer();
                    player.getServer().execute(() -> CustomInteractListScreenHandler.openMenu(player, CustomInteractListScreenHandler.Type.ENTITYUSE, this.claim));
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
