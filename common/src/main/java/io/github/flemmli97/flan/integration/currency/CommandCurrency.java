package io.github.flemmli97.flan.integration.currency;

import dev.architectury.injectables.annotations.ExpectPlatform;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import java.util.function.Consumer;

public class CommandCurrency {

    @ExpectPlatform
    public static boolean sellClaimBlocks(ServerPlayerEntity player, int blocks, float value, Consumer<Text> message) {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static boolean buyClaimBlocks(ServerPlayerEntity player, int blocks, float value, Consumer<Text> message) {
        throw new AssertionError();
    }
}
