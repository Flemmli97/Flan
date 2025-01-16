package io.github.flemmli97.flan.player.display;

import io.github.flemmli97.flan.claim.Claim;
import io.github.flemmli97.flan.claim.ClaimBox;

import java.util.function.Supplier;

public class ClaimDisplayBox extends DisplayBox {

    private final Claim claim;
    private final Supplier<ClaimBox> boxSup;

    public ClaimDisplayBox(Claim claim, Supplier<ClaimBox> sup, Supplier<Boolean> removed) {
        super(sup.get(), removed);
        this.claim = claim;
        this.boxSup = sup;
    }

    @Override
    public ClaimBox box() {
        return this.boxSup.get();
    }

    @Override
    public boolean is3d() {
        return this.claim.is3d();
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
