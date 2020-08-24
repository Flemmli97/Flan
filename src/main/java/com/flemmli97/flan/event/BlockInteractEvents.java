package com.flemmli97.flan.event;

import com.flemmli97.flan.claim.Claim;
import com.flemmli97.flan.claim.ClaimStorage;
import com.flemmli97.flan.claim.EnumPermission;
import com.flemmli97.flan.claim.BlockToPermissionMap;
import com.flemmli97.flan.claim.PermHelper;
import com.flemmli97.flan.config.ConfigHandler;
import com.flemmli97.flan.player.EnumDisplayType;
import com.flemmli97.flan.player.PlayerClaimData;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.LockableContainerBlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.ToolItem;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

public class BlockInteractEvents {

    public static ActionResult breakBlocks(PlayerEntity p, World world, Hand hand, BlockPos pos, Direction dir) {
        if (world.isClient)
            return ActionResult.PASS;
        ServerPlayerEntity player = (ServerPlayerEntity) p;
        ClaimStorage storage = ClaimStorage.get((ServerWorld) world);
        Claim claim = storage.getClaimAt(pos);
        if (claim != null) {
            if (!claim.canInteract(player, EnumPermission.BREAK, pos, true)) {
                PlayerClaimData.get(player).addDisplayClaim(claim, EnumDisplayType.MAIN, player.getBlockPos().getY());
                return ActionResult.SUCCESS;
            }
        }
        return ActionResult.PASS;
    }

    //Right click block
    public static ActionResult useBlocks(PlayerEntity p, World world, Hand hand, BlockHitResult hitResult) {
        if (world.isClient)
            return ActionResult.PASS;
        ServerPlayerEntity player = (ServerPlayerEntity) p;
        ItemStack stack = player.getStackInHand(hand);
        if (stack.getItem() == ConfigHandler.config.claimingItem) {
            ItemInteractEvents.claimLandHandling(player, hitResult.getBlockPos());
            return ActionResult.SUCCESS;
        }
        if (stack.getItem() == ConfigHandler.config.inspectionItem) {
            ItemInteractEvents.inspect(player, hitResult.getBlockPos());
            return ActionResult.SUCCESS;
        }
        ClaimStorage storage = ClaimStorage.get((ServerWorld) world);
        Claim claim = storage.getClaimAt(hitResult.getBlockPos());
        if (claim != null) {
            boolean emptyHand = !player.getMainHandStack().isEmpty() || !player.getOffHandStack().isEmpty();
            boolean cancelBlockInteract = player.shouldCancelInteraction() && emptyHand;
            if (!cancelBlockInteract) {
                BlockState state = world.getBlockState(hitResult.getBlockPos());
                BlockEntity blockEntity = world.getBlockEntity(hitResult.getBlockPos());
                if (blockEntity != null) {
                    if (blockEntity instanceof LockableContainerBlockEntity) {
                        if(claim.canInteract(player, EnumPermission.OPENCONTAINER, hitResult.getBlockPos(), true))
                            return ActionResult.PASS;
                        PlayerClaimData.get(player).addDisplayClaim(claim, EnumDisplayType.MAIN, player.getBlockPos().getY());
                        return ActionResult.FAIL;
                    }
                }
                EnumPermission perm = BlockToPermissionMap.getFromBlock(state.getBlock());
                if (perm != null) {
                    if(claim.canInteract(player, perm, hitResult.getBlockPos(), true))
                        return ActionResult.PASS;
                    PlayerClaimData.get(player).addDisplayClaim(claim, EnumDisplayType.MAIN, player.getBlockPos().getY());
                    return ActionResult.FAIL;
                }
            }
            BlockPos placePos = hitResult.getBlockPos().offset(hitResult.getSide());
            claim = storage.getClaimAt(placePos);
            if(claim==null)
                return ActionResult.PASS;
            if (stack.getItem() instanceof BlockItem || stack.getItem() instanceof ToolItem || stack.getItem() == Items.ARMOR_STAND) {
                if(claim.canInteract(player, EnumPermission.PLACE, placePos, true))
                    return ActionResult.PASS;
                PlayerClaimData.get(player).addDisplayClaim(claim, EnumDisplayType.MAIN, player.getBlockPos().getY());
                return ActionResult.FAIL;
            }
        }
        return ActionResult.PASS;
    }

