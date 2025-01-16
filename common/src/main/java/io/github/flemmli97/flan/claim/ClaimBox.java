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
        return this.intersects(other.minX, other.minY, other.minZ, other.maxX, other.maxY, other.maxZ);
    }

    public boolean intersects(double x, double y, double z, double X, double Y, double Z) {
        return this.minX < X && this.maxX > x && this.minY < Y && this.maxY > y && this.minZ < Z && this.maxZ > z;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this)
            return true;
        if (!(obj instanceof ClaimBox otherBox))
            return false;
        return this.minX == otherBox.minX && this.minY == otherBox.minY && this.minZ == otherBox.minZ
                && this.maxX == otherBox.maxX && this.maxY == otherBox.maxY && this.maxZ == otherBox.maxZ;
    }
}
