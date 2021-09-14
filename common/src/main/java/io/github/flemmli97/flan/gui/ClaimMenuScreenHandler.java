package io.github.flemmli97.flan.gui;

import io.github.flemmli97.flan.api.permission.ClaimPermission;
import io.github.flemmli97.flan.api.permission.PermissionRegistry;
import io.github.flemmli97.flan.claim.Claim;
import io.github.flemmli97.flan.claim.ClaimStorage;
import io.github.flemmli97.flan.claim.PermHelper;
import io.github.flemmli97.flan.config.ConfigHandler;
import io.github.flemmli97.flan.gui.inv.SeparateInv;
import io.github.flemmli97.flan.player.PlayerClaimData;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class ClaimMenuScreenHandler extends ServerOnlyScreenHandler<Claim> {

    private final Claim claim;

    private ClaimMenuScreenHandler(int syncId, PlayerInventory playerInventory, Claim claim) {
        super(syncId, playerInventory, 1, claim);
        this.claim = claim;
    }

    public static void openClaimMenu(ServerPlayerEntity player, Claim claim) {
        NamedScreenHandlerFactory fac = new NamedScreenHandlerFactory() {
            @Override
            public ScreenHandler createMenu(int syncId, PlayerInventory inv, PlayerEntity player) {
                return new ClaimMenuScreenHandler(syncId, inv, claim);
            }

            @Override
            public Text getDisplayName() {
                return PermHelper.simpleColoredText(claim.parentClaim() != null ? ConfigHandler.lang.screenMenuSub : ConfigHandler.lang.screenMenu);
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
                    close.setCustomName(ServerScreenHelper.coloredGuiText(ConfigHandler.lang.screenClose, Formatting.DARK_RED));
                    inv.updateStack(i, close);
                    break;
                case 2:
                    ItemStack perm = new ItemStack(Items.BEACON);
                    perm.setCustomName(ServerScreenHelper.coloredGuiText(ConfigHandler.lang.screenMenuGlobal, Formatting.GOLD));
                    if (player instanceof ServerPlayerEntity && !this.hasEditPerm(claim, (ServerPlayerEntity) player))
                        ServerScreenHelper.addLore(perm, ServerScreenHelper.coloredGuiText(ConfigHandler.lang.screenNoPerm, Formatting.DARK_RED));
                    inv.updateStack(i, perm);
                    break;
                case 3:
                    ItemStack group = new ItemStack(Items.WRITABLE_BOOK);
                    group.setCustomName(ServerScreenHelper.coloredGuiText(ConfigHandler.lang.screenMenuGroup, Formatting.GOLD));
                    if (player instanceof ServerPlayerEntity && !this.hasEditPerm(claim, (ServerPlayerEntity) player))
                        ServerScreenHelper.addLore(group, ServerScreenHelper.coloredGuiText(ConfigHandler.lang.screenNoPerm, Formatting.DARK_RED));
                    inv.updateStack(i, group);
                    break;
                case 4:
                    ItemStack potions = new ItemStack(Items.POTION);
                    potions.setCustomName(ServerScreenHelper.coloredGuiText(ConfigHandler.lang.screenMenuPotion, Formatting.GOLD));
                    if (player instanceof ServerPlayerEntity && !this.hasPerm(claim, (ServerPlayerEntity) player, PermissionRegistry.EDITPOTIONS))
                        ServerScreenHelper.addLore(potions, ServerScreenHelper.coloredGuiText(ConfigHandler.lang.screenNoPerm, Formatting.DARK_RED));
                    inv.updateStack(i, potions);
                    break;
                case 5:
                    ItemStack sign = new ItemStack(Items.OAK_SIGN);
                    sign.setCustomName(ServerScreenHelper.coloredGuiText(ConfigHandler.lang.screenMenuClaimText, Formatting.GOLD));
                    if (player instanceof ServerPlayerEntity && !this.hasPerm(claim, (ServerPlayerEntity) player, PermissionRegistry.EDITCLAIM))
                        ServerScreenHelper.addLore(sign, ServerScreenHelper.coloredGuiText(ConfigHandler.lang.screenNoPerm, Formatting.DARK_RED));
                    inv.updateStack(i, sign);
                    break;
                case 8:
                    ItemStack delete = new ItemStack(Items.BARRIER);
                    delete.setCustomName(ServerScreenHelper.coloredGuiText(ConfigHandler.lang.screenMenuDelete, Formatting.RED));
                    if (player instanceof ServerPlayerEntity && !this.hasPerm(claim, (ServerPlayerEntity) player, PermissionRegistry.EDITCLAIM))
                        ServerScreenHelper.addLore(delete, ServerScreenHelper.coloredGuiText(ConfigHandler.lang.screenNoPerm, Formatting.DARK_RED));
                    inv.updateStack(i, delete);
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
        switch (index) {
            case 0:
                player.closeHandledScreen();
                ServerScreenHelper.playSongToPlayer(player, SoundEvents.UI_BUTTON_CLICK, 1, 1f);
                break;
            case 2:
                if (this.hasEditPerm(this.claim, player)) {
                    player.closeHandledScreen();
                    player.getServer().execute(() -> PermissionScreenHandler.openClaimMenu(player, this.claim, null));
                    ServerScreenHelper.playSongToPlayer(player, SoundEvents.UI_BUTTON_CLICK, 1, 1f);
                } else
                    ServerScreenHelper.playSongToPlayer(player, SoundEvents.ENTITY_VILLAGER_NO, 1, 1f);
                break;
            case 3:
                if (this.hasEditPerm(this.claim, player)) {
                    player.closeHandledScreen();
                    player.getServer().execute(() -> GroupScreenHandler.openGroupMenu(player, this.claim));
                    ServerScreenHelper.playSongToPlayer(player, SoundEvents.UI_BUTTON_CLICK, 1, 1f);
                } else
                    ServerScreenHelper.playSongToPlayer(player, SoundEvents.ENTITY_VILLAGER_NO, 1, 1f);
                break;
            case 4:
                if (this.hasPerm(this.claim, player, PermissionRegistry.EDITPOTIONS)) {
                    player.closeHandledScreen();
                    player.getServer().execute(() -> PotionEditScreenHandler.openPotionMenu(player, this.claim));
                    ServerScreenHelper.playSongToPlayer(player, SoundEvents.UI_BUTTON_CLICK, 1, 1f);
                } else
                    ServerScreenHelper.playSongToPlayer(player, SoundEvents.ENTITY_VILLAGER_NO, 1, 1f);
                break;
            case 5:
                if (this.hasPerm(this.claim, player, PermissionRegistry.EDITCLAIM)) {
                    player.closeHandledScreen();
                    player.getServer().execute(() -> ClaimTextHandler.openClaimMenu(player, this.claim));
                    ServerScreenHelper.playSongToPlayer(player, SoundEvents.UI_BUTTON_CLICK, 1, 1f);
                } else
                    ServerScreenHelper.playSongToPlayer(player, SoundEvents.ENTITY_VILLAGER_NO, 1, 1f);
                break;
            case 8:
                if (this.hasPerm(this.claim, player, PermissionRegistry.EDITCLAIM)) {
                    player.closeHandledScreen();
                    player.getServer().execute(() -> ConfirmScreenHandler.openConfirmScreen(player, (bool) -> {
                        if (bool) {
                            ClaimStorage storage = ClaimStorage.get(player.getServerWorld());
                            storage.deleteClaim(this.claim, true, PlayerClaimData.get(player).getEditMode(), player.getServerWorld());
                            player.closeHandledScreen();
                            player.sendMessage(PermHelper.simpleColoredText(ConfigHandler.lang.deleteClaim, Formatting.RED), false);
                            ServerScreenHelper.playSongToPlayer(player, SoundEvents.BLOCK_ANVIL_PLACE, 1, 1f);
                        } else {
                            player.closeHandledScreen();
                            player.getServer().execute(() -> ClaimMenuScreenHandler.openClaimMenu(player, this.claim));
                            ServerScreenHelper.playSongToPlayer(player, SoundEvents.ENTITY_VILLAGER_NO, 1, 1f);
                        }
                    }));
                } else
                    ServerScreenHelper.playSongToPlayer(player, SoundEvents.ENTITY_VILLAGER_NO, 1, 1f);
                break;
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
