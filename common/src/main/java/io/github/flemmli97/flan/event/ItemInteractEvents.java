package io.github.flemmli97.flan.event;

import com.google.common.collect.Sets;
import com.mojang.authlib.GameProfile;
import io.github.flemmli97.flan.api.data.IPermissionContainer;
import io.github.flemmli97.flan.api.permission.ClaimPermission;
import io.github.flemmli97.flan.api.permission.ObjectToPermissionMap;
import io.github.flemmli97.flan.api.permission.PermissionRegistry;
import io.github.flemmli97.flan.claim.Claim;
import io.github.flemmli97.flan.claim.ClaimStorage;
import io.github.flemmli97.flan.claim.PermHelper;
import io.github.flemmli97.flan.config.ConfigHandler;
import io.github.flemmli97.flan.platform.integration.permissions.PermissionNodeHandler;
import io.github.flemmli97.flan.player.EnumEditMode;
import io.github.flemmli97.flan.player.PlayerClaimData;
import io.github.flemmli97.flan.player.display.EnumDisplayType;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockUpdatePacket;
import net.minecraft.network.protocol.game.ClientboundContainerSetSlotPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.Set;

public class ItemInteractEvents {

    public static InteractionResultHolder<ItemStack> useItem(Player p, Level world, InteractionHand hand) {
        if (!(p instanceof ServerPlayer player) || p.isSpectator())
            return InteractionResultHolder.pass(p.getItemInHand(hand));
        ItemStack stack = player.getItemInHand(hand);
        if (ConfigHandler.isClaimingTool(stack)) {
            HitResult ray = player.pick(64, 0, false);
            if (ray != null && ray.getType() == HitResult.Type.BLOCK) {
                claimLandHandling(player, ((BlockHitResult) ray).getBlockPos());
                return InteractionResultHolder.success(stack);
            }
            return InteractionResultHolder.pass(stack);
        }
        if (ConfigHandler.isInspectionTool(stack)) {
            HitResult ray = player.pick(32, 0, false);
            if (ray != null && ray.getType() == HitResult.Type.BLOCK) {
                inspect(player, ((BlockHitResult) ray).getBlockPos());
                return InteractionResultHolder.success(stack);
            }
            return InteractionResultHolder.pass(stack);
        }

        ClaimStorage storage = ClaimStorage.get((ServerLevel) world);
        BlockPos pos = player.blockPosition();
        BlockHitResult hitResult = getPlayerHitResult(world, player, ClipContext.Fluid.SOURCE_ONLY);
        if (hitResult.getType() == HitResult.Type.BLOCK) {
            pos = new BlockPlaceContext(player, hand, stack, hitResult).getClickedPos();
        }
        IPermissionContainer claim = storage.getForPermissionCheck(pos);
        if (claim == null)
            return InteractionResultHolder.pass(stack);
        ClaimPermission perm = ObjectToPermissionMap.getFromItem(stack.getItem());
        if (perm != null) {
            boolean success = claim.canInteract(player, perm, pos, true);
            if (success)
                return InteractionResultHolder.pass(stack);
            if (perm == PermissionRegistry.PLACE) {
                BlockPos update = pos;
                if (stack.getItem() == Items.LILY_PAD) {
                    BlockHitResult upResult = hitResult.withPosition(hitResult.getBlockPos().above());
                    update = new BlockPlaceContext(new UseOnContext(player, hand, upResult)).getClickedPos();
                }
                player.connection.send(new ClientboundBlockUpdatePacket(update, world.getBlockState(update)));
                PlayerClaimData.get(player).addDisplayClaim(claim, EnumDisplayType.MAIN, player.blockPosition().getY());
                updateHeldItem(player);
            }
            return InteractionResultHolder.fail(stack);
        }
        return InteractionResultHolder.pass(stack);
    }

    private static final Set<Item> blackListedItems = Sets.newHashSet(Items.COMPASS, Items.FILLED_MAP, Items.FIREWORK_ROCKET);

