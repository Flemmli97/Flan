package com.flemmli97.flan.player;

import com.flemmli97.flan.claim.Claim;
import com.flemmli97.flan.claim.ParticleIndicators;
import com.flemmli97.flan.config.ConfigHandler;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import net.minecraft.block.BlockState;
import net.minecraft.network.packet.s2c.play.ParticleS2CPacket;
import net.minecraft.particle.DustParticleEffect;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Heightmap;
import net.minecraft.server.world.ServerWorld;

import java.util.List;
import java.util.Set;

public class ClaimDisplay {

    private int displayTime, height;
    private final Claim toDisplay;
    public final EnumDisplayType type;
    private int[][] poss;

    private int[][] middlePoss;

    private int[] prevDims;

    private final DustParticleEffect corner, middle;
    public ClaimDisplay(Claim claim, EnumDisplayType type, int y) {
        this.toDisplay = claim;
        this.displayTime = ConfigHandler.config.claimDisplayTime;
        this.prevDims = claim.getDimensions();
        this.type = type;
        this.height = y;
        switch (type){
            case SUB:
                this.corner = ParticleIndicators.SUBCLAIMCORNER;
                this.middle = ParticleIndicators.SUBCLAIMMIDDLE;
                break;
            case CONFLICT:
                this.corner = ParticleIndicators.OVERLAPCLAIM;
                this.middle = ParticleIndicators.OVERLAPCLAIM;
                break;
            case EDIT:
                this.corner = ParticleIndicators.EDITCLAIMCORNER;
                this.middle = ParticleIndicators.EDITCLAIMMIDDLE;
                break;
            default:
                this.corner = ParticleIndicators.CLAIMCORNER;
                this.middle = ParticleIndicators.CLAIMMIDDLE;
                break;
        }
    }

    public boolean display(ServerPlayerEntity player) {
        this.displayTime--;
        int[] dims = this.toDisplay.getDimensions();
        if (this.poss == null || this.changed(dims)) {
            this.middlePoss = calculateDisplayPos(player.getServerWorld(), dims, this.height);
            this.poss = new int[][]{
                    this.getPosFrom(player.getServerWorld(), dims[0], dims[2], this.height),
                    this.getPosFrom(player.getServerWorld(), dims[1], dims[2], this.height),
                    this.getPosFrom(player.getServerWorld(), dims[0], dims[3], this.height),
                    this.getPosFrom(player.getServerWorld(), dims[1], dims[3], this.height),
            };
        }
        for (int[] pos : this.poss) {
            player.networkHandler.sendPacket(new ParticleS2CPacket(this.corner, true, pos[0] + 0.5, pos[1] + 0.25, pos[2] + 0.5, 0, 0.25f, 0, 0, 1));
        }
        if(this.middlePoss!=null)
            for (int[] pos : this.middlePoss) {
                player.networkHandler.sendPacket(new ParticleS2CPacket(this.middle, true, pos[0] + 0.5, pos[1] + 0.25, pos[2] + 0.5, 0, 0.25f, 0, 0, 1));
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

    public static int[][] calculateDisplayPos(ServerWorld world, int[] from, int height) {
        List<int[]> l = Lists.newArrayList();
        Set<Integer> xs = Sets.newHashSet();
        addEvenly(from[0], from[1], 10, xs);
        xs.add(from[0]+1);
        xs.add(from[1]-1);
        Set<Integer> zs = Sets.newHashSet();
        addEvenly(from[2], from[3], 10, zs);
        zs.add(from[2]+1);
        zs.add(from[3]-1);
        for (int x : xs) {
            l.add(getPosFrom(world, x, from[2], height));
            l.add(getPosFrom(world, x, from[3], height));

        }
        for (int z : zs) {
            l.add(getPosFrom(world, from[0], z, height));
            l.add(getPosFrom(world, from[1], z, height));
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

    private static int[] getPosFrom(ServerWorld world, int x, int z, int maxY) {
        int y = nextAirBlockFrom(world, x, maxY, z);
        return new int[]{x, y, z};
    }

    private static int nextAirBlockFrom(ServerWorld world, int x, int y, int z){
        BlockPos pos = new BlockPos(x,y,z);
        BlockState state = world.getBlockState(pos);
        if(state.getMaterial().isReplaceable()){
            pos = pos.down();
            while(world.getBlockState(pos).getMaterial().isReplaceable()){
                pos = pos.down();
            }
            pos = pos.up();
        }
        else{
            pos = pos.up();
            while(!world.getBlockState(pos).getMaterial().isReplaceable()){
                pos = pos.up();
            }
        }
        return pos.getY();
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
