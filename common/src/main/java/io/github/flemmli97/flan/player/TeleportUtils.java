package io.github.flemmli97.flan.player;

import io.github.flemmli97.flan.claim.Claim;
import io.github.flemmli97.flan.claim.ClaimStorage;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Tuple;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.Vec3;

import java.util.function.BiFunction;

public class TeleportUtils {

    public static BlockPos roundedBlockPos(Vec3 pos) {
        return new BlockPos(pos);
    }

    public static Vec3 getTeleportPos(ServerPlayer player, Vec3 playerPos, ClaimStorage storage, int[] dim, BlockPos.MutableBlockPos bPos, BiFunction<Claim, BlockPos, Boolean> check) {
        Tuple<Direction, Vec3> pos = nearestOutside(dim, playerPos);
        bPos.set(pos.getB().x(), pos.getB().y(), pos.getB().z());
        Claim claim = storage.getClaimAt(bPos);
        if (claim == null || check.apply(claim, bPos)) {
            Vec3 ret = pos.getB();
            BlockPos rounded = roundedBlockPos(ret);
            int y = player.getLevel().getChunk(rounded.getX() >> 4, rounded.getZ() >> 4, ChunkStatus.HEIGHTMAPS)
                    .getHeight(Heightmap.Types.MOTION_BLOCKING, rounded.getX() & 15, rounded.getZ() & 15);
            Vec3 dest = new Vec3(ret.x, y + 1, ret.z);
            if (player.level.noCollision(player, player.getBoundingBox().move(dest.subtract(player.position()))))
                return dest;
            return new Vec3(rounded.getX() + 0.5, y + 1, rounded.getZ() + 0.5);
        }
        int[] newDim = claim.getDimensions();
        switch (pos.getA()) {
            case NORTH:
                dim[2] = newDim[2];
                break;
            case SOUTH:
                dim[3] = newDim[3];
                break;
            case EAST:
                dim[1] = newDim[1];
                break;
            default:
                dim[0] = newDim[0];
                break;
        }
        return getTeleportPos(player, playerPos, storage, dim, bPos, check);
    }

    private static Tuple<Direction, Vec3> nearestOutside(int[] dim, Vec3 from) {
        double northDist = Math.abs(from.z() - dim[2]);
        double southDist = Math.abs(dim[3] - from.z());
        double westDist = Math.abs(from.x() - dim[0]);
        double eastDist = Math.abs(dim[1] - from.x());
        if (northDist > southDist) {
            if (eastDist > westDist) {
                if (southDist > westDist)
                    return new Tuple<>(Direction.WEST, new Vec3(dim[0] - 1.5, from.y(), from.z()));
                return new Tuple<>(Direction.SOUTH, new Vec3(from.x(), from.y(), dim[3] + 1.5));
            }
            if (southDist > eastDist)
                return new Tuple<>(Direction.EAST, new Vec3(dim[1] + 1.5, from.y(), from.z()));
            return new Tuple<>(Direction.SOUTH, new Vec3(from.x(), from.y(), dim[3] + 1.5));
        }
        if (eastDist > westDist) {
            if (northDist > westDist)
                return new Tuple<>(Direction.WEST, new Vec3(dim[0] - 1.5, from.y(), from.z()));
            return new Tuple<>(Direction.NORTH, new Vec3(from.x(), from.y(), dim[2] - 1.5));
        }
        if (northDist > eastDist)
            return new Tuple<>(Direction.EAST, new Vec3(dim[1] + 1.5, from.y(), from.z()));
        return new Tuple<>(Direction.NORTH, new Vec3(from.x(), from.y(), dim[2] - 1.5));
    }
}
