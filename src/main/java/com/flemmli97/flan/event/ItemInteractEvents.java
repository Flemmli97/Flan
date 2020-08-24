package com.flemmli97.flan.event;

import com.flemmli97.flan.claim.Claim;
import com.flemmli97.flan.claim.ClaimStorage;
import com.flemmli97.flan.claim.EnumPermission;
import com.flemmli97.flan.claim.PermHelper;
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
import net.minecraft.util.Formatting;
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
                claimLandHandling(player, ((BlockHitResult) ray).getBlockPos());
                return TypedActionResult.success(stack);
            }
            return TypedActionResult.pass(stack);
        }
        if (stack.getItem() == ConfigHandler.config.inspectionItem) {
            HitResult ray = player.rayTrace(32, 0, false);
            if (ray != null && ray.getType() == HitResult.Type.BLOCK) {
                inspect(player, ((BlockHitResult) ray).getBlockPos());
                return TypedActionResult.success(stack);
            }
            return TypedActionResult.pass(stack);
        }
        ClaimStorage storage = ClaimStorage.get((ServerWorld) world);
        BlockPos pos = player.getBlockPos();
        Claim claim = storage.getClaimAt(pos);
        if (claim == null)
            return TypedActionResult.pass(stack);
        if (stack.getItem() == Items.ENDER_PEARL)
            return claim.canInteract(player, EnumPermission.ENDERPEARL, pos, true)? TypedActionResult.pass(stack) : TypedActionResult.fail(stack);
        if (stack.getItem() instanceof BucketItem)
            return claim.canInteract(player, EnumPermission.BUCKET, pos, true) ? TypedActionResult.pass(stack) : TypedActionResult.fail(stack);
        return TypedActionResult.pass(stack);
    }

    private static boolean cantClaimInWorld(ServerWorld world){
        for(String s : ConfigHandler.config.blacklistedWorlds){
            if(s.equals(world.getRegistryKey().getValue().toString())) {
                return true;
            }
        }
        return false;
    }

    public static void claimLandHandling(ServerPlayerEntity player, BlockPos target){
        if(ConfigHandler.config.worldWhitelist){
            if(!cantClaimInWorld(player.getServerWorld())) {
                player.sendMessage(PermHelper.simpleColoredText(ConfigHandler.lang.landClaimDisabledWorld, Formatting.DARK_RED), false);
                return;
            }
        }
        else if(cantClaimInWorld(player.getServerWorld())) {
            player.sendMessage(PermHelper.simpleColoredText(ConfigHandler.lang.landClaimDisabledWorld, Formatting.DARK_RED), false);
            return;
        }
        ClaimStorage storage = ClaimStorage.get(player.getServerWorld());
        Claim claim = storage.getClaimAt(target);
        PlayerClaimData data = PlayerClaimData.get(player);
        if (claim != null) {
            if (claim.canInteract(player, EnumPermission.EDITCLAIM, target)) {
                if (data.getEditMode() == EnumEditMode.SUBCLAIM) {
                    Claim subClaim = claim.getSubClaim(target);
                    if (subClaim != null && data.currentEdit()==null) {
                        if (subClaim.isCorner(target)) {
                            data.setEditClaim(subClaim);
                            data.setEditingCorner(target);
                            player.sendMessage(PermHelper.simpleColoredText(ConfigHandler.lang.resizeClaim, Formatting.GOLD), false);
                        }
                        else {
                            data.addDisplayClaim(claim, EnumDisplayType.MAIN);
                            player.sendMessage(PermHelper.simpleColoredText(ConfigHandler.lang.cantClaimHere, Formatting.RED), false);
                        }
                    } else {
                        if(data.currentEdit()!=null){
                            boolean fl = claim.resizeSubclaim(data.currentEdit(), data.editingCorner(), target);
                            if(!fl)
                                player.sendMessage(PermHelper.simpleColoredText(ConfigHandler.lang.conflictOther, Formatting.RED), false);
                            data.setEditClaim(null);
                            data.setEditingCorner(null);
                        }
                        else if (data.editingCorner() != null) {
                            boolean fl = claim.tryCreateSubClaim(data.editingCorner(), target);
                            if(!fl)
                                player.sendMessage(PermHelper.simpleColoredText(ConfigHandler.lang.conflictOther, Formatting.RED), false);
                            else{
                                data.addDisplayClaim(claim, EnumDisplayType.MAIN);
                                player.sendMessage(PermHelper.simpleColoredText(ConfigHandler.lang.subClaimCreateSuccess, Formatting.GOLD), false);
                            }
                            data.setEditingCorner(null);
                        } else
                            data.setEditingCorner(target);
                    }
                } else {
                    if (claim.isCorner(target)) {
                        data.setEditClaim(claim);
                        data.setEditingCorner(target);
                        player.sendMessage(PermHelper.simpleColoredText(ConfigHandler.lang.resizeClaim, Formatting.GOLD), false);
                    }
                    else if(data.currentEdit()!=null){
                        storage.resizeClaim(data.currentEdit(), data.editingCorner(), target, player);
                        data.setEditClaim(null);
                        data.setEditingCorner(null);
                    }
                    else {
                        data.addDisplayClaim(claim, EnumDisplayType.MAIN);
                        player.sendMessage(PermHelper.simpleColoredText(ConfigHandler.lang.cantClaimHere, Formatting.RED), false);
                    }
                }
            } else {
                data.addDisplayClaim(claim, EnumDisplayType.MAIN);
                player.sendMessage(PermHelper.simpleColoredText(ConfigHandler.lang.cantClaimHere, Formatting.RED), false);
            }
        }
        else if(data.getEditMode() == EnumEditMode.SUBCLAIM){
            player.sendMessage(PermHelper.simpleColoredText(String.format(ConfigHandler.lang.wrongMode, data.getEditMode()), Formatting.RED), false);
        }
        else {
            if(data.currentEdit()!=null){
                storage.resizeClaim(data.currentEdit(), data.editingCorner(), target, player);
                data.setEditClaim(null);
                data.setEditingCorner(null);
            }
            else if (data.editingCorner() != null) {
                storage.createClaim(data.editingCorner(), target, player);
                data.setEditingCorner(null);
            }
            else
                data.setEditingCorner(target);
        }
    }

    public static void inspect(ServerPlayerEntity player, BlockPos target){
        Claim claim = ClaimStorage.get(player.getServerWorld()).getClaimAt(target);
        if (claim != null) {
            String owner = claim.getOwner()==null?"<Admin>":"<UNKOWN>";
            if(claim.getOwner()!=null) {
                GameProfile prof = player.getServer().getUserCache().getByUuid(claim.getOwner());
                if (prof != null && prof.getName() != null)
                    owner = prof.getName();
            }
            Text text = PermHelper.simpleColoredText(String.format(ConfigHandler.lang.inspectBlockOwner,
                    owner,
                    target.getX(), target.getY(), target.getZ()), Formatting.GREEN);
            player.sendMessage(text, false);
            PlayerClaimData.get(player).addDisplayClaim(claim, EnumDisplayType.MAIN);
        } else
            player.sendMessage(PermHelper.simpleColoredText(ConfigHandler.lang.inspectNoClaim, Formatting.RED), false);
    }
}
