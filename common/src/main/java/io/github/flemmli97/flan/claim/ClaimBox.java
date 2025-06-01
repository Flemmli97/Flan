package io.github.flemmli97.flan.claim;


import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.AABB;

public record ClaimBox(int minX, int minY, int minZ, int maxX, int maxY, int maxZ) {

    public boolean insideClaim(BlockPos pos) {
        return this.minX <= pos.getX() && this.maxX >= pos.getX() && this.minZ <= pos.getZ() && this.maxZ >= pos.getZ() && this.minY <= pos.getY()
                && this.maxY >= pos.getY();
    }

    public boolean intersects(ClaimBox other) {
        return this.intersects(other.minX, other.minY, other.minZ, other.maxX, other.maxY, other.maxZ);
    }

    public boolean intersects(AABB other) {
        return this.intersects(other.minX - 1, other.minY, other.minZ - 1, other.maxX, other.maxY, other.maxZ);
    }

    public boolean intersects(double x, double y, double z, double X, double Y, double Z) {
        return this.minX < X && this.maxX > x && this.minY < Y && this.maxY > y && this.minZ < Z && this.maxZ > z;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this)
            return true;
        if (!(obj instanceof ClaimBox(int x1, int y1, int z1, int x2, int y2, int z2)))
            return false;
        return this.minX == x1 && this.minY == y1 && this.minZ == z1
                && this.maxX == x1 && this.maxY == y1 && this.maxZ == z1;
    }
}
