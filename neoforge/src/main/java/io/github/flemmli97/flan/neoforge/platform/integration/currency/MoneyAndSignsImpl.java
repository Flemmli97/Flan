package io.github.flemmli97.flan.neoforge.platform.integration.currency;

import io.github.flemmli97.flan.Flan;
import io.github.flemmli97.flan.api.economy.CurrencyHandler;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;

import java.util.function.Consumer;

public class MoneyAndSignsImpl implements CurrencyHandler {

    public static final Identifier ID = Identifier.fromNamespaceAndPath(Flan.MODID, "money_and_signs");

    public static void register() {
    }

    @Override
    public boolean withdraw(Player player, double price, Consumer<Component> message) {
        // Disabled cause the mod needs to update due to ResourceLocation -> Identifier
//        if (Flan.diceMCMoneySign) {
//            UUID uuid = player.getUUID();
//            MoneyWSD manager = MoneyWSD.get();
//            double bal = manager.getBalance(MoneyMod.AcctTypes.PLAYER.key, uuid);
//            double price = blocks * value;
//            if (bal >= price) {
//                PlayerClaimData data = PlayerClaimData.get(player);
//                data.setAdditionalClaims(data.getAdditionalClaims() + blocks);
//                manager.changeBalance(MoneyMod.AcctTypes.PLAYER.key, uuid, -price);
//                message.accept(ClaimUtils.translatedText("flan.buySuccess", blocks, price, ChatFormatting.GOLD));
//                return true;
//            }
//            message.accept(ClaimUtils.translatedText("flan.buyFail", ChatFormatting.DARK_RED));
//            return false;
//        }
        return false;
    }

    @Override
    public boolean deposit(Player player, double price, Consumer<Component> message) {
        // Disabled cause the mod needs to update due to ResourceLocation -> Identifier
//        if (Flan.diceMCMoneySign) {
//            PlayerClaimData data = PlayerClaimData.get(player);
//            if (data.getAdditionalClaims() - Math.max(0, data.usedClaimBlocks() - data.getClaimBlocks()) < blocks) {
//                message.accept(ClaimUtils.translatedText("flan.sellFail", ChatFormatting.DARK_RED));
//                return false;
//            }
//            double price = blocks * value;
//            MoneyWSD.get().changeBalance(MoneyMod.AcctTypes.PLAYER.key, player.getUUID(), price);
//            data.setAdditionalClaims(data.getAdditionalClaims() - blocks);
//            message.accept(ClaimUtils.translatedText("flan.sellSuccess", blocks, price, ChatFormatting.GOLD));
//            return true;
//        }
        return false;
    }
}
