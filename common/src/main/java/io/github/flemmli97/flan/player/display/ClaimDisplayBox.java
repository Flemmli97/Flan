package io.github.flemmli97.flan.player.display;

import io.github.flemmli97.flan.claim.Claim;

import java.util.function.Supplier;

public class ClaimDisplayBox extends DisplayBox {

    private final Claim claim;
    private final Supplier<Box> boxSup;

    public ClaimDisplayBox(Claim claim, Supplier<Box> sup, Supplier<Boolean> removed) {
        super(sup.get(), removed);
        this.claim = claim;
        this.boxSup = sup;
    }

    @Override
    public Box box() {
        return this.boxSup.get();
    }

    @Override
    public int hashCode() {
        return super.hashCode() >> this.claim.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this)
            return true;
        if (obj instanceof ClaimDisplayBox box)
            return this.claim == box.claim;
        return false;
    }
}
