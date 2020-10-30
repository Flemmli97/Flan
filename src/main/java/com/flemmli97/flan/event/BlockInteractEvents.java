package com.flemmli97.flan.event;

import com.flemmli97.flan.claim.BlockToPermissionMap;
import com.flemmli97.flan.claim.Claim;
import com.flemmli97.flan.claim.ClaimStorage;
import com.flemmli97.flan.claim.EnumPermission;
import com.flemmli97.flan.claim.IPermissionContainer;
import com.flemmli97.flan.config.ConfigHandler;
import com.flemmli97.flan.gui.LockedLecternScreenHandler;
import com.flemmli97.flan.player.EnumDisplayType;
import com.flemmli97.flan.player.PlayerClaimData;
import net.minecraft.block.BlockState;
import net.minecraft.block.DoorBlock;
import net.minecraft.block.LecternBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.LecternBlockEntity;
import net.minecraft.block.entity.LockableContainerBlockEntity;
import net.minecraft.block.enums.DoubleBlockHalf;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.s2c.play.BlockUpdateS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class BlockInteractEvents {

    public static boolean breakBlocks(World world, PlayerEntity p, BlockPos pos, BlockState state, BlockEntity tile){
        if (world.isClient || p.isSpectator())
            return true;
        ServerPlayerEntity player = (ServerPlayerEntity) p;
        ClaimStorage storage = ClaimStorage.get((ServerWorld) world);
        IPermissionContainer claim = storage.getForPermissionCheck(pos);
        if (claim != null) {
            if (!claim.canInteract(player, EnumPermission.BREAK, pos, true)) {
                PlayerClaimData.get(player).addDisplayClaim(claim, EnumDisplayType.MAIN, player.getBlockPos().getY());
                return false;
            }
        }
        return true;
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
        IPermissionContainer claim = storage.getForPermissionCheck(hitResult.getBlockPos());
        if (claim != null) {
            boolean emptyHand = !player.getMainHandStack().isEmpty() || !player.getOffHandStack().isEmpty();
            boolean cancelBlockInteract = player.shouldCancelInteraction() && emptyHand;
            if (!cancelBlockInteract) {
                BlockState state = world.getBlockState(hitResult.getBlockPos());
                BlockEntity blockEntity = world.getBlockEntity(hitResult.getBlockPos());
                if (blockEntity != null) {
                    if (blockEntity instanceof LockableContainerBlockEntity) {
                        if (claim.canInteract(player, EnumPermission.OPENCONTAINER, hitResult.getBlockPos(), true))
                            return ActionResult.PASS;
                        PlayerClaimData.get(player).addDisplayClaim(claim, EnumDisplayType.MAIN, player.getBlockPos().getY());
                        return ActionResult.FAIL;
                    }
                    if (blockEntity instanceof LecternBlockEntity) {
                        if (claim.canInteract(player, EnumPermission.LECTERNTAKE, hitResult.getBlockPos(), false))
                            return ActionResult.PASS;
                        if (state.get(LecternBlock.HAS_BOOK))
                            LockedLecternScreenHandler.create(player, (LecternBlockEntity) blockEntity);
                        return ActionResult.FAIL;
                    }
                }
                EnumPermission perm = BlockToPermissionMap.getFromBlock(state.getBlock());
                //Pressureplate handled elsewhere
                if (perm != null && perm != EnumPermission.PRESSUREPLATE) {
                    if (claim.canInteract(player, perm, hitResult.getBlockPos(), true))
                        return ActionResult.PASS;
                    if (state.getBlock() instanceof DoorBlock) {
                        DoubleBlockHalf half = state.get(DoorBlock.HALF);
                        if (half == DoubleBlockHalf.LOWER) {
                            BlockState other = world.getBlockState(hitResult.getBlockPos().up());
                            player.networkHandler.sendPacket(new BlockUpdateS2CPacket(hitResult.getBlockPos().up(), other));
                        } else {
                            BlockState other = world.getBlockState(hitResult.getBlockPos().down());
                            player.networkHandler.sendPacket(new BlockUpdateS2CPacket(hitResult.getBlockPos().down(), other));
                        }
                    }
                    PlayerClaimData.get(player).addDisplayClaim(claim, EnumDisplayType.MAIN, player.getBlockPos().getY());
                    return ActionResult.FAIL;
                }
            }
        }
        return ActionResult.PASS;
    }

    public static boolean cancelEntityBlockCollision(BlockState state, World world, BlockPos pos, Entity entity) {
        if (entity.world.isClient)
            return false;
        if (entity instanceof ServerPlayerEntity) {
            EnumPermission perm = BlockToPermissionMap.getFromBlock(state.getBlock());
            if (perm != EnumPermission.PRESSUREPLATE && perm != EnumPermission.PORTAL)
                return false;
            ClaimStorage storage = ClaimStorage.get((ServerWorld) world);
            IPermissionContainer claim = storage.getForPermissionCheck(pos);
            if (claim != null)
                return !claim.canInteract((ServerPlayerEntity) entity, perm, pos, false);
        } else if (entity instanceof ProjectileEntity) {
            EnumPermission perm = BlockToPermissionMap.getFromBlock(state.getBlock());
            if (perm != EnumPermission.PRESSUREPLATE && perm != EnumPermission.BUTTONLEVER)
                return false;
            Entity owner = ((ProjectileEntity) entity).getOwner();
            if (owner instanceof ServerPlayerEntity) {
                ClaimStorage storage = ClaimStorage.get((ServerWorld) world);
                IPermissionContainer claim = storage.getForPermissionCheck(pos);
                if (claim != null)
                    return !claim.canInteract((ServerPlayerEntity) owner, perm, pos, false);
            }
        }
        return false;
    }

    public static boolean preventFallOn(Entity entity, double heightDifference, boolean onGround, BlockState landedState, BlockPos landedPosition) {
        if (entity.world.isClient)
            return false;
        if (entity instanceof ServerPlayerEntity) {
            ClaimStorage storage = ClaimStorage.get((ServerWorld) entity.world);
            IPermissionContainer claim = storage.getForPermissionCheck(landedPosition);
            if(claim==null)
                return false;
            EnumPermission perm = BlockToPermissionMap.getFromBlock(landedState.getBlock());
            if (perm == EnumPermission.TRAMPLE)
                return !claim.canInteract((ServerPlayerEntity) entity, perm, landedPosition, true);
        } else if (entity instanceof ProjectileEntity) {
            Entity owner = ((ProjectileEntity) entity).getOwner();
            if (owner instanceof ServerPlayerEntity) {
                ClaimStorage storage = ClaimStorage.get((ServerWorld) entity.world);
                IPermissionContainer claim = storage.getForPermissionCheck(landedPosition);
                EnumPermission perm = BlockToPermissionMap.getFromBlock(landedState.getBlock());
                if (perm == EnumPermission.TRAMPLE)
                    return !claim.canInteract((ServerPlayerEntity) owner, perm, landedPosition, true);
            }
        }
        return false;
    }

    public static boolean canBreakTurtleEgg(World world, BlockPos pos, Entity entity) {
        if (world.isClient)
            return false;
        ServerWorld serverWorld = (ServerWorld) world;
        if (entity instanceof ServerPlayerEntity) {
            ClaimStorage storage = ClaimStorage.get(serverWorld);
            IPermissionContainer claim = storage.getForPermissionCheck(pos);
            if(claim==null)
                return false;
            return !claim.canInteract((ServerPlayerEntity) entity, EnumPermission.TRAMPLE, pos, true);
        } else if (entity instanceof ProjectileEntity) {
            Entity owner = ((ProjectileEntity) entity).getOwner();
            if (owner instanceof ServerPlayerEntity) {
                ClaimStorage storage = ClaimStorage.get(serverWorld);
                IPermissionContainer claim = storage.getForPermissionCheck(pos);
                if(claim==null)
                    return false;
                return !claim.canInteract((ServerPlayerEntity) owner, EnumPermission.TRAMPLE, pos, true);
            }
        } else if (entity instanceof ItemEntity) {
            Entity owner = serverWorld.getEntity(((ItemEntity) entity).getThrower());
            if (owner instanceof ServerPlayerEntity) {
                ClaimStorage storage = ClaimStorage.get(serverWorld);
                IPermissionContainer claim = storage.getForPermissionCheck(pos);
                if(claim==null)
                    return false;
                return !claim.canInteract((ServerPlayerEntity) owner, EnumPermission.TRAMPLE, pos, true);
            }
        }
        return false;
    }
}
