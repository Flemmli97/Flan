package io.github.flemmli97.flan.event;

import io.github.flemmli97.flan.api.ClaimPermission;
import io.github.flemmli97.flan.api.PermissionRegistry;
import io.github.flemmli97.flan.claim.ClaimStorage;
import io.github.flemmli97.flan.claim.IPermissionContainer;
import io.github.flemmli97.flan.claim.ObjectToPermissionMap;
import io.github.flemmli97.flan.config.ConfigHandler;
import io.github.flemmli97.flan.gui.LockedLecternScreenHandler;
import io.github.flemmli97.flan.player.EnumDisplayType;
import io.github.flemmli97.flan.player.PlayerClaimData;
import net.minecraft.block.BlockState;
import net.minecraft.block.DoorBlock;
import net.minecraft.block.InventoryProvider;
import net.minecraft.block.LecternBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.LecternBlockEntity;
import net.minecraft.block.enums.DoubleBlockHalf;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.packet.s2c.play.BlockUpdateS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;

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
            Identifier id = Registry.BLOCK.getId(state.getBlock());
            if (alwaysAllowBlock(id, world.getBlockEntity(pos)))
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
            boolean emptyHand = !player.getMainHandStack().isEmpty() || !player.getOffHandStack().isEmpty();
            boolean cancelBlockInteract = player.shouldCancelInteraction() && emptyHand;
            if (!cancelBlockInteract) {
                BlockState state = world.getBlockState(hitResult.getBlockPos());
                Identifier id = Registry.BLOCK.getId(state.getBlock());
                BlockEntity blockEntity = world.getBlockEntity(hitResult.getBlockPos());
                if (alwaysAllowBlock(id, blockEntity))
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
                if (blockEntity != null) {
                    if (blockEntity instanceof LecternBlockEntity lectern) {
                        if (claim.canInteract(player, PermissionRegistry.LECTERNTAKE, hitResult.getBlockPos(), false))
                            return ActionResult.PASS;
                        if (state.get(LecternBlock.HAS_BOOK))
                            LockedLecternScreenHandler.create(player, lectern);
                        return ActionResult.FAIL;
                    }
                    if (!ConfigHandler.config.lenientBlockEntityCheck || blockEntity instanceof Inventory || blockEntity instanceof InventoryProvider) {
                        if (claim.canInteract(player, PermissionRegistry.OPENCONTAINER, hitResult.getBlockPos(), true))
                            return ActionResult.PASS;
                        PlayerClaimData.get(player).addDisplayClaim(claim, EnumDisplayType.MAIN, player.getBlockPos().getY());
                        return ActionResult.FAIL;
                    }
                }
            }
        }
        return ActionResult.PASS;
    }

    public static boolean alwaysAllowBlock(Identifier id, BlockEntity blockEntity) {
        return ConfigHandler.config.ignoredBlocks.contains(id.toString())
                || (blockEntity != null
                && ConfigHandler.config.blockEntityTagIgnore.stream().anyMatch(blockEntity.writeNbt(new NbtCompound())::contains));
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
        if (entity instanceof ServerPlayerEntity player) {
            ClaimPermission perm = ObjectToPermissionMap.getFromBlock(landedState.getBlock());
            if (perm != PermissionRegistry.TRAMPLE)
                return false;
            ClaimStorage storage = ClaimStorage.get((ServerWorld) entity.world);
            IPermissionContainer claim = storage.getForPermissionCheck(landedPosition);
            if (claim == null)
                return false;
            return !claim.canInteract(player, perm, landedPosition, true);
        } else if (entity instanceof ProjectileEntity projectile) {
            Entity owner = projectile.getOwner();
            if (owner instanceof ServerPlayerEntity player) {
                ClaimPermission perm = ObjectToPermissionMap.getFromBlock(landedState.getBlock());
                if (perm != PermissionRegistry.TRAMPLE)
                    return false;
                ClaimStorage storage = ClaimStorage.get((ServerWorld) entity.world);
                IPermissionContainer claim = storage.getForPermissionCheck(landedPosition);
                return !claim.canInteract(player, perm, landedPosition, true);
            }
        }
        return false;
    }

    public static boolean canBreakTurtleEgg(World world, BlockPos pos, Entity entity) {
        if (world.isClient)
            return false;
        ServerWorld serverWorld = (ServerWorld) world;
        if (entity instanceof ServerPlayerEntity player) {
            ClaimStorage storage = ClaimStorage.get(serverWorld);
            IPermissionContainer claim = storage.getForPermissionCheck(pos);
            if (claim == null)
                return false;
            return !claim.canInteract(player, PermissionRegistry.TRAMPLE, pos, true);
        } else if (entity instanceof ProjectileEntity projectile) {
            Entity owner = projectile.getOwner();
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
