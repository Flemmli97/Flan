package io.github.flemmli97.flan.platform.integration.currency;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import java.util.function.Consumer;

public abstract class CommandCurrency {

    protected static CommandCurrency INSTANCE;

    public static CommandCurrency instance() {
        return INSTANCE;
    }

    public abstract boolean sellClaimBlocks(ServerPlayer player, int blocks, float value, Consumer<Component> message);

    public abstract boolean buyClaimBlocks(ServerPlayer player, int blocks, float value, Consumer<Component> message);
}
