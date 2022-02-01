package io.github.flemmli97.flan.integration.currency;

import dev.architectury.injectables.annotations.ExpectPlatform;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import java.util.function.Consumer;

public class CommandCurrency {

    @ExpectPlatform
    public static boolean sellClaimBlocks(ServerPlayer player, int blocks, float value, Consumer<Component> message) {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static boolean buyClaimBlocks(ServerPlayer player, int blocks, float value, Consumer<Component> message) {
        throw new AssertionError();
    }
}
