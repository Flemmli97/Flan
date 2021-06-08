package com.flemmli97.flan.player;

import com.flemmli97.flan.claim.Claim;

public interface IPlayerClaimImpl {

    PlayerClaimData get();

    /**
     * @return Gets the current claim the player is in. Can be null if not in a claim.
     */
    Claim getCurrentClaim();
}