    public static boolean blockCollisionEntity(BlockState state, World world, BlockPos pos, Entity entity) {
        if (entity.world.isClient)
            return false;
        if (entity instanceof ServerPlayerEntity) {
            EnumPermission perm = BlockToPermissionMap.getFromBlock(state.getBlock());
            if (perm != EnumPermission.PRESSUREPLATE && perm != EnumPermission.PORTAL)
                return false;
            ClaimStorage storage = ClaimStorage.get((ServerWorld) world);
            Claim claim = storage.getClaimAt(pos);
            if (claim != null)
                return !claim.canInteract((ServerPlayerEntity) entity, perm, pos, true);
        } else if (entity instanceof ProjectileEntity) {
            EnumPermission perm = BlockToPermissionMap.getFromBlock(state.getBlock());
            if (perm != EnumPermission.PRESSUREPLATE && perm != EnumPermission.BUTTONLEVER)
                return false;
            Entity owner = ((ProjectileEntity) entity).getOwner();
            if (owner instanceof ServerPlayerEntity) {
                ClaimStorage storage = ClaimStorage.get((ServerWorld) world);
                Claim claim = storage.getClaimAt(pos);
                if (claim != null)
                    return !claim.canInteract((ServerPlayerEntity) owner, perm, pos, true);
            }
        }
        return false;
    }

    public static boolean entityFall(Entity entity, double heightDifference, boolean onGround, BlockState landedState, BlockPos landedPosition) {
        if (entity.world.isClient)
            return false;
        if (entity instanceof ServerPlayerEntity) {
            ClaimStorage storage = ClaimStorage.get((ServerWorld) entity.world);
            Claim claim = storage.getClaimAt(landedPosition);
            EnumPermission perm = BlockToPermissionMap.getFromBlock(landedState.getBlock());
            if (perm == EnumPermission.TRAMPLE)
                return !claim.canInteract((ServerPlayerEntity) entity, perm, landedPosition, true);
        } else if (entity instanceof ProjectileEntity) {
            Entity owner = ((ProjectileEntity) entity).getOwner();
            if (owner instanceof ServerPlayerEntity) {
                ClaimStorage storage = ClaimStorage.get((ServerWorld) entity.world);
                Claim claim = storage.getClaimAt(landedPosition);
                EnumPermission perm = BlockToPermissionMap.getFromBlock(landedState.getBlock());
                if (perm == EnumPermission.TRAMPLE)
                    return !claim.canInteract((ServerPlayerEntity) owner, perm, landedPosition, true);
            }
        }
        return false;
    }

    public static boolean turtleEggHandle(World world, BlockPos pos, Entity entity) {
        if (world.isClient)
            return false;
        ServerWorld serverWorld = (ServerWorld) world;
        if (entity instanceof ServerPlayerEntity) {
            ClaimStorage storage = ClaimStorage.get(serverWorld);
            Claim claim = storage.getClaimAt(pos);
            return !claim.canInteract((ServerPlayerEntity) entity, EnumPermission.TRAMPLE, pos, true);
        } else if (entity instanceof ProjectileEntity) {
            Entity owner = ((ProjectileEntity) entity).getOwner();
            if (owner instanceof ServerPlayerEntity) {
                ClaimStorage storage = ClaimStorage.get(serverWorld);
                Claim claim = storage.getClaimAt(pos);
                return !claim.canInteract((ServerPlayerEntity) owner, EnumPermission.TRAMPLE, pos, true);
            }
        } else if (entity instanceof ItemEntity) {
            Entity owner = serverWorld.getEntity(((ItemEntity) entity).getThrower());
            if (owner instanceof ServerPlayerEntity) {
                ClaimStorage storage = ClaimStorage.get(serverWorld);
                Claim claim = storage.getClaimAt(pos);
                return !claim.canInteract((ServerPlayerEntity) owner, EnumPermission.TRAMPLE, pos, true);
            }
        }
        return false;
    }
}
