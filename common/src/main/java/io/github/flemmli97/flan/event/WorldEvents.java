package io.github.flemmli97.flan.event;

import io.github.flemmli97.flan.api.data.IPermissionContainer;
import io.github.flemmli97.flan.api.permission.PermissionRegistry;
import io.github.flemmli97.flan.claim.ClaimStorage;
import net.minecraft.block.BlockState;
import net.minecraft.entity.LightningEntity;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.explosion.Explosion;

public class WorldEvents {

    public static void modifyExplosion(Explosion explosion, ServerWorld world) {
        ClaimStorage storage = ClaimStorage.get(world);
        explosion.getAffectedBlocks().removeIf(pos -> {
            IPermissionContainer claim = storage.getForPermissionCheck(pos);
            if (claim != null)
                return !claim.canInteract(null, PermissionRegistry.EXPLOSIONS, pos);
            return false;
        });
    }

    public static boolean pistonCanPush(BlockState state, World world, BlockPos blockPos, Direction direction, Direction pistonDir) {
        if (world.isClient || state.isAir())
            return true;
        BlockPos dirPos = blockPos.offset(direction);
        ClaimStorage storage = ClaimStorage.get((ServerWorld) world);
        IPermissionContainer from = storage.getForPermissionCheck(blockPos);
        IPermissionContainer to = storage.getForPermissionCheck(dirPos);
        boolean flag = true;
        if (from.equals(to)) {
            BlockPos oppPoos = blockPos.offset(direction.getOpposite());
            IPermissionContainer opp = storage.getForPermissionCheck(oppPoos);
            if (!from.equals(opp))
                flag = from.canInteract(null, PermissionRegistry.PISTONBORDER, oppPoos);
        } else
            flag = from.canInteract(null, PermissionRegistry.PISTONBORDER, blockPos) && to.canInteract(null, PermissionRegistry.PISTONBORDER, dirPos);
        if (!flag) {
            //Idk enough about piston behaviour to update more blocks when slime is involved.
            //Ghost blocks appear when trying to push slime contraptions across border
            world.updateListeners(blockPos, state, state, 20);
            BlockState toState = world.getBlockState(dirPos);
            world.updateListeners(dirPos, toState, toState, 20);
        }
        return flag;
    }

    public static boolean canFlow(BlockState fluidBlockState, BlockView world, BlockPos blockPos, Direction direction) {
        if (!(world instanceof ServerWorld) || direction == Direction.UP || direction == Direction.DOWN)
            return true;
        ClaimStorage storage = ClaimStorage.get((ServerWorld) world);
        IPermissionContainer from = storage.getForPermissionCheck(blockPos);
        IPermissionContainer to = storage.getForPermissionCheck(blockPos.offset(direction));
        return from.equals(to) || to.canInteract(null, PermissionRegistry.WATERBORDER, blockPos);
    }

    public static boolean canStartRaid(ServerPlayerEntity player) {
        IPermissionContainer claim = ClaimStorage.get(player.getServerWorld()).getForPermissionCheck(player.getBlockPos());
        return claim.canInteract(player, PermissionRegistry.RAID, player.getBlockPos());
    }

    public static boolean canFireSpread(ServerWorld world, BlockPos pos) {
        IPermissionContainer claim = ClaimStorage.get(world).getForPermissionCheck(pos);
        return claim.canInteract(null, PermissionRegistry.FIRESPREAD, pos);
    }

    public static boolean preventMobSpawn(ServerWorld world, MobEntity entity) {
        IPermissionContainer claim = ClaimStorage.get(world).getForPermissionCheck(entity.getBlockPos());
        if (entity.getType().getSpawnGroup() == SpawnGroup.MONSTER)
            return claim.canInteract(null, PermissionRegistry.MOBSPAWN, entity.getBlockPos());
        return claim.canInteract(null, PermissionRegistry.ANIMALSPAWN, entity.getBlockPos());
    }

    public static boolean lightningFire(LightningEntity lightning) {
        if (!(lightning.world instanceof ServerWorld))
            return true;
        BlockPos.Mutable mutable = lightning.getBlockPos().mutableCopy();
        ServerWorld world = (ServerWorld) lightning.world;
        for (int x = -1; x <= 1; x++)
            for (int z = -1; z <= 1; z++) {
                mutable.set(mutable.getX() + x, mutable.getY(), mutable.getZ() + z);
                IPermissionContainer claim = ClaimStorage.get(world).getForPermissionCheck(mutable);
                if (!claim.canInteract(null, PermissionRegistry.LIGHTNING, mutable))
                    return false;
            }
        return true;
    }
}
