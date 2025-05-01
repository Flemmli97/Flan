package io.github.flemmli97.flan.api.forge;

import io.github.flemmli97.flan.claim.Claim;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.eventbus.api.Event;

/**
 * Event for when a player crosses a claims border
 * To check if a claim is a subclaim use {@link Claim#isSubclaim}
 */
public class ClaimBorderCrossEvent extends Event {

    private final ServerPlayer player;
    private final Claim enter;
    private final Claim exit;

    public ClaimBorderCrossEvent(ServerPlayer player, Claim enter, Claim exit) {
        this.player = player;
        this.enter = enter;
        this.exit = exit;
    }

    public ServerPlayer getPlayer() {
        return this.player;
    }

    /**
     * @return The claim the player now enters
     */
    public Claim enteringClaim() {
        return this.enter;
    }

    /**
     * @return The claim the player leaves
     */
    public Claim exititingClaim() {
        return this.exit;
    }
}
