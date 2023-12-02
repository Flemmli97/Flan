package io.github.flemmli97.flan.player.display;

import io.github.flemmli97.flan.claim.Claim;
import io.github.flemmli97.flan.claim.ParticleIndicators;
import io.github.flemmli97.flan.config.ConfigHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.SectionPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.network.protocol.game.ClientboundBlockUpdatePacket;
import net.minecraft.network.protocol.game.ClientboundLevelParticlesPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ClaimDisplay {

    private boolean initialDisplay;
    private int displayTime;
    private final int height;
    private final DisplayBox display;
    public final EnumDisplayType type;
    private int[][] corners;

    private int[][] middlePoss;

    private DisplayBox.Box prevDims;

    private final DustParticleOptions corner, middle;
    private final Block displayBlock;

    public ClaimDisplay(Claim claim, EnumDisplayType type, int y) {
        this(claim.display(), claim.getWorld(), type, y);
    }

    public ClaimDisplay(DisplayBox display, Level level, EnumDisplayType type, int y) {
        this.display = display;
        this.displayTime = ConfigHandler.config.claimDisplayTime;
        this.prevDims = display.box();
        this.type = type;
        this.height = Math.max(1 + level.getMinBuildHeight(), y);
        switch (type) {
            case SUB -> {
                this.corner = ParticleIndicators.SUBCLAIMCORNER;
                this.middle = ParticleIndicators.SUBCLAIMMIDDLE;
                this.displayBlock = Blocks.IRON_BLOCK;
            }
            case CONFLICT -> {
                this.corner = ParticleIndicators.OVERLAPCLAIM;
                this.middle = ParticleIndicators.OVERLAPCLAIM;
                this.displayBlock = Blocks.REDSTONE_BLOCK;
            }
            case EDIT -> {
                this.corner = ParticleIndicators.EDITCLAIMCORNER;
                this.middle = ParticleIndicators.EDITCLAIMMIDDLE;
                this.displayBlock = Blocks.LAPIS_BLOCK;
            }
            default -> {
                this.corner = ParticleIndicators.CLAIMCORNER;
                this.middle = ParticleIndicators.CLAIMMIDDLE;
                this.displayBlock = Blocks.GOLD_BLOCK;
            }
        }
    }

    public boolean display(ServerPlayer player, boolean remove) {
        if (--this.displayTime % 2 == 0)
            return this.display.isRemoved();
        DisplayBox.Box dims = this.display.box();
        if (this.corners == null || this.changed(dims)) {
            this.onRemoved(player);
            this.middlePoss = calculateDisplayPos(player.serverLevel(), dims, this.height, this.display.excludedSides());
            this.corners = new int[][]{
                    getPosFrom(player.serverLevel(), dims.minX(), dims.minZ(), this.height),
                    getPosFrom(player.serverLevel(), dims.maxX(), dims.minZ(), this.height),
                    getPosFrom(player.serverLevel(), dims.minX(), dims.maxZ(), this.height),
                    getPosFrom(player.serverLevel(), dims.maxX(), dims.maxZ(), this.height),
            };
            this.initialDisplay = false;
        }
        if (ConfigHandler.config.particleDisplay) {
            for (int[] pos : this.corners) {
                if (pos[1] != pos[2])
                    player.connection.send(new ClientboundLevelParticlesPacket(this.corner, true, pos[0] + 0.5, pos[2] + 0.25, pos[3] + 0.5, 0, 0.5f, 0, 0, 1));
                player.connection.send(new ClientboundLevelParticlesPacket(this.corner, true, pos[0] + 0.5, pos[1] + 0.25, pos[3] + 0.5, 0, 0.5f, 0, 0, 1));
            }
            if (this.middlePoss != null)
                for (int[] pos : this.middlePoss) {
                    if (pos[1] != pos[2])
                        player.connection.send(new ClientboundLevelParticlesPacket(this.middle, true, pos[0] + 0.5, pos[2] + 0.25, pos[3] + 0.5, 0, 0.5f, 0, 0, 1));
                    player.connection.send(new ClientboundLevelParticlesPacket(this.middle, true, pos[0] + 0.5, pos[1] + 0.25, pos[3] + 0.5, 0, 0.5f, 0, 0, 1));
                }
        } else if (!this.initialDisplay) {
            for (int[] pos : this.corners) {
                BlockPos blockPos = new BlockPos(pos[0], pos[1] != pos[2] ? pos[2] : pos[1], pos[3]);
                player.connection.send(new ClientboundBlockUpdatePacket(blockPos.below(), this.displayBlock.defaultBlockState()));
            }
            if (this.middlePoss != null)
                for (int[] pos : this.middlePoss) {
                    BlockPos blockPos = new BlockPos(pos[0], pos[1] != pos[2] ? pos[2] : pos[1], pos[3]);
                    player.connection.send(new ClientboundBlockUpdatePacket(blockPos.below(), this.displayBlock.defaultBlockState()));
                }
        }
        this.prevDims = dims;
        if (!this.initialDisplay)
            this.initialDisplay = true;
        return this.display.isRemoved() || (remove && this.displayTime < 0);
    }

    public void onRemoved(ServerPlayer player) {
        if (!ConfigHandler.config.particleDisplay) {
            if (this.corners != null)
                for (int[] pos : this.corners) {
                    BlockPos blockPos = new BlockPos(pos[0], pos[1] != pos[2] ? pos[2] : pos[1], pos[3]);
                    blockPos = blockPos.below();
                    player.connection.send(new ClientboundBlockUpdatePacket(blockPos, player.level().getBlockState(blockPos)));
                }
            if (this.middlePoss != null)
                for (int[] pos : this.middlePoss) {
                    BlockPos blockPos = new BlockPos(pos[0], pos[1] != pos[2] ? pos[2] : pos[1], pos[3]);
                    blockPos = blockPos.below();
                    player.connection.send(new ClientboundBlockUpdatePacket(blockPos, player.level().getBlockState(blockPos)));
                }
        }
    }

    private boolean changed(DisplayBox.Box dims) {
        return !this.prevDims.equals(dims);
    }

    public static int[][] calculateDisplayPos(ServerLevel world, DisplayBox.Box from, int height, Set<Direction> exclude) {
        List<int[]> l = new ArrayList<>();
        Set<Integer> xs = new HashSet<>();
        addEvenly(from.minX(), from.maxX(), 10, xs);
        xs.add(from.minX() + 1);
        xs.add(from.maxX() - 1);
        Set<Integer> zs = new HashSet<>();
        addEvenly(from.minZ(), from.maxZ(), 10, zs);
        zs.add(from.minZ() + 1);
        zs.add(from.maxZ() - 1);
        for (int x : xs) {
            if (!exclude.contains(Direction.NORTH))
                l.add(getPosFrom(world, x, from.minZ(), height));
            if (!exclude.contains(Direction.SOUTH))
                l.add(getPosFrom(world, x, from.maxZ(), height));
        }
        for (int z : zs) {
            if (!exclude.contains(Direction.WEST))
                l.add(getPosFrom(world, from.minX(), z, height));
            if (!exclude.contains(Direction.EAST))
                l.add(getPosFrom(world, from.maxX(), z, height));
        }

        return l.toArray(new int[0][]);
    }

    private static void addEvenly(int min, int max, int step, Set<Integer> l) {
        if (max - min < step * 1.5)
            return;
        if (max - min > 0 && max - min <= step * 0.5) {
            l.add(max - step + 1);
            l.add(min + step - 1);
            return;
        }
        l.add(max - step);
        l.add(min + step);
        addEvenly(min + step, max - step, step, l);
    }

    /**
     * Returns an array of form [x,y1,y2,z] where y1 = height of the lowest replaceable block and y2 = height of the
     * lowest air block above water (if possible)
     */
    public static int[] getPosFrom(ServerLevel world, int x, int z, int maxY) {
        LevelChunk chunk = world.getChunk(SectionPos.blockToSectionCoord(x), SectionPos.blockToSectionCoord(z));
        int[] y = nextAirAndWaterBlockFrom(chunk, x, maxY, z);
        return new int[]{x, y[0], y[1], z};
    }

    // SAFETY: Ensure that the X/Z coordinates are for the given chunk
    // since the position is mutating only up or down, it's always in the same chunk
    @SuppressWarnings("deprecation")
    private static int[] nextAirAndWaterBlockFrom(LevelChunk chunk, int x, int y, int z) {
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos(x, y, z);
        BlockState state = chunk.getBlockState(pos);
        if (state.canBeReplaced()) {
            //Move Down
            boolean startedInLiquid = state.liquid();
            boolean liquidCheck = false;
            int liquidHeight = pos.getY();
            while (state.canBeReplaced() && !chunk.isOutsideBuildHeight(pos)) {
                pos.move(0, -1, 0);
                state = chunk.getBlockState(pos);
                if (!startedInLiquid && !liquidCheck && state.liquid()) {
                    liquidCheck = true;
                    liquidHeight = pos.getY();
                }
            }
            int[] yRet = {pos.getY() + 1, (liquidCheck ? liquidHeight : pos.getY()) + 1};
            if (startedInLiquid) {
                pos.set(pos.getX(), liquidHeight + 1, pos.getZ());
                state = chunk.getBlockState(pos);
                while (state.liquid() && !chunk.isOutsideBuildHeight(pos)) {
                    pos.move(0, 1, 0);
                    state = chunk.getBlockState(pos);
                }
                if (state.canBeReplaced())
                    yRet[1] = pos.getY();
            }
            return yRet;
        }
        //Move Up
        while (!state.canBeReplaced() && !chunk.isOutsideBuildHeight(pos)) {
            pos.move(0, 1, 0);
            state = chunk.getBlockState(pos);
        }
        int[] yRet = {pos.getY(), pos.getY()};
        while (state.liquid() && !chunk.isOutsideBuildHeight(pos)) {
            pos.move(0, 1, 0);
            state = chunk.getBlockState(pos);
        }
        if (state.canBeReplaced())
            yRet[1] = pos.getY();
        return yRet;
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
}
