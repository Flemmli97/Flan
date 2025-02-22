package io.github.flemmli97.flan.event;

import com.google.common.collect.Sets;
import io.github.flemmli97.flan.api.data.IPermissionContainer;
import io.github.flemmli97.flan.api.permission.BuiltinPermission;
import io.github.flemmli97.flan.api.permission.InteractionOverrideManager;
import io.github.flemmli97.flan.claim.Claim;
import io.github.flemmli97.flan.claim.ClaimStorage;
import io.github.flemmli97.flan.claim.ClaimUtils;
import io.github.flemmli97.flan.config.ConfigHandler;
import io.github.flemmli97.flan.platform.integration.permissions.PermissionNodeHandler;
import io.github.flemmli97.flan.player.PlayerClaimData;
import io.github.flemmli97.flan.player.display.EnumDisplayType;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockUpdatePacket;
import net.minecraft.network.protocol.game.ClientboundContainerSetSlotPacket;
import net.minecraft.resources.ResourceLocation;
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
            BlockPos pos = rayTargetPos(player);
            if (pos != null) {
                claimLandHandling(player, pos);
                return InteractionResultHolder.success(stack);
            }
            return InteractionResultHolder.pass(stack);
        }
        if (ConfigHandler.isInspectionTool(stack)) {
            BlockPos pos = rayTargetPos(player, 32, false);
            if (pos != null) {
                inspect(player, pos);
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
        if (claim instanceof Claim real && real.canUseItem(stack))
            return InteractionResultHolder.pass(stack);
        ResourceLocation perm = InteractionOverrideManager.INSTANCE.getItemUse(stack.getItem());
        if (perm != null) {
            boolean success = claim.canInteract(player, perm, pos, true);
            if (success)
                return InteractionResultHolder.pass(stack);
            if (perm.equals(BuiltinPermission.PLACE)) {
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

    private static final Set<Item> BLACK_LISTED_ITEMS = Sets.newHashSet(Items.COMPASS, Items.FILLED_MAP, Items.FIREWORK_ROCKET);

    public static InteractionResult onItemUseBlock(UseOnContext context) {
        if (!(context.getPlayer() instanceof ServerPlayer player) || context.getItemInHand().isEmpty())
            return InteractionResult.PASS;
        ClaimStorage storage = ClaimStorage.get((ServerLevel) context.getLevel());
        BlockPos interactPos = context.getClickedPos();
        InteractionResult interact = itemUseOn(context.getLevel(), player, storage, interactPos, context.getItemInHand());
        if (interact != InteractionResult.PASS)
            return interact;
        BlockPos placePos = new BlockPlaceContext(context).getClickedPos();
        return itemUseOn(context.getLevel(), player, storage, placePos, context.getItemInHand());
    }

    private static InteractionResult itemUseOn(Level level, ServerPlayer player, ClaimStorage storage, BlockPos placePos, ItemStack stack) {
        IPermissionContainer claim = storage.getForPermissionCheck(placePos);
        Claim column = storage.getForPermissionCheck(new BlockPos(placePos.getX(), level.getMaxBuildHeight(), placePos.getZ()))
                instanceof Claim real ? real : null;
        if (claim == null)
            return InteractionResult.PASS;
        if (BLACK_LISTED_ITEMS.contains(stack.getItem()))
            return InteractionResult.PASS;
        if (claim instanceof Claim real && real.canUseItem(stack))
            return InteractionResult.PASS;
        ResourceLocation perm = InteractionOverrideManager.INSTANCE.getItemUse(stack.getItem());
        if (perm == null) {
            if (stack.has(DataComponents.JUKEBOX_PLAYABLE))
                perm = BuiltinPermission.JUKEBOX;
        }
        if (perm != null) {
            if (claim.canInteract(player, perm, placePos, false)) {
                if (column != null && stack.getItem() instanceof BlockItem) {
                    column.extendDownwards(placePos);
                }
                return InteractionResult.PASS;
            } else {
                player.displayClientMessage(ClaimUtils.translatedText("flan.noPermissionSimple", ChatFormatting.DARK_RED), true);
                return InteractionResult.FAIL;
            }
        }
        if (claim.canInteract(player, BuiltinPermission.PLACE, placePos, false)) {
            if (column != null && stack.getItem() instanceof BlockItem) {
                column.extendDownwards(placePos);
            }
            return InteractionResult.PASS;
        }
        player.displayClientMessage(ClaimUtils.translatedText("flan.noPermissionSimple", ChatFormatting.DARK_RED), true);
        BlockState other = level.getBlockState(placePos.above());
        player.connection.send(new ClientboundBlockUpdatePacket(placePos.above(), other));
        PlayerClaimData.get(player).addDisplayClaim(claim, EnumDisplayType.MAIN, player.blockPosition().getY());
        updateHeldItem(player);
        return InteractionResult.FAIL;
    }

    /**
     * -2 == Main inventory update
     */
    private static void updateHeldItem(ServerPlayer player) {
        player.connection.send(new ClientboundContainerSetSlotPacket(-2, 0, player.getInventory().selected, player.getInventory().getSelected()));
        player.connection.send(new ClientboundContainerSetSlotPacket(-2, 0, 40, player.getInventory().getItem(40)));
    }

    private static boolean cantClaimInWorld(ServerLevel world) {
        for (String s : ConfigHandler.CONFIG.blacklistedWorlds) {
            if (s.equals(world.dimension().location().toString())) {
                return true;
            }
        }
        return false;
    }

    public static boolean canClaimWorld(ServerLevel level, ServerPlayer player) {
        PlayerClaimData data = PlayerClaimData.get(player);
        if (data.isAdminIgnoreClaim())
            return true;
        if (ConfigHandler.CONFIG.worldWhitelist) {
            if (!cantClaimInWorld(level)) {
                player.displayClientMessage(ClaimUtils.translatedText("flan.landClaimDisabledWorld", ChatFormatting.DARK_RED), false);
                return false;
            }
        } else if (cantClaimInWorld(level)) {
            player.displayClientMessage(ClaimUtils.translatedText("flan.landClaimDisabledWorld", ChatFormatting.DARK_RED), false);
            return false;
        }
        return true;
    }

    public static BlockPos rayTargetPos(ServerPlayer player) {
        PlayerClaimData data = PlayerClaimData.get(player);
        return rayTargetPos(player, data.claimingRange, data.getClaimMode().is3d && data.editingCorner() != null);
    }

    public static BlockPos rayTargetPos(ServerPlayer player, int range, boolean allowMiss) {
        HitResult ray = player.pick(range, 0, false);
        if (ray instanceof BlockHitResult res) {
            if (allowMiss) {
                return res.getBlockPos();
            }
            return res.getType() != HitResult.Type.MISS ? res.getBlockPos() : null;
        }
        return null;
    }

    public static boolean canPlayerClaim(ServerLevel level, ServerPlayer player) {
        PlayerClaimData data = PlayerClaimData.get(player);
        if (data.isAdminIgnoreClaim())
            return true;
        if (!PermissionNodeHandler.INSTANCE.perm(player, PermissionNodeHandler.claimCreate, false)) {
            return false;
        }
        if (ConfigHandler.CONFIG.worldWhitelist) {
            return cantClaimInWorld(level);
        }
        return !cantClaimInWorld(level);
    }

    public static void claimLandHandling(ServerPlayer player, BlockPos target) {
        if (!PermissionNodeHandler.INSTANCE.perm(player, PermissionNodeHandler.claimCreate, false)) {
            player.displayClientMessage(ClaimUtils.translatedText("flan.noPermission", ChatFormatting.DARK_RED), true);
            return;
        }
        if (!canClaimWorld(player.serverLevel(), player))
            return;
        ClaimStorage storage = ClaimStorage.get(player.serverLevel());
        Claim claim = storage.getClaimAt(target);
        if (claim == null)
            claim = storage.getClaimAt(new BlockPos(target.getX(), player.serverLevel().getMaxBuildHeight(), target.getZ()));
        PlayerClaimData data = PlayerClaimData.get(player);
        if (data.claimCooldown())
            return;
        data.setClaimActionCooldown();
        if (claim != null) {
            if (claim.canInteract(player, BuiltinPermission.EDITCLAIM, target)) {
                if (data.getClaimMode().isSubclaim) {
                    Claim subClaim = claim.getSubClaim(target);
                    if (subClaim != null && data.currentEdit() == null) {
                        if (subClaim.isCorner(target)) {
                            data.setEditClaim(subClaim, player.blockPosition().getY());
                            data.setEditingCorner(target);
                            player.displayClientMessage(ClaimUtils.translatedText("flan.resizeClaim", ChatFormatting.GOLD), false);
                        } else {
                            player.displayClientMessage(ClaimUtils.translatedText("flan.cantClaimHere", ChatFormatting.RED), false);
                        }
                        data.addDisplayClaim(claim, EnumDisplayType.MAIN, player.blockPosition().getY());
                    } else {
                        if (data.currentEdit() != null) {
                            if (!data.editingCorner().equals(target)) {
                                Set<Claim> fl = claim.resizeSubclaim(data.currentEdit(), data.editingCorner(), target);
                                if (!fl.isEmpty()) {
                                    fl.forEach(confl -> data.addDisplayClaim(confl, EnumDisplayType.MAIN, player.blockPosition().getY()));
                                    player.displayClientMessage(ClaimUtils.translatedText("flan.conflictOther", ChatFormatting.RED), false);
                                }
                                data.setEditClaim(null, 0);
                                data.setEditingCorner(null);
                            }
                        } else if (data.editingCorner() != null) {
                            if (!data.editingCorner().equals(target)) {
                                Set<Claim> fl = claim.tryCreateSubClaim(data.editingCorner(), target, data.getClaimMode().is3d);
                                data.addDisplayClaim(claim, EnumDisplayType.MAIN, player.blockPosition().getY());
                                if (!fl.isEmpty()) {
                                    fl.forEach(confl -> data.addDisplayClaim(confl, EnumDisplayType.CONFLICT, player.blockPosition().getY()));
                                    player.displayClientMessage(ClaimUtils.translatedText("flan.conflictOther", ChatFormatting.RED), false);
                                } else {
                                    player.displayClientMessage(ClaimUtils.translatedText("flan.subClaimCreateSuccess", ChatFormatting.GOLD), false);
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
                        player.displayClientMessage(ClaimUtils.translatedText("flan.resizeClaim", ChatFormatting.GOLD), false);
                    } else if (data.currentEdit() != null) {
                        storage.resizeClaim(data.currentEdit(), data.editingCorner(), target, player);
                        data.setEditClaim(null, 0);
                        data.setEditingCorner(null);
                    } else {
                        data.addDisplayClaim(claim, EnumDisplayType.MAIN, player.blockPosition().getY());
                        player.displayClientMessage(ClaimUtils.translatedText("flan.cantClaimHere", ChatFormatting.RED), false);
                    }
                }
            } else {
                data.addDisplayClaim(claim, EnumDisplayType.MAIN, player.blockPosition().getY());
                player.displayClientMessage(ClaimUtils.translatedText("flan.cantClaimHere", ChatFormatting.RED), false);
            }
        } else if (data.getClaimMode().isSubclaim) {
            player.displayClientMessage(ClaimUtils.translatedText("flan.wrongMode",
                    Component.translatable(data.getClaimMode().translationKey)
                            .withStyle(ChatFormatting.AQUA), ChatFormatting.RED), false);
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
        Claim claim = ClaimStorage.get(player.serverLevel()).getClaimAt(target);
        PlayerClaimData data = PlayerClaimData.get(player);
        if (data.claimCooldown())
            return;
        data.setClaimActionCooldown();
        if (claim != null) {
            String owner = claim.isAdminClaim() ? "<Admin>" : ClaimUtils.fetchUsername(claim.getOwner(), player.level().getServer()).orElse(claim.getOwner().toString());
            Component text = ClaimUtils.translatedText("flan.inspectBlockOwner",
                    owner, target.getX(), target.getY(), target.getZ(), ChatFormatting.GREEN);
            player.displayClientMessage(text, false);
            data.addDisplayClaim(claim, EnumDisplayType.MAIN, player.blockPosition().getY());
        } else
            player.displayClientMessage(ClaimUtils.translatedText("flan.inspectNoClaim", ChatFormatting.RED), false);
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
