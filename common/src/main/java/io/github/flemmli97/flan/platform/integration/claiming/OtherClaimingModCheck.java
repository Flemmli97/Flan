package io.github.flemmli97.flan.platform.integration.claiming;

import io.github.flemmli97.flan.Flan;
import io.github.flemmli97.flan.claim.Claim;
import io.github.flemmli97.flan.player.display.DisplayBox;

import java.util.Set;

public interface OtherClaimingModCheck {

    OtherClaimingModCheck INSTANCE = Flan.getPlatformInstance(OtherClaimingModCheck.class,
            "io.github.flemmli97.flan.fabric.platform.integration.claiming.OtherClaimingModCheckImpl",
            "io.github.flemmli97.flan.forge.platform.integration.claiming.OtherClaimingModCheckImpl");

    void findConflicts(Claim claim, Set<DisplayBox> set);
}
