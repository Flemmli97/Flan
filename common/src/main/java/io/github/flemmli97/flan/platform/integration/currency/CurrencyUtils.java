package io.github.flemmli97.flan.platform.integration.currency;

import io.github.flemmli97.flan.api.economy.CurrencyHandler;
import io.github.flemmli97.flan.api.economy.CurrencyRegistry;
import io.github.flemmli97.flan.claim.ClaimUtils;
import io.github.flemmli97.flan.player.PlayerClaimData;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import java.util.function.Consumer;

public class CurrencyUtils {

    public static boolean buyClaimBlocks(ServerPlayer player, int blocks, float value, Consumer<Component> message) {
        if (value == -1) {
            message.accept(ClaimUtils.translatedText("flan.buyDisabled", ChatFormatting.DARK_RED));
            return false;
        }
        CurrencyHandler handler = CurrencyRegistry.findFirst();
        if (handler != null) {
            double price = blocks * value;
            if (handler.withdraw(player, price, message)) {
                PlayerClaimData data = PlayerClaimData.get(player);
                data.setAdditionalClaims(data.getAdditionalClaims() + blocks);
                message.accept(ClaimUtils.translatedText("flan.buySuccess", blocks, price, ChatFormatting.GOLD));
                return true;
            }
        }
        message.accept(ClaimUtils.translatedText("flan.currencyMissing", ChatFormatting.DARK_RED));
        return true;
    }

    public static boolean sellClaimBlocks(ServerPlayer player, int blocks, float value, Consumer<Component> message) {
        if (value == -1) {
            message.accept(ClaimUtils.translatedText("flan.sellDisabled", ChatFormatting.DARK_RED));
            return false;
        }
        CurrencyHandler handler = CurrencyRegistry.findFirst();
        if (handler != null) {
            PlayerClaimData data = PlayerClaimData.get(player);
            if (data.getAdditionalClaims() - Math.max(0, data.usedClaimBlocks() - data.getClaimBlocks()) < blocks) {
                message.accept(ClaimUtils.translatedText("flan.sellFail", ChatFormatting.DARK_RED));
                return false;
            }
            double price = blocks * value;
            if (handler.deposit(player, price, message)) {
                data.setAdditionalClaims(data.getAdditionalClaims() - blocks);
                message.accept(ClaimUtils.translatedText("flan.sellSuccess", blocks, price, ChatFormatting.GOLD));
            }
            return true;
        }
        message.accept(ClaimUtils.translatedText("flan.currencyMissing", ChatFormatting.DARK_RED));
        return false;
    }
}
