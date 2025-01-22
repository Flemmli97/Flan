package io.github.flemmli97.flan.player.display;

import io.github.flemmli97.flan.claim.Claim;
import io.github.flemmli97.flan.claim.ClaimBox;
import io.github.flemmli97.flan.config.ConfigHandler;
import io.github.flemmli97.flan.player.ClientBlockDisplayTracker;
import io.github.flemmli97.flan.player.PlayerClaimData;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.SectionPos;
import net.minecraft.core.Vec3i;
import net.minecraft.network.protocol.game.ClientboundLevelParticlesPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;

public class ClaimDisplay {

    private int displayTime;
    private final int displayHeight;

    private final DisplayBox display;
    public final EnumDisplayType type;

    private DisplayBoxPos pos;
    private ClaimBox prevDims;

    private final UUID displayId = UUID.randomUUID();

    public ClaimDisplay(Claim claim, EnumDisplayType type, int y) {
        this(claim.display(), claim.getLevel(), type, y);
    }

    public ClaimDisplay(DisplayBox display, Level level, EnumDisplayType type, int y) {
        this.display = display;
        this.displayTime = ConfigHandler.CONFIG.claimDisplayTime;
        this.type = type;
        this.displayHeight = Math.max(1 + level.getMinBuildHeight(), y);
    }

    private static DisplayBoxPos calculatePos(ServerLevel level, DisplayBox display, int height) {
        boolean is3d = display.is3d();
        ChunkCache chunkCache = new ChunkCache(level);
        ClaimBox box = display.box();
        List<BlockPos> vertices = boxVertices(box, is3d);
        List<BlockPos> edges = boxEdges(box, display.excludedSides(), is3d, height, vertices, chunkCache);
        if (!is3d) {
            List<BlockPos> verticesNew = new ArrayList<>();
            for (BlockPos pos : vertices) {
                Height heightPos = getHeight(pos.getX(), pos.getZ(), height, chunkCache);
                if (heightPos != null) {
                    if (heightPos.solid != heightPos.water)
                        verticesNew.add(new BlockPos(box.minX(), heightPos.water, box.minZ()));
                    verticesNew.add(new BlockPos(box.minX(), heightPos.solid, box.minZ()));
                }
            }
            vertices = verticesNew;
        }
        return new DisplayBoxPos(vertices, edges);
    }

    private static List<BlockPos> boxVertices(ClaimBox box, boolean is3d) {
        List<BlockPos> vertices = new ArrayList<>();
        if (is3d) {
            vertices.add(new BlockPos(box.minX(), box.minY(), box.minZ()));
            vertices.add(new BlockPos(box.maxX(), box.minY(), box.minZ()));
            vertices.add(new BlockPos(box.maxX(), box.minY(), box.maxZ()));
            vertices.add(new BlockPos(box.minX(), box.minY(), box.maxZ()));

            vertices.add(new BlockPos(box.minX(), box.maxY(), box.minZ()));
            vertices.add(new BlockPos(box.maxX(), box.maxY(), box.minZ()));
            vertices.add(new BlockPos(box.maxX(), box.maxY(), box.maxZ()));
            vertices.add(new BlockPos(box.minX(), box.maxY(), box.maxZ()));
        } else {
            vertices.add(new BlockPos(box.minX(), 0, box.minZ()));
            vertices.add(new BlockPos(box.maxX(), 0, box.minZ()));
            vertices.add(new BlockPos(box.maxX(), 0, box.maxZ()));
            vertices.add(new BlockPos(box.minX(), 0, box.maxZ()));
        }
        return vertices;
    }

