package io.github.flemmli97.flan.utils;

import io.github.flemmli97.flan.claim.Claim;
import io.github.flemmli97.flan.claim.ClaimBox;
import io.github.flemmli97.flan.claim.ClaimStorage;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Tuple;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.Vec3;

import java.util.function.BiFunction;

public class TeleportUtils {

    public static BlockPos roundedBlockPos(Vec3 pos) {
        return BlockPos.containing(pos);
    }

    public static Vec3 getTeleportPos(ServerPlayer player, Vec3 playerPos, ClaimStorage storage, Area2D dim, BlockPos.MutableBlockPos bPos, BiFunction<Claim, BlockPos, Boolean> check) {
        return getTeleportPos(player, playerPos, storage, dim, false, bPos, check);
    }

    public static Vec3 getTeleportPos(ServerPlayer player, Vec3 playerPos, ClaimStorage storage, Area2D dim, boolean checkSub, BlockPos.MutableBlockPos bPos, BiFunction<Claim, BlockPos, Boolean> check) {
        Tuple<Direction, Vec3> pos = nearestOutside(dim, playerPos);
        bPos.set(pos.getB().x(), pos.getB().y(), pos.getB().z());
        Claim claim = storage.getClaimAt(bPos);
        if (checkSub) {
            Claim sub = claim != null ? claim.getSubClaim(bPos) : null;
            if (sub != null)
                claim = sub;
        }
        if (claim == null || check.apply(claim, bPos)) {
            Vec3 ret = pos.getB();
            BlockPos rounded = roundedBlockPos(ret);
            int y = player.serverLevel().getChunk(rounded.getX() >> 4, rounded.getZ() >> 4)
                    .getHeight(Heightmap.Types.MOTION_BLOCKING, rounded.getX() & 15, rounded.getZ() & 15);
            Vec3 dest = new Vec3(ret.x, y + 1, ret.z);
            if (player.level().noCollision(player, player.getBoundingBox().move(dest.subtract(player.position()))))
                return dest;
            return new Vec3(rounded.getX() + 0.5, y + 1, rounded.getZ() + 0.5);
        }
        ClaimBox newDim = claim.getDimensions();
        switch (pos.getA()) {
            case NORTH -> dim.minZ = newDim.minZ();
            case SOUTH -> dim.maxZ = newDim.maxZ();
            case EAST -> dim.maxX = newDim.maxX();
            default -> dim.minX = newDim.minX();
        }
        return getTeleportPos(player, playerPos, storage, dim, checkSub, bPos, check);
    }

    private static Tuple<Direction, Vec3> nearestOutside(Area2D dim, Vec3 from) {
        double northDist = Math.abs(from.z() - dim.minZ);
        double southDist = Math.abs(dim.maxZ - from.z());
        double westDist = Math.abs(from.x() - dim.minX);
        double eastDist = Math.abs(dim.maxX - from.x());
        if (northDist > southDist) {
            if (eastDist > westDist) {
                if (southDist > westDist)
                    return new Tuple<>(Direction.WEST, new Vec3(dim.minX - 1.5, from.y(), from.z()));
                return new Tuple<>(Direction.SOUTH, new Vec3(from.x(), from.y(), dim.maxZ + 1.5));
            }
            if (southDist > eastDist)
                return new Tuple<>(Direction.EAST, new Vec3(dim.maxX + 1.5, from.y(), from.z()));
            return new Tuple<>(Direction.SOUTH, new Vec3(from.x(), from.y(), dim.maxZ + 1.5));
        }
        if (eastDist > westDist) {
            if (northDist > westDist)
                return new Tuple<>(Direction.WEST, new Vec3(dim.minX - 1.5, from.y(), from.z()));
            return new Tuple<>(Direction.NORTH, new Vec3(from.x(), from.y(), dim.minZ - 1.5));
        }
        if (northDist > eastDist)
            return new Tuple<>(Direction.EAST, new Vec3(dim.maxX + 1.5, from.y(), from.z()));
        return new Tuple<>(Direction.NORTH, new Vec3(from.x(), from.y(), dim.minZ - 1.5));
    }

    public static class Area2D {

        public int minX, minZ, maxX, maxZ;

        public Area2D(int minX, int minZ, int maxX, int maxZ) {
            this.minX = minX;
            this.maxX = maxX;
            this.minZ = minZ;
            this.maxZ = maxZ;
        }

        public Area2D(ClaimBox box) {
            this.minX = box.minX();
            this.maxX = box.maxX();
            this.minZ = box.minZ();
            this.maxZ = box.maxZ();
        }
    }
}