    public static InteractionResult onItemUseBlock(UseOnContext context) {
        if (!(context.getPlayer() instanceof ServerPlayer player) || context.getItemInHand().isEmpty())
            return InteractionResult.PASS;
        ClaimStorage storage = ClaimStorage.get((ServerLevel) context.getLevel());
        BlockPos placePos = new BlockPlaceContext(context).getClickedPos();
        IPermissionContainer claim = storage.getForPermissionCheck(placePos.offset(0, 255, 0));
        if (claim == null)
            return InteractionResult.PASS;
        if (blackListedItems.contains(context.getItemInHand().getItem()))
            return InteractionResult.PASS;
        boolean actualInClaim = !(claim instanceof Claim) || placePos.getY() >= ((Claim) claim).getDimensions()[4];
        ClaimPermission perm = ObjectToPermissionMap.getFromItem(context.getItemInHand().getItem());
        if (perm != null) {
            if (claim.canInteract(player, perm, placePos, false))
                return InteractionResult.PASS;
            else if (actualInClaim) {
                player.displayClientMessage(PermHelper.simpleColoredText(ConfigHandler.langManager.get("noPermissionSimple"), ChatFormatting.DARK_RED), true);
                return InteractionResult.FAIL;
            }
        }
        if (claim.canInteract(player, PermissionRegistry.PLACE, placePos, false)) {
            if (!actualInClaim && context.getItemInHand().getItem() instanceof BlockItem) {
                ((Claim) claim).extendDownwards(placePos);
            }
            return InteractionResult.PASS;
        } else if (actualInClaim) {
            player.displayClientMessage(PermHelper.simpleColoredText(ConfigHandler.langManager.get("noPermissionSimple"), ChatFormatting.DARK_RED), true);
            BlockState other = context.getLevel().getBlockState(placePos.above());
            player.connection.send(new ClientboundBlockUpdatePacket(placePos.above(), other));
            PlayerClaimData.get(player).addDisplayClaim(claim, EnumDisplayType.MAIN, player.blockPosition().getY());
            updateHeldItem(player);
            return InteractionResult.FAIL;
        }
        return InteractionResult.PASS;
    }

    /**
     * -2 == Main inventory update
     */
    private static void updateHeldItem(ServerPlayer player) {
        player.connection.send(new ClientboundContainerSetSlotPacket(-2, 0, player.getInventory().selected, player.getInventory().getSelected()));
        player.connection.send(new ClientboundContainerSetSlotPacket(-2, 0, 40, player.getInventory().getItem(40)));
    }

    private static boolean cantClaimInWorld(ServerLevel world) {
        for (String s : ConfigHandler.config.blacklistedWorlds) {
            if (s.equals(world.dimension().location().toString())) {
                return true;
            }
        }
        return false;
    }

    public static boolean canClaimWorld(ServerLevel world, ServerPlayer player) {
        PlayerClaimData data = PlayerClaimData.get(player);
        if (data.isAdminIgnoreClaim())
            return true;
        if (ConfigHandler.config.worldWhitelist) {
            if (!cantClaimInWorld(player.getLevel())) {
                player.displayClientMessage(PermHelper.simpleColoredText(ConfigHandler.langManager.get("landClaimDisabledWorld"), ChatFormatting.DARK_RED), false);
                return false;
            }
        } else if (cantClaimInWorld(player.getLevel())) {
            player.displayClientMessage(PermHelper.simpleColoredText(ConfigHandler.langManager.get("landClaimDisabledWorld"), ChatFormatting.DARK_RED), false);
            return false;
        }
        return true;
    }

