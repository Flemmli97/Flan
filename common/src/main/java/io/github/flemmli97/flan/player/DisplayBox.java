package io.github.flemmli97.flan.player;

import net.minecraft.core.Direction;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.Set;
import java.util.function.Supplier;

public class DisplayBox {

    private final Box box;
    private final Supplier<Box> boxSup;
    private final Supplier<Boolean> removed;

    private final EnumSet<Direction> excludedSides = EnumSet.noneOf(Direction.class);

    public DisplayBox(int minX, int minY, int minZ, int maxX, int maxY, int maxZ, Direction... exclude) {
        this(minX, minY, minZ, maxX, maxY, maxZ, () -> false, exclude);
    }

    public DisplayBox(int minX, int minY, int minZ, int maxX, int maxY, int maxZ, Supplier<Boolean> removed, Direction... exclude) {
        this.box = new Box(minX, minY, minZ, Math.max(minX + 1, maxX), maxY, Math.max(minZ + 1, maxZ));
        this.boxSup = null;
        this.removed = removed;
        this.excludedSides.addAll(Arrays.asList(exclude));
    }

    /**
     * For claims with dynamic size (atm only from this mod)
     */
    public DisplayBox(Supplier<Box> sup, Supplier<Boolean> removed, Direction... exclude) {
        this.box = sup.get();
        this.boxSup = sup;
        this.removed = removed;
        this.excludedSides.addAll(Arrays.asList(exclude));
    }

    public boolean isRemoved() {
        return this.removed.get();
    }

    public Box box() {
        if (this.boxSup != null)
            return this.boxSup.get();
        return this.box;
    }

    public Set<Direction> excludedSides() {
        return this.excludedSides;
    }

    public record Box(int minX, int minY, int minZ, int maxX, int maxY, int maxZ) {

        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof Box otherBox))
                return false;
            return this.minX == otherBox.minX && this.minY == otherBox.minY && this.minZ == otherBox.minZ
                    && this.maxX == otherBox.maxX && this.maxY == otherBox.maxY && this.maxZ == otherBox.maxZ;
        }
    }
}
