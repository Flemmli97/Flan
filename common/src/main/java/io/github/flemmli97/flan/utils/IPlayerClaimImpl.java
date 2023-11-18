package io.github.flemmli97.flan.utils;

import io.github.flemmli97.flan.claim.Claim;
import io.github.flemmli97.flan.player.PlayerClaimData;

public interface IPlayerClaimImpl {

    PlayerClaimData get();

    /**
     * @return Gets the current claim the player is in. Can be null if not in a claim.
     */
    Claim getCurrentClaim();
}