    private static List<BlockPos> boxEdges(ClaimBox box, Set<Direction> exclude, boolean is3d, int height, List<BlockPos> vertices, ChunkCache chunkCache) {
        List<BlockPos> edges = new ArrayList<>();
        if (is3d) {
            for (int i = 0; i < 4; i++) {
                int next = (i + 1) % 4;
                int upperNext = (i + 1) % 4 + 4;
                BlockPos now = vertices.get(i);
                BlockPos nowUpper = vertices.get(i + 4);
                BlockPos nextPos = vertices.get(next);
                BlockPos nextPosUpper = vertices.get(upperNext);
                interpolateEvenly(now, nextPos, true, 10, edges::add);
                interpolateEvenly(nowUpper, nextPosUpper, true, 10, edges::add);
                interpolateEvenly(now, nowUpper, true, 10, edges::add);
            }
        } else {
            Consumer<BlockPos> cons = pos -> {
                Height calc = getHeight(pos.getX(), pos.getZ(), height, chunkCache);
                if (calc != null) {
                    if (calc.solid != calc.water)
                        edges.add(new BlockPos(pos.getX(), calc.water, pos.getZ()));
                    edges.add(new BlockPos(pos.getX(), calc.solid, pos.getZ()));
                }
            };
            for (int i = 0; i < 4; i++) {
                int next = (i + 1) % 4;
                BlockPos now = vertices.get(i);
                BlockPos nextPos = vertices.get(next);
                if (!exclude.contains(Direction.NORTH) && now.getZ() == nextPos.getZ() && now.getZ() == box.minZ()) {
                    interpolateEvenly(now, nextPos, false, 10, cons);
                }
                if (!exclude.contains(Direction.SOUTH) && now.getZ() == nextPos.getZ() && now.getZ() == box.maxZ()) {
                    interpolateEvenly(now, nextPos, false, 10, cons);
                }
                if (!exclude.contains(Direction.WEST) && now.getX() == nextPos.getX() && now.getX() == box.minX()) {
                    interpolateEvenly(now, nextPos, false, 10, cons);
                }
                if (!exclude.contains(Direction.EAST) && now.getX() == nextPos.getX() && now.getX() == box.maxX()) {
                    interpolateEvenly(now, nextPos, false, 10, cons);
                }
            }
        }
        return edges;
    }

    private static void interpolateEvenly(@NotNull BlockPos start, BlockPos end, boolean height, int step, Consumer<BlockPos> cons) {
        BlockPos.MutableBlockPos startM = start.mutable();
        BlockPos.MutableBlockPos endM = end.mutable();
        int dX = Integer.compare(endM.getX() - startM.getX(), 0);
        int dY = Integer.compare(height ? endM.getY() - startM.getY() : 0, 0);
        int dZ = Integer.compare(endM.getZ() - startM.getZ(), 0);
        startM.move(dX, dY, dZ);
        endM.move(-dX, -dY, -dZ);
        cons.accept(startM.immutable());
        cons.accept(endM.immutable());
        if (startM.distManhattan(endM) < step)
            return;
        int dist;
        while ((dist = (height ? startM.distManhattan(endM) : dist2d(startM, endM))) > step) {
            int amount = (int) Math.min(step, dist * 0.5);
            startM.move(dX * amount, dY * amount, dZ * amount);
            endM.move(-dX * amount, -dY * amount, -dZ * amount);
            cons.accept(startM.immutable());
            cons.accept(endM.immutable());
            if (dist < startM.distManhattan(endM) || amount < step)
                break;
        }
    }

    private static int dist2d(Vec3i first, Vec3i sec) {
        int dX = Math.abs(sec.getX() - first.getX());
        int dZ = Math.abs(sec.getZ() - first.getZ());
        return dX + dZ;
    }

    public boolean display(ServerPlayer player, boolean remove) {
        if (--this.displayTime % 2 == 0)
            return this.display.isRemoved();
        ClaimBox dims = this.display.box();
        if (this.pos == null || this.changed(dims)) {
            this.pos = calculatePos(player.serverLevel(), this.display, this.displayHeight);
            if (!ConfigHandler.CONFIG.particleDisplay) {
                PlayerClaimData data = PlayerClaimData.get(player);
                Set<ClientBlockDisplayTracker.DisplayData> displayData = new HashSet<>();
                for (BlockPos pos : this.pos.vertices) {
                    displayData.add(new ClientBlockDisplayTracker.DisplayData(pos, this.type.displayBlock));
                }
                for (BlockPos pos : this.pos.edges) {
                    displayData.add(new ClientBlockDisplayTracker.DisplayData(pos, this.type.displayBlock));
                }
                data.clientBlockDisplayTracker.displayFakeBlocks(this.displayId, displayData);
            }
        }
        if (ConfigHandler.CONFIG.particleDisplay) {
            for (BlockPos pos : this.pos.vertices) {
                player.connection.send(new ClientboundLevelParticlesPacket(this.type.cornerParticle, true, pos.getX() + 0.5, pos.getY() + 0.5 + player.serverLevel().getRandom().nextDouble() * 1.5, pos.getZ() + 0.5, 0, 1, 0, 1, 0));
            }
            for (BlockPos pos : this.pos.vertices) {
                player.connection.send(new ClientboundLevelParticlesPacket(this.type.middleParticle, true, pos.getX() + 0.5, pos.getY() + 0.5 + player.serverLevel().getRandom().nextDouble() * 1.5, pos.getZ() + 0.5, 0, 1, 0, 1, 0));
            }
        }
        this.prevDims = dims;
        return this.display.isRemoved() || (remove && this.displayTime < 0);
    }

