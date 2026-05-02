package io.github.flemmli97.flan.claim;

import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.AABB;

public record ClaimBox(int minX, int minY, int minZ, int maxX, int maxY, int maxZ) {

    public boolean insideClaim(BlockPos pos) {
        return this.intersects(pos.getX(), pos.getY(), pos.getZ(), pos.getX(), pos.getY(), pos.getZ(), 0);
    }

    public boolean intersects(ClaimBox other) {
        return this.intersects(other.minX, other.minY, other.minZ, other.maxX, other.maxY, other.maxZ);
    }

    public boolean intersects(AABB other) {
        return this.intersects(other.minX, other.minY, other.minZ, other.maxX, other.maxY, other.maxZ, 1);
    }

    public boolean intersects(double x, double y, double z, double X, double Y, double Z) {
        return this.intersects(x, y, z, X, Y, Z, 0);
    }

    public boolean intersects(double x, double y, double z, double X, double Y, double Z, double padding) {
        return this.minX <= X && this.maxX + padding >= x && this.minY <= Y && this.maxY + padding >= y && this.minZ <= Z && this.maxZ + padding >= z;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this)
            return true;
        if (!(obj instanceof ClaimBox(int x1, int y1, int z1, int x2, int y2, int z2)))
            return false;
        return this.minX == x1 && this.minY == y1 && this.minZ == z1
                && this.maxX == x2 && this.maxY == y2 && this.maxZ == z2;
    }

    public boolean contains(ClaimBox other) {
        return this.minX <= other.minX && this.maxX >= other.maxX
                && this.minY <= other.minY && this.maxY >= other.maxY
                && this.minZ <= other.minZ && this.maxZ >= other.maxZ;
    }
}