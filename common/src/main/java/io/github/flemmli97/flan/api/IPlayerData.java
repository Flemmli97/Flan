package io.github.flemmli97.flan.api;

import io.github.flemmli97.flan.config.ConfigHandler;

public interface IPlayerData {

    int getClaimBlocks();

    int getAdditionalClaims();

    int usedClaimBlocks();

    default boolean canUseClaimBlocks(int amount) {
        if (ConfigHandler.config.maxClaimBlocks == -1)
            return true;
        int usedClaimsBlocks = this.usedClaimBlocks();
        return usedClaimsBlocks + amount <= this.getClaimBlocks() + this.getAdditionalClaims();
    }
}
