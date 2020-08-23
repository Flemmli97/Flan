package com.flemmli97.flan.player;

import com.flemmli97.flan.claim.Claim;
import com.flemmli97.flan.claim.ParticleIndicators;
import com.flemmli97.flan.config.ConfigHandler;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import net.minecraft.network.packet.s2c.play.ParticleS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.Heightmap;
import net.minecraft.world.World;

import java.util.List;
import java.util.Set;

public class ClaimDisplay {

    private int displayTime;
    private final Claim toDisplay;
    private int[][] poss;

    private int[][] middlePoss;

    private int[] prevDims;

    public ClaimDisplay(Claim claim) {
        this.toDisplay = claim;
        this.displayTime = ConfigHandler.config.claimDisplayTime;
        this.prevDims = claim.getDimensions();
    }

    public boolean display(ServerPlayerEntity player) {
        this.displayTime--;
        int[] dims = this.toDisplay.getDimensions();
        if (this.poss == null || this.changed(dims)) {
            this.middlePoss = this.calculateDisplayPos(player.world);
            this.poss = new int[][]{
                    this.getPosFrom(player.world, this.prevDims[0], this.prevDims[2], this.prevDims[4]),
                    this.getPosFrom(player.world, this.prevDims[1], this.prevDims[2], this.prevDims[4]),
                    this.getPosFrom(player.world, this.prevDims[0], this.prevDims[3], this.prevDims[4]),
                    this.getPosFrom(player.world, this.prevDims[1], this.prevDims[3], this.prevDims[4]),
            };
        }

        for (int[] pos : this.poss) {
            player.networkHandler.sendPacket(new ParticleS2CPacket(ParticleIndicators.CLAIMCORNER, true, pos[0] + 0.5, pos[1] + 0.25, pos[2] + 0.5, 0, 0.25f, 0, 0, 1));
        }
        for (int[] pos : this.middlePoss) {
            player.networkHandler.sendPacket(new ParticleS2CPacket(ParticleIndicators.CLAIMMIDDLE, true, pos[0] + 0.5, pos[1] + 0.25, pos[2] + 0.5, 0, 0.25f, 0, 0, 1));
        }
        this.prevDims = dims;
        return toDisplay.isRemoved() || displayTime < 0;
    }

    private boolean changed(int[] dims) {
        for (int i = 0; i < dims.length; i++)
            if (dims[i] != this.prevDims[i])
                return true;
        return false;
    }

    private int[][] calculateDisplayPos(World world) {
        List<int[]> l = Lists.newArrayList();
        Set<Integer> xs = Sets.newHashSet();
        this.addEvenly(this.prevDims[0], this.prevDims[1], 10, xs);
        Set<Integer> zs = Sets.newHashSet();
        this.addEvenly(this.prevDims[2], this.prevDims[3], 10, zs);
        for (int x : xs) {
            l.add(this.getPosFrom(world, x, this.prevDims[2], this.prevDims[4]));
            l.add(this.getPosFrom(world, x, this.prevDims[3], this.prevDims[4]));

        }
        for (int z : zs) {
            l.add(this.getPosFrom(world, this.prevDims[0], z, this.prevDims[4]));
            l.add(this.getPosFrom(world, this.prevDims[1], z, this.prevDims[4]));
        }

        return l.toArray(new int[0][]);
    }

    private void addEvenly(int min, int max, int step, Set<Integer> l) {
        if (max - min < step * 1.5)
            return;
        if (max - min > 0 && max - min <= step * 0.5) {
            l.add(max - step + 1);
            l.add(min + step - 1);
            return;
        }
        l.add(max - step);
        l.add(min + step);
        this.addEvenly(min + step, max - step, step, l);
    }

    private int[] getPosFrom(World world, int x, int z, int maxY) {
        return new int[]{x, Math.max(maxY, world.getChunk(x >> 4, z >> 4).sampleHeightmap(Heightmap.Type.WORLD_SURFACE, x & 15, z & 15) + 1), z};
    }

    @Override
    public int hashCode() {
        return this.toDisplay.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj instanceof ClaimDisplay)
            return this.toDisplay.equals(((ClaimDisplay) obj).toDisplay);
        return false;
    }
}
