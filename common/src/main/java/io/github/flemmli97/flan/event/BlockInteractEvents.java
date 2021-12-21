package io.github.flemmli97.flan.event;

import io.github.flemmli97.flan.CrossPlatformStuff;
import io.github.flemmli97.flan.api.data.IPermissionContainer;
import io.github.flemmli97.flan.api.permission.ClaimPermission;
import io.github.flemmli97.flan.api.permission.ObjectToPermissionMap;
import io.github.flemmli97.flan.api.permission.PermissionRegistry;
import io.github.flemmli97.flan.claim.ClaimStorage;
import io.github.flemmli97.flan.config.ConfigHandler;
import io.github.flemmli97.flan.gui.LockedLecternScreenHandler;
import io.github.flemmli97.flan.player.EnumDisplayType;
import io.github.flemmli97.flan.player.PlayerClaimData;
import net.minecraft.block.BlockState;
import net.minecraft.block.DoorBlock;
import net.minecraft.block.LecternBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.LecternBlockEntity;
import net.minecraft.block.enums.DoubleBlockHalf;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.packet.s2c.play.BlockUpdateS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

import java.util.List;

public class BlockInteractEvents {

    public static ActionResult startBreakBlocks(PlayerEntity player, World world, Hand hand, BlockPos pos, Direction direction) {
        return breakBlocks(world, player, pos, world.getBlockState(pos), world.getBlockEntity(pos)) ? ActionResult.PASS : ActionResult.FAIL;
    }

