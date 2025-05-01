package io.github.flemmli97.flan.api.fabric;

import io.github.flemmli97.flan.claim.Claim;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.Nullable;

public class ClaimBorderCrossEvent {

    public interface BorderEvent {

        /**
         * Callback for when a player crosses a claims border. This gets triggered for subclaims too!
         * To check if a claim is a subclaim use {@link Claim#isSubclaim}
         *
         * @param player The player
         * @param enter  The claim the player now enters.  to check
         * @param exit   The claim the player leaves
         */
        void borderCross(ServerPlayer player, @Nullable Claim enter, @Nullable Claim exit);

    }

    public static Event<BorderEvent> EVENT = EventFactory.createArrayBacked(BorderEvent.class,
            (listeners) -> (player, permission, pos) -> {
                for (BorderEvent event : listeners) {
                    event.borderCross(player, permission, pos);
                }
            }
    );
}
