package io.github.flemmli97.flan.gui;

import io.github.flemmli97.flan.api.permission.ClaimPermission;
import io.github.flemmli97.flan.api.permission.PermissionRegistry;
import io.github.flemmli97.flan.claim.Claim;
import io.github.flemmli97.flan.claim.ClaimStorage;
import io.github.flemmli97.flan.claim.PermHelper;
import io.github.flemmli97.flan.config.ConfigHandler;
import io.github.flemmli97.flan.gui.inv.SeparateInv;
import io.github.flemmli97.flan.player.PlayerClaimData;
import net.minecraft.ChatFormatting;
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

public class ClaimMenuScreenHandler extends ServerOnlyScreenHandler<Claim> {

    private final Claim claim;

    private ClaimMenuScreenHandler(int syncId, Inventory playerInventory, Claim claim) {
        super(syncId, playerInventory, 1, claim);
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
                return PermHelper.simpleColoredText(claim.parentClaim() != null ? ConfigHandler.langManager.get("screenMenuSub") : ConfigHandler.langManager.get("screenMenu"));
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
                    close.setHoverName(ServerScreenHelper.coloredGuiText(ConfigHandler.langManager.get("screenClose"), ChatFormatting.DARK_RED));
                    inv.updateStack(i, close);
                }
                case 2 -> {
                    ItemStack perm = new ItemStack(Items.BEACON);
                    perm.setHoverName(ServerScreenHelper.coloredGuiText(ConfigHandler.langManager.get("screenMenuGlobal"), ChatFormatting.GOLD));
                    if (player instanceof ServerPlayer && !this.hasEditPerm(claim, (ServerPlayer) player))
                        ServerScreenHelper.addLore(perm, ServerScreenHelper.coloredGuiText(ConfigHandler.langManager.get("screenNoPerm"), ChatFormatting.DARK_RED));
                    inv.updateStack(i, perm);
                }
                case 3 -> {
                    ItemStack group = new ItemStack(Items.WRITABLE_BOOK);
                    group.setHoverName(ServerScreenHelper.coloredGuiText(ConfigHandler.langManager.get("screenMenuGroup"), ChatFormatting.GOLD));
                    if (player instanceof ServerPlayer && !this.hasEditPerm(claim, (ServerPlayer) player))
                        ServerScreenHelper.addLore(group, ServerScreenHelper.coloredGuiText(ConfigHandler.langManager.get("screenNoPerm"), ChatFormatting.DARK_RED));
                    inv.updateStack(i, group);
                }
                case 4 -> {
                    ItemStack potions = new ItemStack(Items.POTION);
                    potions.setHoverName(ServerScreenHelper.coloredGuiText(ConfigHandler.langManager.get("screenMenuPotion"), ChatFormatting.GOLD));
                    if (player instanceof ServerPlayer && !this.hasPerm(claim, (ServerPlayer) player, PermissionRegistry.EDITPOTIONS))
                        ServerScreenHelper.addLore(potions, ServerScreenHelper.coloredGuiText(ConfigHandler.langManager.get("screenNoPerm"), ChatFormatting.DARK_RED));
                    inv.updateStack(i, potions);
                }
                case 5 -> {
                    ItemStack sign = new ItemStack(Items.OAK_SIGN);
                    sign.setHoverName(ServerScreenHelper.coloredGuiText(ConfigHandler.langManager.get("screenMenuClaimText"), ChatFormatting.GOLD));
                    if (player instanceof ServerPlayer && !this.hasPerm(claim, (ServerPlayer) player, PermissionRegistry.EDITCLAIM))
                        ServerScreenHelper.addLore(sign, ServerScreenHelper.coloredGuiText(ConfigHandler.langManager.get("screenNoPerm"), ChatFormatting.DARK_RED));
                    inv.updateStack(i, sign);
                }
                case 6 -> {
                    ItemStack head = new ItemStack(Items.ZOMBIE_HEAD);
                    head.setHoverName(ServerScreenHelper.coloredGuiText(ConfigHandler.langManager.get("screenMenuFakePlayers"), ChatFormatting.GOLD));
                    if (player instanceof ServerPlayer && !this.hasPerm(claim, (ServerPlayer) player, PermissionRegistry.EDITPERMS))
                        ServerScreenHelper.addLore(head, ServerScreenHelper.coloredGuiText(ConfigHandler.langManager.get("screenNoPerm"), ChatFormatting.DARK_RED));
                    inv.updateStack(i, head);
                }
                case 8 -> {
                    ItemStack delete = new ItemStack(Items.BARRIER);
                    delete.setHoverName(ServerScreenHelper.coloredGuiText(ConfigHandler.langManager.get("screenMenuDelete"), ChatFormatting.RED));
                    if (player instanceof ServerPlayer && !this.hasPerm(claim, (ServerPlayer) player, PermissionRegistry.EDITCLAIM))
                        ServerScreenHelper.addLore(delete, ServerScreenHelper.coloredGuiText(ConfigHandler.langManager.get("screenNoPerm"), ChatFormatting.DARK_RED));
                    inv.updateStack(i, delete);
                }
                default -> inv.updateStack(i, ServerScreenHelper.emptyFiller());
            }
        }
    }

    @Override
    protected boolean isRightSlot(int slot) {
        return slot == 0 || slot == 2 || slot == 3 || slot == 4 || slot == 5 || slot == 6 || slot == 8;
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
                if (this.hasPerm(this.claim, player, PermissionRegistry.EDITPOTIONS)) {
                    player.closeContainer();
                    player.getServer().execute(() -> PotionEditScreenHandler.openPotionMenu(player, this.claim));
                    ServerScreenHelper.playSongToPlayer(player, SoundEvents.UI_BUTTON_CLICK, 1, 1f);
                } else
                    ServerScreenHelper.playSongToPlayer(player, SoundEvents.VILLAGER_NO, 1, 1f);
                break;
            case 5:
                if (this.hasPerm(this.claim, player, PermissionRegistry.CLAIMMESSAGE)) {
                    player.closeContainer();
                    player.getServer().execute(() -> ClaimTextHandler.openClaimMenu(player, this.claim));
                    ServerScreenHelper.playSongToPlayer(player, SoundEvents.UI_BUTTON_CLICK, 1, 1f);
                } else
                    ServerScreenHelper.playSongToPlayer(player, SoundEvents.VILLAGER_NO, 1, 1f);
                break;
            case 6:
                if (this.hasPerm(this.claim, player, PermissionRegistry.EDITPERMS)) {
                    player.closeContainer();
                    player.getServer().execute(() -> FakePlayerScreenHandler.open(player, this.claim));
                    ServerScreenHelper.playSongToPlayer(player, SoundEvents.UI_BUTTON_CLICK, 1, 1f);
                } else
                    ServerScreenHelper.playSongToPlayer(player, SoundEvents.VILLAGER_NO, 1, 1f);
                break;
            case 8:
                if (this.hasPerm(this.claim, player, PermissionRegistry.EDITCLAIM)) {
                    player.closeContainer();
                    player.getServer().execute(() -> ConfirmScreenHandler.openConfirmScreen(player, (bool) -> {
                        if (bool) {
                            ClaimStorage storage = ClaimStorage.get(player.getLevel());
                            storage.deleteClaim(this.claim, true, PlayerClaimData.get(player).getEditMode(), player.getLevel());
                            player.closeContainer();
                            player.displayClientMessage(PermHelper.simpleColoredText(ConfigHandler.langManager.get("deleteClaim"), ChatFormatting.RED), false);
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
