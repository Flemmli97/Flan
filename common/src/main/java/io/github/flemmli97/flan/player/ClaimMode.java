package io.github.flemmli97.flan.player;

public enum ClaimMode {

    DEFAULT(false, false, "flan.claimMode.default"),
    SUBCLAIM(false, true, "flan.claimMode.subclaim"),
    DEFAULT_3D(true, false, "flan.claimMode.default.3d"),
    SUBCLAIM_3D(true, true, "flan.claimMode.subclaim.3d");

    public final boolean is3d, isSubclaim;
    public final String translationKey;

    ClaimMode(boolean is3d, boolean isSubclaim, String translationKey) {
        this.is3d = is3d;
        this.isSubclaim = isSubclaim;
        this.translationKey = translationKey;
    }
}