    public static void claimLandHandling(ServerPlayer player, BlockPos target) {
        if (!PermissionNodeHandler.INSTANCE.perm(player, PermissionNodeHandler.claimCreate, false)) {
            player.displayClientMessage(PermHelper.simpleColoredText(ConfigHandler.langManager.get("noPermission"), ChatFormatting.DARK_RED), true);
            return;
        }
        if (!canClaimWorld(player.getLevel(), player))
            return;
        ClaimStorage storage = ClaimStorage.get(player.getLevel());
        Claim claim = storage.getClaimAt(target.offset(0, 255, 0));
        PlayerClaimData data = PlayerClaimData.get(player);
        if (data.claimCooldown())
            return;
        data.setClaimActionCooldown();
        if (claim != null) {
            if (claim.canInteract(player, PermissionRegistry.EDITCLAIM, target)) {
                if (data.getEditMode() == EnumEditMode.SUBCLAIM) {
                    Claim subClaim = claim.getSubClaim(target);
                    if (subClaim != null && data.currentEdit() == null) {
                        if (subClaim.isCorner(target)) {
                            data.setEditClaim(subClaim, player.blockPosition().getY());
                            data.setEditingCorner(target);
                            player.displayClientMessage(PermHelper.simpleColoredText(ConfigHandler.langManager.get("resizeClaim"), ChatFormatting.GOLD), false);
                        } else {
                            player.displayClientMessage(PermHelper.simpleColoredText(ConfigHandler.langManager.get("cantClaimHere"), ChatFormatting.RED), false);
                        }
                        data.addDisplayClaim(claim, EnumDisplayType.MAIN, player.blockPosition().getY());
                    } else {
                        if (data.currentEdit() != null) {
                            if (!data.editingCorner().equals(target)) {
                                Set<Claim> fl = claim.resizeSubclaim(data.currentEdit(), data.editingCorner(), target);
                                if (!fl.isEmpty()) {
                                    fl.forEach(confl -> data.addDisplayClaim(confl, EnumDisplayType.MAIN, player.blockPosition().getY()));
                                    player.displayClientMessage(PermHelper.simpleColoredText(ConfigHandler.langManager.get("conflictOther"), ChatFormatting.RED), false);
                                }
                                data.setEditClaim(null, 0);
                                data.setEditingCorner(null);
                            }
                        } else if (data.editingCorner() != null) {
                            if (!data.editingCorner().equals(target)) {
                                Set<Claim> fl = claim.tryCreateSubClaim(data.editingCorner(), target);
                                data.addDisplayClaim(claim, EnumDisplayType.MAIN, player.blockPosition().getY());
                                if (!fl.isEmpty()) {
                                    fl.forEach(confl -> data.addDisplayClaim(confl, EnumDisplayType.CONFLICT, player.blockPosition().getY()));
                                    player.displayClientMessage(PermHelper.simpleColoredText(ConfigHandler.langManager.get("conflictOther"), ChatFormatting.RED), false);
                                } else {
                                    player.displayClientMessage(PermHelper.simpleColoredText(ConfigHandler.langManager.get("subClaimCreateSuccess"), ChatFormatting.GOLD), false);
                                }
                                data.setEditingCorner(null);
                            }
                        } else
                            data.setEditingCorner(target);
                    }
                } else {
                    if (claim.isCorner(target)) {
                        data.setEditClaim(claim, player.blockPosition().getY());
                        data.setEditingCorner(target);
                        player.displayClientMessage(PermHelper.simpleColoredText(ConfigHandler.langManager.get("resizeClaim"), ChatFormatting.GOLD), false);
                    } else if (data.currentEdit() != null) {
                        storage.resizeClaim(data.currentEdit(), data.editingCorner(), target, player);
                        data.setEditClaim(null, 0);
                        data.setEditingCorner(null);
                    } else {
                        data.addDisplayClaim(claim, EnumDisplayType.MAIN, player.blockPosition().getY());
                        player.displayClientMessage(PermHelper.simpleColoredText(ConfigHandler.langManager.get("cantClaimHere"), ChatFormatting.RED), false);
                    }
                }
            } else {
                data.addDisplayClaim(claim, EnumDisplayType.MAIN, player.blockPosition().getY());
                player.displayClientMessage(PermHelper.simpleColoredText(ConfigHandler.langManager.get("cantClaimHere"), ChatFormatting.RED), false);
            }
        } else if (data.getEditMode() == EnumEditMode.SUBCLAIM) {
            player.displayClientMessage(PermHelper.simpleColoredText(String.format(ConfigHandler.langManager.get("wrongMode"), data.getEditMode()), ChatFormatting.RED), false);
        } else {
            if (data.currentEdit() != null) {
                storage.resizeClaim(data.currentEdit(), data.editingCorner(), target, player);
                data.setEditClaim(null, 0);
                data.setEditingCorner(null);
            } else if (data.editingCorner() != null) {
                storage.createClaim(data.editingCorner(), target, player);
                data.setEditingCorner(null);
            } else
                data.setEditingCorner(target);
        }
    }

    public static void inspect(ServerPlayer player, BlockPos target) {
        Claim claim = ClaimStorage.get(player.getLevel()).getClaimAt(target);
        PlayerClaimData data = PlayerClaimData.get(player);
        if (data.claimCooldown())
            return;
        data.setClaimActionCooldown();
        if (claim != null) {
            String owner = claim.isAdminClaim() ? "<Admin>" : player.getServer().getProfileCache().get(claim.getOwner()).map(GameProfile::getName).orElse("<UNKOWN>");
            Component text = PermHelper.simpleColoredText(String.format(ConfigHandler.langManager.get("inspectBlockOwner"),
                    owner,
                    target.getX(), target.getY(), target.getZ()), ChatFormatting.GREEN);
            player.displayClientMessage(text, false);
            data.addDisplayClaim(claim, EnumDisplayType.MAIN, player.blockPosition().getY());
        } else
            player.displayClientMessage(PermHelper.simpleColoredText(ConfigHandler.langManager.get("inspectNoClaim"), ChatFormatting.RED), false);
    }

    /**
     * From {@link Item#getPlayerPOVHitResult}
     */
    protected static BlockHitResult getPlayerHitResult(Level level, Player player, ClipContext.Fluid fluidMode) {
        float xRot = player.getXRot();
        float yRot = player.getYRot();
        Vec3 eye = player.getEyePosition();
        float h = Mth.cos(-yRot * Mth.DEG_TO_RAD - Mth.PI);
        float i = Mth.sin(-yRot * Mth.DEG_TO_RAD - Mth.PI);
        float j = -Mth.cos(-xRot * Mth.DEG_TO_RAD);
        float k = Mth.sin(-xRot * Mth.DEG_TO_RAD);
        float l = i * j;
        float n = h * j;
        Vec3 vec32 = eye.add(l * 5.0D, k * 5.0D, n * 5.0D);
        return level.clip(new ClipContext(eye, vec32, net.minecraft.world.level.ClipContext.Block.OUTLINE, fluidMode, player));
    }
}
