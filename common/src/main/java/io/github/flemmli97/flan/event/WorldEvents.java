package io.github.flemmli97.flan.event;

import io.github.flemmli97.flan.api.data.IPermissionContainer;
import io.github.flemmli97.flan.api.permission.PermissionRegistry;
import io.github.flemmli97.flan.claim.ClaimStorage;
import io.github.flemmli97.flan.config.ConfigHandler;
import io.github.flemmli97.flan.mixin.StructureManagerAccessor;
import io.github.flemmli97.flan.player.LogoutTracker;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.StructureStart;

public class WorldEvents {

    public static void modifyExplosion(Explosion explosion, ServerLevel world) {
        ClaimStorage storage = ClaimStorage.get(world);
        explosion.getToBlow().removeIf(pos -> {
            IPermissionContainer claim = storage.getForPermissionCheck(pos);
            if (claim != null)
                return !claim.canInteract(null, PermissionRegistry.EXPLOSIONS, pos);
            return false;
        });
    }

    public static boolean pistonCanPush(BlockState state, Level world, BlockPos blockPos, Direction direction, Direction pistonDir) {
        if (world.isClientSide)
            return true;
        BlockPos dirPos = blockPos.relative(direction);
        ClaimStorage storage = ClaimStorage.get((ServerLevel) world);
        IPermissionContainer from = storage.getForPermissionCheck(blockPos);
        IPermissionContainer to = storage.getForPermissionCheck(dirPos);
        boolean flag = true;
        if (from.equals(to)) {
            BlockPos oppPoos = blockPos.relative(direction.getOpposite());
            IPermissionContainer opp = storage.getForPermissionCheck(oppPoos);
            if (!from.equals(opp))
                flag = from.canInteract(null, PermissionRegistry.PISTONBORDER, oppPoos);
        } else if (!state.isAir())
            flag = from.canInteract(null, PermissionRegistry.PISTONBORDER, blockPos) && to.canInteract(null, PermissionRegistry.PISTONBORDER, dirPos);
        if (!flag) {
            //Idk enough about piston behaviour to update more blocks when slime is involved.
            //Ghost blocks appear when trying to push slime contraptions across border
            world.sendBlockUpdated(blockPos, state, state, 20);
            BlockState toState = world.getBlockState(dirPos);
            world.sendBlockUpdated(dirPos, toState, toState, 20);
        }
        return flag;
    }

    public static boolean canFlow(BlockState fluidBlockState, BlockGetter world, BlockPos blockPos, Direction direction) {
        if (!(world instanceof ServerLevel) || direction == Direction.UP || direction == Direction.DOWN)
            return true;
        ClaimStorage storage = ClaimStorage.get((ServerLevel) world);
        IPermissionContainer from = storage.getForPermissionCheck(blockPos);
        IPermissionContainer to = storage.getForPermissionCheck(blockPos.relative(direction));
        return from.equals(to) || to.canInteract(null, PermissionRegistry.WATERBORDER, blockPos);
    }

    public static boolean canStartRaid(ServerPlayer player) {
        IPermissionContainer claim = ClaimStorage.get(player.serverLevel()).getForPermissionCheck(player.blockPosition());
        return claim.canInteract(player, PermissionRegistry.RAID, player.blockPosition());
    }

    public static boolean canFireSpread(ServerLevel world, BlockPos pos) {
        IPermissionContainer claim = ClaimStorage.get(world).getForPermissionCheck(pos);
        return claim.canInteract(null, PermissionRegistry.FIRESPREAD, pos);
    }

    public static boolean preventMobSpawn(ServerLevel world, Mob entity) {
        IPermissionContainer claim = ClaimStorage.get(world).getForPermissionCheck(entity.blockPosition());
        if (entity.getType().getCategory() == MobCategory.MONSTER)
            return claim.canInteract(null, PermissionRegistry.MOBSPAWN, entity.blockPosition());
        return claim.canInteract(null, PermissionRegistry.ANIMALSPAWN, entity.blockPosition());
    }

    public static boolean lightningFire(LightningBolt lightning) {
        if (!(lightning.level() instanceof ServerLevel world))
            return true;
        BlockPos.MutableBlockPos mutable = lightning.blockPosition().mutable();
        for (int x = -1; x <= 1; x++)
            for (int z = -1; z <= 1; z++) {
                mutable.set(mutable.getX() + x, mutable.getY(), mutable.getZ() + z);
                IPermissionContainer claim = ClaimStorage.get(world).getForPermissionCheck(mutable);
                if (!claim.canInteract(null, PermissionRegistry.LIGHTNING, mutable))
                    return false;
            }
        return true;
    }

    public static void serverTick(MinecraftServer server) {
        LogoutTracker.getInstance(server).tick();
    }

    @SuppressWarnings("deprecation")
    public static void onStructureGen(StructureStart structureStart, StructureManager structureManager) {
        if (!ConfigHandler.config.autoClaimStructures)
            return;
        LevelAccessor acc = ((StructureManagerAccessor) structureManager).getLevel();
        ServerLevel level = null;
        if (acc instanceof WorldGenRegion region)
            level = region.getLevel();
        else if (acc instanceof ServerLevel serverLevel)
            level = serverLevel;
        if (level == null)
            return;
        BoundingBox bb = structureStart.getBoundingBox();
        ClaimStorage.get(level)
                .createAdminClaim(new BlockPos(bb.minX(), bb.minY(), bb.minZ()), new BlockPos(bb.maxX(), bb.minY(), bb.maxZ()), level);
    }
}
