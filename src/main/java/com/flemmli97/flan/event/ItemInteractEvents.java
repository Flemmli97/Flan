package com.flemmli97.flan.event;

import com.flemmli97.flan.claim.Claim;
import com.flemmli97.flan.claim.ClaimStorage;
import com.flemmli97.flan.claim.EnumPermission;
import com.flemmli97.flan.config.ConfigHandler;
import com.flemmli97.flan.player.EnumDisplayType;
import com.flemmli97.flan.player.EnumEditMode;
import com.flemmli97.flan.player.PlayerClaimData;
import com.mojang.authlib.GameProfile;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BucketItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class ItemInteractEvents {

    public static TypedActionResult<ItemStack> useItem(PlayerEntity p, World world, Hand hand) {
        if (world.isClient)
            return TypedActionResult.pass(p.getStackInHand(hand));
        ServerPlayerEntity player = (ServerPlayerEntity) p;
        ItemStack stack = player.getStackInHand(hand);
        if (stack.getItem() == ConfigHandler.config.claimingItem) {
            HitResult ray = player.rayTrace(64, 0, false);
            if (ray != null && ray.getType() == HitResult.Type.BLOCK) {
                BlockHitResult blockRay = (BlockHitResult) ray;
                ClaimStorage storage = ClaimStorage.get((ServerWorld) world);
                Claim claim = storage.getClaimAt(blockRay.getBlockPos());
                PlayerClaimData data = PlayerClaimData.get(player);
                if (claim != null) {
                    if (claim.canInteract(player, EnumPermission.EDITCLAIM, blockRay.getBlockPos())) {
                        if (data.getEditMode() == EnumEditMode.SUBCLAIM) {
                            Claim subClaim = claim.getSubClaim(blockRay.getBlockPos());
                            if (subClaim != null) {
                                if (subClaim.isCorner(blockRay.getBlockPos()))
                                    data.setEditClaim(subClaim);
                            } else {
                                if (data.editingCorner() != null) {
                                    if (data.currentEdit() == null) {
                                        boolean fl = claim.tryCreateSubClaim(data.editingCorner(), blockRay.getBlockPos());
                                    } else {
                                        //subClaim.resizeClaim(data.currentEdit(), data.editingCorner());
                                        data.setEditClaim(null);
                                    }
                                    data.setEditingCorner(null);
                                } else
                                    data.setEditingCorner(blockRay.getBlockPos());
                            }
                        } else {
                            if (claim.isCorner(blockRay.getBlockPos()))
                                data.setEditClaim(claim);
                        }
                    } else {
                        data.addDisplayClaim(claim, EnumDisplayType.MAIN);
                        player.sendMessage(Text.of(ConfigHandler.lang.cantClaimHere), false);
                    }
                } else {
                    if (data.editingCorner() != null) {
                        if (data.currentEdit() == null)
                            storage.createClaim(data.editingCorner(), blockRay.getBlockPos(), player);
                        else {
                            storage.resizeClaim(data.currentEdit(), data.editingCorner());
                            data.setEditClaim(null);
                        }
                        data.setEditingCorner(null);
                    } else
                        data.setEditingCorner(blockRay.getBlockPos());
                }
            }
            return TypedActionResult.success(stack);
        }
        if (stack.getItem() == ConfigHandler.config.inspectionItem) {
            HitResult ray = player.rayTrace(32, 0, false);
            if (ray != null && ray.getType() == HitResult.Type.BLOCK) {
                BlockHitResult blockRay = (BlockHitResult) ray;
                Claim claim = ClaimStorage.get((ServerWorld) world).getClaimAt(new BlockPos(ray.getPos()));
                if (claim != null) {
                    String owner = "<UNKOWN>";
                    GameProfile prof = world.getServer().getUserCache().getByUuid(claim.getOwner());
                    if (prof != null && prof.getName() != null)
                        owner = prof.getName();
                    Text text = Text.of(String.format(ConfigHandler.lang.inspectBlockOwner,
                            owner,
                            blockRay.getBlockPos().getX(), blockRay.getBlockPos().getY(), blockRay.getBlockPos().getZ()));
                    player.sendMessage(text, false);
                    PlayerClaimData.get(player).addDisplayClaim(claim, EnumDisplayType.MAIN);
                } else
                    player.sendMessage(Text.of(ConfigHandler.lang.inspectNoClaim), false);
            }
            return TypedActionResult.success(stack);
        }
        ClaimStorage storage = ClaimStorage.get((ServerWorld) world);
        BlockPos pos = player.getBlockPos();
        Claim claim = storage.getClaimAt(pos);
        if (claim == null)
            return TypedActionResult.pass(stack);
        if (stack.getItem() == Items.ENDER_PEARL)
            return claim.canInteract(player, EnumPermission.ENDERPEARL, pos) ? TypedActionResult.pass(stack) : TypedActionResult.fail(stack);
        if (stack.getItem() instanceof BucketItem)
            return claim.canInteract(player, EnumPermission.BUCKET, pos) ? TypedActionResult.pass(stack) : TypedActionResult.fail(stack);
        return TypedActionResult.pass(stack);
    }
}