    public void onRemoved(ServerPlayer player) {
        if (!ConfigHandler.CONFIG.particleDisplay) {
            PlayerClaimData data = PlayerClaimData.get(player);
            data.clientBlockDisplayTracker.resetFakeBlocks(this.displayId);
        }
    }

    private boolean changed(ClaimBox dims) {
        return this.prevDims == null || !this.prevDims.equals(dims);
    }

    /**
     * Returns a height instance with
     */
    public static Height getHeight(int x, int z, int y, ChunkCache chunkCache) {
        LevelChunk chunk = chunkCache.fetchChunk(x, z);
        if (chunk == null)
            return null;
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos(x, y, z);
        BlockState state = chunk.getBlockState(pos);
        if (state.canBeReplaced()) {
            //Move Down
            boolean startedInLiquid = state.liquid();
            boolean inLiquid = false;
            int liquidHeight = pos.getY();
            while (state.canBeReplaced() && !chunk.isOutsideBuildHeight(pos)) {
                pos.move(0, -1, 0);
                state = chunk.getBlockState(pos);
                if (!startedInLiquid && !inLiquid && state.liquid()) {
                    inLiquid = true;
                    liquidHeight = pos.getY();
                }
            }
            int height = pos.getY();
            liquidHeight = inLiquid ? liquidHeight : height;
            if (startedInLiquid) {
                pos.set(pos.getX(), liquidHeight + 1, pos.getZ());
                state = chunk.getBlockState(pos);
                while (state.liquid() && !chunk.isOutsideBuildHeight(pos)) {
                    pos.move(0, 1, 0);
                    state = chunk.getBlockState(pos);
                }
                if (state.canBeReplaced())
                    liquidHeight = pos.getY() - 1;
            }
            return new Height(height, liquidHeight);
        }
        //Move Up
        while (!state.canBeReplaced() && !chunk.isOutsideBuildHeight(pos)) {
            pos.move(0, 1, 0);
            state = chunk.getBlockState(pos);
        }
        int height = pos.getY() - 1;
        boolean liquid = false;
        while (state.liquid() && !state.canBeReplaced() && !chunk.isOutsideBuildHeight(pos)) {
            pos.move(0, 1, 0);
            liquid = true;
            state = chunk.getBlockState(pos);
        }
        return new Height(height, liquid ? pos.getY() - 1 : height);
    }

    @Override
    public int hashCode() {
        return this.display.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj instanceof ClaimDisplay)
            return this.display.equals(((ClaimDisplay) obj).display);
        return false;
    }

    record DisplayBoxPos(List<BlockPos> vertices, List<BlockPos> edges) {
    }

    public record Height(int solid, int water) {
    }

    public static class ChunkCache {

        private final Map<ChunkPos, LevelChunk> chunkCache = new HashMap<>();
        private final ServerLevel level;

        public ChunkCache(ServerLevel serverLevel) {
            this.level = serverLevel;
        }

        public LevelChunk fetchChunk(int x, int z) {
            ChunkPos pos = new ChunkPos(SectionPos.blockToSectionCoord(x), SectionPos.blockToSectionCoord(z));
            return this.chunkCache.computeIfAbsent(pos, k -> {
                if (!this.level.hasChunk(pos.x, pos.z))
                    return null;
                return this.level.getChunk(pos.x, pos.z);
            });
        }
    }
}
