package io.github.flemmli97.flan.integration.currency.forge;

import dicemc.money.MoneyMod;
import dicemc.money.storage.MoneyWSD;
import io.github.flemmli97.flan.Flan;
import io.github.flemmli97.flan.claim.PermHelper;
import io.github.flemmli97.flan.config.ConfigHandler;
import io.github.flemmli97.flan.player.PlayerClaimData;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.UUID;
import java.util.function.Consumer;

public class CommandCurrencyImpl {

    public static boolean sellClaimBlocks(ServerPlayerEntity player, int blocks, float value, Consumer<Text> message) {
        if (value == -1) {
            message.accept(PermHelper.simpleColoredText(ConfigHandler.langManager.get("sellDisabled"), Formatting.DARK_RED));
            return false;
        }
        if (Flan.diceMCMoneySign) {
            PlayerClaimData data = PlayerClaimData.get(player);
            if (data.getAdditionalClaims() - Math.max(0, data.usedClaimBlocks() - data.getClaimBlocks()) < blocks) {
                message.accept(PermHelper.simpleColoredText(ConfigHandler.langManager.get("sellFail"), Formatting.DARK_RED));
                return false;
            }
            double price = blocks * value;
            MoneyWSD.get(player.getServerWorld()).changeBalance(MoneyMod.AcctTypes.PLAYER.key, player.getUuid(), price);
            data.setAdditionalClaims(data.getAdditionalClaims() - blocks);
            message.accept(PermHelper.simpleColoredText(String.format(ConfigHandler.langManager.get("sellSuccess"), blocks, price), Formatting.GOLD));
            return true;
        }
        message.accept(PermHelper.simpleColoredText(ConfigHandler.langManager.get("currencyMissing"), Formatting.DARK_RED));
        return false;
    }

    public static boolean buyClaimBlocks(ServerPlayerEntity player, int blocks, float value, Consumer<Text> message) {
        if (value == -1) {
            message.accept(PermHelper.simpleColoredText(ConfigHandler.langManager.get("buyDisabled"), Formatting.DARK_RED));
            return false;
        }
        if (Flan.diceMCMoneySign) {
            UUID uuid = player.getUuid();
            MoneyWSD manager = MoneyWSD.get(player.getServerWorld());
            double bal = manager.getBalance(MoneyMod.AcctTypes.PLAYER.key, uuid);
            double price = blocks * value;
            if (bal >= price) {
                PlayerClaimData data = PlayerClaimData.get(player);
                data.setAdditionalClaims(data.getAdditionalClaims() + blocks);
                manager.changeBalance(MoneyMod.AcctTypes.PLAYER.key, uuid, -price);
                message.accept(PermHelper.simpleColoredText(String.format(ConfigHandler.langManager.get("buySuccess"), blocks, price), Formatting.GOLD));
                return true;
            }
            message.accept(PermHelper.simpleColoredText(ConfigHandler.langManager.get("buyFail"), Formatting.DARK_RED));
            return false;
        }
        message.accept(PermHelper.simpleColoredText(ConfigHandler.langManager.get("currencyMissing"), Formatting.DARK_RED));
        return false;
    }
}
