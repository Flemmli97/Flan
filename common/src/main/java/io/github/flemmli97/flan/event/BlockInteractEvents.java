package io.github.flemmli97.flan.event;

import io.github.flemmli97.flan.api.data.IPermissionContainer;
import io.github.flemmli97.flan.api.permission.ClaimPermission;
import io.github.flemmli97.flan.api.permission.ObjectToPermissionMap;
import io.github.flemmli97.flan.api.permission.PermissionRegistry;
import io.github.flemmli97.flan.claim.ClaimStorage;
import io.github.flemmli97.flan.config.ConfigHandler;
import io.github.flemmli97.flan.gui.LockedLecternScreenHandler;
import io.github.flemmli97.flan.platform.CrossPlatformStuff;
import io.github.flemmli97.flan.player.PlayerClaimData;
import io.github.flemmli97.flan.player.display.EnumDisplayType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockUpdatePacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.LecternBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.LecternBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.phys.BlockHitResult;

import java.util.List;

public class BlockInteractEvents {

    public static InteractionResult startBreakBlocks(Player player, Level world, InteractionHand hand, BlockPos pos, Direction direction) {
        return breakBlocks(world, player, pos, world.getBlockState(pos), world.getBlockEntity(pos)) ? InteractionResult.PASS : InteractionResult.FAIL;
    }

    public static boolean breakBlocks(Level world, Player p, BlockPos pos, BlockState state, BlockEntity tile) {
        if (!(p instanceof ServerPlayer player) || p.isSpectator())
            return true;
        ClaimStorage storage = ClaimStorage.get((ServerLevel) world);
        IPermissionContainer claim = storage.getForPermissionCheck(pos);
        if (claim != null) {
            ResourceLocation id = CrossPlatformStuff.INSTANCE.registryBlocks().getIDFrom(state.getBlock());
            if (contains(id, world.getBlockEntity(pos), ConfigHandler.config.breakBlockBlacklist, ConfigHandler.config.breakBETagBlacklist))
                return true;
            if (!claim.canInteract(player, PermissionRegistry.BREAK, pos, true)) {
                PlayerClaimData.get(player).addDisplayClaim(claim, EnumDisplayType.MAIN, player.blockPosition().getY());
                return false;
            }
        }
        return true;
    }

    //Right click block
    public static InteractionResult useBlocks(Player p, Level world, InteractionHand hand, BlockHitResult hitResult) {
        if (!(p instanceof ServerPlayer player))
            return InteractionResult.PASS;
        ItemStack stack = player.getItemInHand(hand);
        if (stack.getItem() == ConfigHandler.config.claimingItem) {
            ItemInteractEvents.claimLandHandling(player, hitResult.getBlockPos());
            return InteractionResult.SUCCESS;
        }
        if (stack.getItem() == ConfigHandler.config.inspectionItem) {
            ItemInteractEvents.inspect(player, hitResult.getBlockPos());
            return InteractionResult.SUCCESS;
        }
        ClaimStorage storage = ClaimStorage.get((ServerLevel) world);
        IPermissionContainer claim = storage.getForPermissionCheck(hitResult.getBlockPos());
        if (claim != null) {
            BlockState state = world.getBlockState(hitResult.getBlockPos());
            ResourceLocation id = CrossPlatformStuff.INSTANCE.registryBlocks().getIDFrom(state.getBlock());
            BlockEntity blockEntity = world.getBlockEntity(hitResult.getBlockPos());
            if (contains(id, blockEntity, ConfigHandler.config.interactBlockBlacklist, ConfigHandler.config.interactBETagBlacklist))
                return InteractionResult.PASS;
            ClaimPermission perm = ObjectToPermissionMap.getFromBlock(state.getBlock());
            if (perm == PermissionRegistry.PROJECTILES)
                perm = PermissionRegistry.OPENCONTAINER;
            //Pressureplate handled elsewhere
            if (perm != null && perm != PermissionRegistry.PRESSUREPLATE) {
                if (claim.canInteract(player, perm, hitResult.getBlockPos(), true))
                    return InteractionResult.PASS;
                if (state.getBlock() instanceof DoorBlock) {
                    DoubleBlockHalf half = state.getValue(DoorBlock.HALF);
                    if (half == DoubleBlockHalf.LOWER) {
                        BlockState other = world.getBlockState(hitResult.getBlockPos().above());
                        player.connection.send(new ClientboundBlockUpdatePacket(hitResult.getBlockPos().above(), other));
                    } else {
                        BlockState other = world.getBlockState(hitResult.getBlockPos().below());
                        player.connection.send(new ClientboundBlockUpdatePacket(hitResult.getBlockPos().below(), other));
                    }
                }
                PlayerClaimData.get(player).addDisplayClaim(claim, EnumDisplayType.MAIN, player.blockPosition().getY());
                return InteractionResult.FAIL;
            }
            if (blockEntity != null && !(player.isSecondaryUseActive() && !stack.isEmpty())) {
                if (blockEntity instanceof LecternBlockEntity) {
                    if (claim.canInteract(player, PermissionRegistry.LECTERNTAKE, hitResult.getBlockPos(), false))
                        return InteractionResult.PASS;
                    if (state.getValue(LecternBlock.HAS_BOOK))
                        LockedLecternScreenHandler.create(player, (LecternBlockEntity) blockEntity);
                    return InteractionResult.FAIL;
                }
                if (!ConfigHandler.config.lenientBlockEntityCheck || CrossPlatformStuff.INSTANCE.isInventoryTile(blockEntity)) {
                    if (claim.canInteract(player, PermissionRegistry.OPENCONTAINER, hitResult.getBlockPos(), true))
                        return InteractionResult.PASS;
                    PlayerClaimData.get(player).addDisplayClaim(claim, EnumDisplayType.MAIN, player.blockPosition().getY());
                    return InteractionResult.FAIL;
                }
            }
            boolean shift = player.isSecondaryUseActive() || stack.isEmpty();
            boolean res = claim.canInteract(player, PermissionRegistry.INTERACTBLOCK, hitResult.getBlockPos(), shift);
            if (!res && shift)
                PlayerClaimData.get(player).addDisplayClaim(claim, EnumDisplayType.MAIN, player.blockPosition().getY());
            return res ? InteractionResult.PASS : InteractionResult.FAIL;
        }
        return InteractionResult.PASS;
    }

