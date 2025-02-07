package io.github.flemmli97.flan.api.events;

import dev.architectury.event.Event;
import dev.architectury.event.EventFactory;
import io.github.flemmli97.flan.claim.Claim;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.Nullable;

public interface CrossBorderEvent {

    Event<CrossBorderEvent> EVENT = EventFactory.createLoop();

    void crossClaimBorder(ServerPlayer player, @Nullable Claim enterClaim, @Nullable Claim exitClaim);

}
