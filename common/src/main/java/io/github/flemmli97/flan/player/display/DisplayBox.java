package io.github.flemmli97.flan.player.display;

import io.github.flemmli97.flan.claim.ClaimBox;
import net.minecraft.core.Direction;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.Set;
import java.util.function.Supplier;

public class DisplayBox {

    private final ClaimBox box;
    private final Supplier<Boolean> removed;

    private final EnumSet<Direction> excludedSides = EnumSet.noneOf(Direction.class);

    public DisplayBox(int minX, int minY, int minZ, int maxX, int maxY, int maxZ, Direction... exclude) {
        this(minX, minY, minZ, maxX, maxY, maxZ, () -> false, exclude);
    }

    public DisplayBox(int minX, int minY, int minZ, int maxX, int maxY, int maxZ, Supplier<Boolean> removed, Direction... exclude) {
        this.box = new ClaimBox(minX, minY, minZ, Math.max(minX + 1, maxX), maxY, Math.max(minZ + 1, maxZ));
        this.removed = removed;
        this.excludedSides.addAll(Arrays.asList(exclude));
    }

    /**
     * For claims with dynamic size (atm only from this mod)
     */
    public DisplayBox(ClaimBox box, Supplier<Boolean> removed, Direction... exclude) {
        this.box = box;
        this.removed = removed;
        this.excludedSides.addAll(Arrays.asList(exclude));
    }

    public boolean isRemoved() {
        return this.removed.get();
    }

    public ClaimBox box() {
        return this.box;
    }

    public Set<Direction> excludedSides() {
        return this.excludedSides;
    }

    public boolean is3d() {
        return false;
    }

    @Override
    public int hashCode() {
        return this.box.hashCode();
    }


    @Override
    public boolean equals(Object obj) {
        if (obj == this)
            return true;
        if (obj instanceof DisplayBox other)
            return this.box.equals(other.box);
        return false;
    }
}
