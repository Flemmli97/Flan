package io.github.flemmli97.flan.platform.integration.currency;

import io.github.flemmli97.flan.Flan;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import java.util.function.Consumer;

public interface CommandCurrency {

    CommandCurrency INSTANCE = Flan.getPlatformInstance(CommandCurrency.class,
            "io.github.flemmli97.flan.fabric.platform.integration.currency.CommandCurrencyImpl",
            "io.github.flemmli97.flan.forge.platform.integration.currency.CommandCurrencyImpl");

    boolean sellClaimBlocks(ServerPlayer player, int blocks, float value, Consumer<Component> message);

    boolean buyClaimBlocks(ServerPlayer player, int blocks, float value, Consumer<Component> message);
}
