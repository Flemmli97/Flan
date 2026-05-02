package io.github.flemmli97.flan.fabric.platform.integration.currency;

import com.gmail.sneakdevs.diamondeconomy.DiamondUtils;
import io.github.flemmli97.flan.Flan;
import io.github.flemmli97.flan.api.economy.CurrencyHandler;
import io.github.flemmli97.flan.api.economy.CurrencyRegistry;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;

import java.util.function.Consumer;

public class DiamondCurrencyImpl implements CurrencyHandler {

    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(Flan.MODID, "diamond_currency");

    public static void register() {
        CurrencyRegistry.add(ID, new DiamondCurrencyImpl());
    }

    @Override
    public boolean withdraw(Player player, double price, Consumer<Component> message) {
        if (DiamondUtils.getDatabaseManager().getBalanceFromUUID(player.getUUID().toString()) >= price) {
            DiamondUtils.getDatabaseManager().changeBalance(player.getUUID().toString(), -(int) price);
            return true;
        }
        return false;
    }

    @Override
    public boolean deposit(Player player, double price, Consumer<Component> message) {
        DiamondUtils.getDatabaseManager().changeBalance(player.getUUID().toString(), (int) price);
        return true;
    }
}