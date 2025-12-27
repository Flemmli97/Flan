package io.github.flemmli97.flan.event;

import io.github.flemmli97.flan.api.data.IPermissionContainer;
import io.github.flemmli97.flan.api.permission.BuiltinPermission;
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
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.StructureStart;

import java.util.List;

public class WorldEvents {

    public static void modifyExplosion(List<BlockPos> toExplode, ServerLevel level) {
        ClaimStorage storage = ClaimStorage.get(level);
        toExplode.removeIf(pos -> {
            IPermissionContainer claim = storage.getForPermissionCheck(pos);
            if (claim != null)
                return !claim.canInteract(null, BuiltinPermission.EXPLOSIONS, pos);
            return false;
        });
    }

    public static boolean pistonCanPush(BlockState state, Level level, BlockPos blockPos, Direction direction, Direction pistonDir) {
        if (level.isClientSide())
            return true;
        BlockPos dirPos = blockPos.relative(direction);
        ClaimStorage storage = ClaimStorage.get((ServerLevel) level);
        IPermissionContainer from = storage.getForPermissionCheck(blockPos);
        IPermissionContainer to = storage.getForPermissionCheck(dirPos);
        boolean flag = true;
        if (from.equals(to)) {
            BlockPos oppPoos = blockPos.relative(direction.getOpposite());
            IPermissionContainer opp = storage.getForPermissionCheck(oppPoos);
            if (!from.equals(opp))
                flag = from.canInteract(null, BuiltinPermission.PISTONBORDER, oppPoos);
        } else if (!state.isAir())
            flag = from.canInteract(null, BuiltinPermission.PISTONBORDER, blockPos) && to.canInteract(null, BuiltinPermission.PISTONBORDER, dirPos);
        if (!flag) {
            //Idk enough about piston behaviour to update more blocks when slime is involved.
            //Ghost blocks appear when trying to push slime contraptions across border
            level.sendBlockUpdated(blockPos, state, state, 20);
            BlockState toState = level.getBlockState(dirPos);
            level.sendBlockUpdated(dirPos, toState, toState, 20);
        }
        return flag;
    }

    public static boolean canFlow(BlockState fluidBlockState, BlockGetter level, BlockPos blockPos, Direction direction) {
        if (!(level instanceof ServerLevel) || direction == Direction.UP || direction == Direction.DOWN)
            return true;
        ClaimStorage storage = ClaimStorage.get((ServerLevel) level);
        IPermissionContainer from = storage.getForPermissionCheck(blockPos);
        IPermissionContainer to = storage.getForPermissionCheck(blockPos.relative(direction));
        return from.equals(to) || to.canInteract(null, BuiltinPermission.WATERBORDER, blockPos);
    }

    public static boolean canStartRaid(ServerPlayer player, BlockPos pos) {
        IPermissionContainer claim = ClaimStorage.get(player.level()).getForPermissionCheck(pos);
        return claim.canInteract(player, BuiltinPermission.RAID, pos);
    }

    public static boolean canFireSpread(ServerLevel level, BlockPos pos) {
        IPermissionContainer claim = ClaimStorage.get(level).getForPermissionCheck(pos);
        return claim.canInteract(null, BuiltinPermission.FIRESPREAD, pos);
    }

    public static boolean preventMobSpawn(ServerLevel level, Mob entity) {
        return preventMobSpawn(level, entity.blockPosition(), entity.getType().getCategory());
    }

    public static boolean preventMobSpawn(ServerLevel level, BlockPos pos, MobCategory category) {
        IPermissionContainer claim = ClaimStorage.get(level).getForPermissionCheck(pos);
        if (category == MobCategory.MONSTER)
            return claim.canInteract(null, BuiltinPermission.MONSTERSPAWN, pos);
        return claim.canInteract(null, BuiltinPermission.MOBSPAWN, pos);
    }

    public static boolean lightningFire(LightningBolt lightning) {
        if (!(lightning.level() instanceof ServerLevel level))
            return true;
        BlockPos.MutableBlockPos mutable = lightning.blockPosition().mutable();
        for (int x = -1; x <= 1; x++)
            for (int z = -1; z <= 1; z++) {
                mutable.set(mutable.getX() + x, mutable.getY(), mutable.getZ() + z);
                IPermissionContainer claim = ClaimStorage.get(level).getForPermissionCheck(mutable);
                if (!claim.canInteract(null, BuiltinPermission.LIGHTNING, mutable))
                    return false;
            }
        return true;
    }

    public static void serverTick(MinecraftServer server) {
        LogoutTracker.getInstance(server).tick();
    }

    @SuppressWarnings("deprecation")
    public static void onStructureGen(StructureStart structureStart, StructureManager structureManager) {
        if (!ConfigHandler.CONFIG.autoClaimStructures)
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
                .createAdminClaim(new BlockPos(bb.minX(), bb.minY(), bb.minZ()), new BlockPos(bb.maxX(), bb.maxY(), bb.maxZ()), level,
                        true);
    }
}