    public static boolean contains(ResourceLocation id, BlockEntity blockEntity, List<String> idList, List<String> tagList) {
        if (idList.contains(id.getNamespace())
                || idList.contains(id.toString()))
            return true;
        if (blockEntity != null && !tagList.isEmpty()) {
            CompoundTag nbt = blockEntity.saveWithoutMetadata();
            return tagList.stream().anyMatch(tag -> CrossPlatformStuff.INSTANCE.blockDataContains(nbt, tag));
        }
        return false;
    }

    public static boolean cancelEntityBlockCollision(BlockState state, Level world, BlockPos pos, Entity entity) {
        if (world.isClientSide)
            return false;
        ServerPlayer player = null;
        if (entity instanceof ServerPlayer)
            player = (ServerPlayer) entity;
        else if (entity instanceof Projectile) {
            Entity owner = ((Projectile) entity).getOwner();
            if (owner instanceof ServerPlayer)
                player = (ServerPlayer) owner;
        } else if (entity instanceof ItemEntity) {
            Entity owner = ((ItemEntity) entity).getOwner();
            if (owner instanceof ServerPlayer)
                player = (ServerPlayer) owner;
        }
        if (player == null)
            return false;
        ClaimPermission perm = ObjectToPermissionMap.getFromBlock(state.getBlock());
        if (perm == null)
            return false;
        if (perm != PermissionRegistry.PRESSUREPLATE && perm != PermissionRegistry.PORTAL)
            return false;
        ClaimStorage storage = ClaimStorage.get((ServerLevel) world);
        IPermissionContainer claim = storage.getForPermissionCheck(pos);
        if (claim != null)
            return !claim.canInteract(player, perm, pos, false);
        return false;
    }

    public static boolean preventFallOn(Entity entity, double heightDifference, boolean onGround, BlockState landedState, BlockPos landedPosition) {
        if (entity.level.isClientSide)
            return false;
        if (entity instanceof ServerPlayer) {
            ClaimPermission perm = ObjectToPermissionMap.getFromBlock(landedState.getBlock());
            if (perm != PermissionRegistry.TRAMPLE)
                return false;
            ClaimStorage storage = ClaimStorage.get((ServerLevel) entity.level);
            IPermissionContainer claim = storage.getForPermissionCheck(landedPosition);
            if (claim == null)
                return false;
            return !claim.canInteract((ServerPlayer) entity, perm, landedPosition, true);
        } else if (entity instanceof Projectile) {
            Entity owner = ((Projectile) entity).getOwner();
            if (owner instanceof ServerPlayer) {
                ClaimPermission perm = ObjectToPermissionMap.getFromBlock(landedState.getBlock());
                if (perm != PermissionRegistry.TRAMPLE)
                    return false;
                ClaimStorage storage = ClaimStorage.get((ServerLevel) entity.level);
                IPermissionContainer claim = storage.getForPermissionCheck(landedPosition);
                return !claim.canInteract((ServerPlayer) owner, perm, landedPosition, true);
            }
        }
        return false;
    }

    public static boolean canBreakTurtleEgg(Level world, BlockPos pos, Entity entity) {
        if (world.isClientSide)
            return false;
        ServerLevel serverWorld = (ServerLevel) world;
        if (entity instanceof ServerPlayer) {
            ClaimStorage storage = ClaimStorage.get(serverWorld);
            IPermissionContainer claim = storage.getForPermissionCheck(pos);
            if (claim == null)
                return false;
            return !claim.canInteract((ServerPlayer) entity, PermissionRegistry.TRAMPLE, pos, true);
        } else if (entity instanceof Projectile) {
            Entity owner = ((Projectile) entity).getOwner();
            if (owner instanceof ServerPlayer) {
                ClaimStorage storage = ClaimStorage.get(serverWorld);
                IPermissionContainer claim = storage.getForPermissionCheck(pos);
                if (claim == null)
                    return false;
                return !claim.canInteract((ServerPlayer) owner, PermissionRegistry.TRAMPLE, pos, true);
            }
        } else if (entity instanceof ItemEntity) {
            Entity owner = ((ItemEntity) entity).getOwner();
            if (owner instanceof ServerPlayer) {
                ClaimStorage storage = ClaimStorage.get(serverWorld);
                IPermissionContainer claim = storage.getForPermissionCheck(pos);
                if (claim == null)
                    return false;
                return !claim.canInteract((ServerPlayer) owner, PermissionRegistry.TRAMPLE, pos, true);
            }
        }
        return false;
    }
}