    public static boolean breakBlocks(World world, PlayerEntity p, BlockPos pos, BlockState state, BlockEntity tile) {
        if (world.isClient || p.isSpectator())
            return true;
        ServerPlayerEntity player = (ServerPlayerEntity) p;
        ClaimStorage storage = ClaimStorage.get((ServerWorld) world);
        IPermissionContainer claim = storage.getForPermissionCheck(pos);
        if (claim != null) {
            Identifier id = CrossPlatformStuff.registryBlocks().getIDFrom(state.getBlock());
            if (contains(id, world.getBlockEntity(pos), ConfigHandler.config.breakBlockBlacklist, ConfigHandler.config.breakBETagBlacklist))
                return true;
            if (!claim.canInteract(player, PermissionRegistry.BREAK, pos, true)) {
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
            BlockState state = world.getBlockState(hitResult.getBlockPos());
            Identifier id = CrossPlatformStuff.registryBlocks().getIDFrom(state.getBlock());
            BlockEntity blockEntity = world.getBlockEntity(hitResult.getBlockPos());
            if (contains(id, blockEntity, ConfigHandler.config.interactBlockBlacklist, ConfigHandler.config.interactBETagBlacklist))
                return ActionResult.PASS;
            ClaimPermission perm = ObjectToPermissionMap.getFromBlock(state.getBlock());
            if (perm == PermissionRegistry.PROJECTILES)
                perm = PermissionRegistry.OPENCONTAINER;
            //Pressureplate handled elsewhere
            if (perm != null && perm != PermissionRegistry.PRESSUREPLATE) {
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
            if (blockEntity != null && !player.shouldCancelInteraction() && !stack.isEmpty()) {
                if (blockEntity instanceof LecternBlockEntity) {
                    if (claim.canInteract(player, PermissionRegistry.LECTERNTAKE, hitResult.getBlockPos(), false))
                        return ActionResult.PASS;
                    if (state.get(LecternBlock.HAS_BOOK))
                        LockedLecternScreenHandler.create(player, (LecternBlockEntity) blockEntity);
                    return ActionResult.FAIL;
                }
                if (!ConfigHandler.config.lenientBlockEntityCheck || CrossPlatformStuff.isInventoryTile(blockEntity)) {
                    if (claim.canInteract(player, PermissionRegistry.OPENCONTAINER, hitResult.getBlockPos(), true))
                        return ActionResult.PASS;
                    PlayerClaimData.get(player).addDisplayClaim(claim, EnumDisplayType.MAIN, player.getBlockPos().getY());
                    return ActionResult.FAIL;
                }
            }
            ActionResult res = ItemInteractEvents.onItemUseBlock(new ItemUsageContext(player, hand, hitResult));
            if (claim.canInteract(player, PermissionRegistry.INTERACTBLOCK, hitResult.getBlockPos(), false) || res == ActionResult.FAIL) {
                if (res == ActionResult.FAIL)
                    PlayerClaimData.get(player).addDisplayClaim(claim, EnumDisplayType.MAIN, player.getBlockPos().getY());
                return res;
            }
            return ActionResult.PASS;
        }
        return ActionResult.PASS;
    }

    public static boolean contains(Identifier id, BlockEntity blockEntity, List<String> idList, List<String> tagList) {
        if (idList.contains(id.getNamespace())
                || idList.contains(id.toString()))
            return true;
        if (blockEntity != null && !tagList.isEmpty()) {
            CompoundTag nbt = blockEntity.toTag(new CompoundTag());
            return tagList.stream().anyMatch(tag -> CrossPlatformStuff.blockDataContains(nbt, tag));
        }
        return false;
    }

    public static boolean cancelEntityBlockCollision(BlockState state, World world, BlockPos pos, Entity entity) {
        if (world.isClient)
            return false;
        ServerPlayerEntity player = null;
        if (entity instanceof ServerPlayerEntity)
            player = (ServerPlayerEntity) entity;
        else if (entity instanceof ProjectileEntity) {
            Entity owner = ((ProjectileEntity) entity).getOwner();
            if (owner instanceof ServerPlayerEntity)
                player = (ServerPlayerEntity) owner;
        } else if (entity instanceof ItemEntity) {
            Entity owner = ((ServerWorld) world).getEntity(((ItemEntity) entity).getThrower());
            if (owner instanceof ServerPlayerEntity)
                player = (ServerPlayerEntity) owner;
        }
        if (player == null)
            return false;
        ClaimPermission perm = ObjectToPermissionMap.getFromBlock(state.getBlock());
        if (perm == null)
            return false;
        if (perm != PermissionRegistry.PRESSUREPLATE && perm != PermissionRegistry.PORTAL)
            return false;
        ClaimStorage storage = ClaimStorage.get((ServerWorld) world);
        IPermissionContainer claim = storage.getForPermissionCheck(pos);
        if (claim != null)
            return !claim.canInteract(player, perm, pos, false);
        return false;
    }

    public static boolean preventFallOn(Entity entity, double heightDifference, boolean onGround, BlockState landedState, BlockPos landedPosition) {
        if (entity.world.isClient)
            return false;
        if (entity instanceof ServerPlayerEntity) {
            ClaimPermission perm = ObjectToPermissionMap.getFromBlock(landedState.getBlock());
            if (perm != PermissionRegistry.TRAMPLE)
                return false;
            ClaimStorage storage = ClaimStorage.get((ServerWorld) entity.world);
            IPermissionContainer claim = storage.getForPermissionCheck(landedPosition);
            if (claim == null)
                return false;
            return !claim.canInteract((ServerPlayerEntity) entity, perm, landedPosition, true);
        } else if (entity instanceof ProjectileEntity) {
            Entity owner = ((ProjectileEntity) entity).getOwner();
            if (owner instanceof ServerPlayerEntity) {
                ClaimPermission perm = ObjectToPermissionMap.getFromBlock(landedState.getBlock());
                if (perm != PermissionRegistry.TRAMPLE)
                    return false;
                ClaimStorage storage = ClaimStorage.get((ServerWorld) entity.world);
                IPermissionContainer claim = storage.getForPermissionCheck(landedPosition);
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
            if (claim == null)
                return false;
            return !claim.canInteract((ServerPlayerEntity) entity, PermissionRegistry.TRAMPLE, pos, true);
        } else if (entity instanceof ProjectileEntity) {
            Entity owner = ((ProjectileEntity) entity).getOwner();
            if (owner instanceof ServerPlayerEntity) {
                ClaimStorage storage = ClaimStorage.get(serverWorld);
                IPermissionContainer claim = storage.getForPermissionCheck(pos);
                if (claim == null)
                    return false;
                return !claim.canInteract((ServerPlayerEntity) owner, PermissionRegistry.TRAMPLE, pos, true);
            }
        } else if (entity instanceof ItemEntity) {
            Entity owner = serverWorld.getEntity(((ItemEntity) entity).getThrower());
            if (owner instanceof ServerPlayerEntity) {
                ClaimStorage storage = ClaimStorage.get(serverWorld);
                IPermissionContainer claim = storage.getForPermissionCheck(pos);
                if (claim == null)
                    return false;
                return !claim.canInteract((ServerPlayerEntity) owner, PermissionRegistry.TRAMPLE, pos, true);
            }
        }
        return false;
    }
}
