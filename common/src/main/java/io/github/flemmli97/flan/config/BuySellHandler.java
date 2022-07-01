package io.github.flemmli97.flan.config;

import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import io.github.flemmli97.flan.claim.PermHelper;
import io.github.flemmli97.flan.platform.integration.currency.CommandCurrency;
import io.github.flemmli97.flan.player.PlayerClaimData;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class BuySellHandler {

    private static int[] xpCalc;

    private Type buyType = Type.MONEY;
    private Type sellType = Type.MONEY;

    private float buyAmount = -1;
    private float sellAmount = -1;

    private Ingredient ingredient = Ingredient.EMPTY;

    public boolean buy(ServerPlayer player, int blocks, Consumer<Component> message) {
        if (this.buyAmount == -1) {
            message.accept(PermHelper.simpleColoredText(ConfigHandler.langManager.get("buyDisabled"), ChatFormatting.DARK_RED));
            return false;
        }
        PlayerClaimData data = PlayerClaimData.get(player);
        if (ConfigHandler.config.maxBuyBlocks >= 0 && data.getAdditionalClaims() + blocks > ConfigHandler.config.maxBuyBlocks) {
            message.accept(PermHelper.simpleColoredText(ConfigHandler.langManager.get("buyLimit"), ChatFormatting.DARK_RED));
            return false;
        }
        switch (this.buyType) {
            case MONEY -> {
                return CommandCurrency.INSTANCE.buyClaimBlocks(player, blocks, this.buyAmount, message);
            }
            case ITEM -> {
                int deduct = Mth.ceil(blocks * this.buyAmount);
                List<ItemStack> matching = new ArrayList<>();
                int i = 0;
                for (ItemStack stack : player.getInventory().items) {
                    if (this.ingredient.test(stack)) {
                        if (stack.isDamageableItem()) {
                            if (stack.getDamageValue() != 0) {
                                continue;
                            }
                        }
                        //Ignore "special" items
                        if (!this.isJustRenamedItem(stack)) {
                            continue;
                        }
                        matching.add(stack);
                        i += stack.getCount();
                    }
                }
                if (i < deduct) {
                    message.accept(PermHelper.simpleColoredText(ConfigHandler.langManager.get("buyFailItem"), ChatFormatting.DARK_RED));
                    return false;
                }
                i = deduct;
                for (ItemStack stack : matching) {
                    if (i > stack.getCount()) {
                        int count = stack.getCount();
                        stack.setCount(0);
                        i -= count;
                    } else {
                        stack.shrink(i);
                        break;
                    }
                }
                data.setAdditionalClaims(data.getAdditionalClaims() + blocks);
                message.accept(PermHelper.simpleColoredText(String.format(ConfigHandler.langManager.get("buySuccessItem"), blocks, deduct)));
                return true;
            }
            case XP -> {
                int deduct = Mth.ceil(blocks * this.buyAmount);
                if (deduct < totalXpPointsForLevel(player.experienceLevel) + player.experienceProgress * xpForLevel(player.experienceLevel + 1)) {
                    player.giveExperiencePoints(-deduct);
                    data.setAdditionalClaims(data.getAdditionalClaims() + blocks);
                    message.accept(PermHelper.simpleColoredText(String.format(ConfigHandler.langManager.get("buySuccessXP"), blocks, deduct)));
                    return true;
                }
                message.accept(PermHelper.simpleColoredText(ConfigHandler.langManager.get("buyFailXP"), ChatFormatting.DARK_RED));
                return false;
            }
        }
        return false;
    }

    public boolean sell(ServerPlayer player, int blocks, Consumer<Component> message) {
        if (this.sellAmount == -1) {
            message.accept(PermHelper.simpleColoredText(ConfigHandler.langManager.get("sellDisabled"), ChatFormatting.DARK_RED));
            return false;
        }
        PlayerClaimData data = PlayerClaimData.get(player);
        if (data.getAdditionalClaims() - Math.max(0, data.usedClaimBlocks() - data.getClaimBlocks()) < blocks) {
            message.accept(PermHelper.simpleColoredText(ConfigHandler.langManager.get("sellFail"), ChatFormatting.DARK_RED));
            return false;
        }
        switch (this.sellType) {
            case MONEY -> {
                return CommandCurrency.INSTANCE.sellClaimBlocks(player, blocks, this.sellAmount, message);
            }
            case ITEM -> {
                ItemStack[] stacks = this.ingredient.getItems();
                if (this.ingredient.isEmpty()) {
                    return false;
                }
                int amount = Mth.floor(blocks * this.sellAmount);
                ItemStack stack = stacks[0];
                while (amount > 0) {
                    ItemStack toGive = stack.copy();
                    if (amount > 64) {
                        toGive.setCount(64);
                        amount -= 64;
                    } else {
                        toGive.setCount(amount);
                        amount = 0;
                    }
                    boolean bl = player.getInventory().add(toGive);
                    if (!bl || !toGive.isEmpty()) {
                        ItemEntity itemEntity = player.drop(toGive, false);
                        if (itemEntity != null) {
                            itemEntity.setNoPickUpDelay();
                            itemEntity.setOwner(player.getUUID());
                        }
                    }
                }
                data.setAdditionalClaims(data.getAdditionalClaims() - blocks);
                message.accept(Component.translatable(ConfigHandler.langManager.get("sellSuccessItem"), blocks, amount, Component.translatable(stack.getDescriptionId()).withStyle(ChatFormatting.AQUA)));
                return true;
            }
            case XP -> {
                int amount = Mth.floor(blocks * this.buyAmount);
                player.giveExperiencePoints(amount);
                data.setAdditionalClaims(data.getAdditionalClaims() - blocks);
                message.accept(PermHelper.simpleColoredText(String.format(ConfigHandler.langManager.get("sellSuccessXP"), blocks, amount)));
                return false;
            }
        }
        return false;
    }

    private boolean isJustRenamedItem(ItemStack stack) {
        if (!stack.hasTag())
            return true;
        if (stack.getTag().getAllKeys()
                .stream().allMatch(s -> s.equals("Damage") || s.equals("RepairCost") || s.equals("display"))) {
            CompoundTag tag = stack.getTag().getCompound("display");
            return tag.contains("Name") && tag.size() == 1;
        }
        return true;
    }

    private static int totalXpPointsForLevel(int level) {
        if (xpCalc == null || level > xpCalc.length) {
            xpCalc = new int[level + 50];
            xpCalc[0] = 0;
            for (int i = 1; i < xpCalc.length; i++) {
                xpCalc[i] = xpForLevel(i) + xpCalc[i - 1];
            }
        }
        return xpCalc[level];
    }

    /**
     * See {@link Player#getXpNeededForNextLevel()}
     */
    private static int xpForLevel(int level) {
        level -= 1;
        if (level >= 30) {
            return 112 + (level - 30) * 9;
        }
        if (level >= 15) {
            return 37 + (level - 15) * 5;
        }
        return 7 + level * 2;
    }

    public JsonObject toJson() {
        JsonObject obj = new JsonObject();
        obj.addProperty("buyType", this.buyType.toString());
        obj.addProperty("sellType", this.sellType.toString());
        obj.addProperty("buyValue", this.buyAmount);
        obj.addProperty("sellValue", this.sellAmount);
        obj.add("ingredient", this.ingredient.toJson());
        return obj;
    }

    public void fromJson(JsonObject object) {
        this.buyType = Type.valueOf(ConfigHandler.fromJson(object, "buyType", this.buyType.toString()));
        this.sellType = Type.valueOf(ConfigHandler.fromJson(object, "sellType", this.sellType.toString()));
        this.buyAmount = object.has("buyValue") ? object.get("buyValue").getAsFloat() : this.buyAmount;
        this.sellAmount = object.has("sellValue") ? object.get("sellValue").getAsFloat() : this.sellAmount;
        try {
            this.ingredient = object.has("ingredient") ? Ingredient.fromJson(object.get("ingredient")) : Ingredient.EMPTY;
        } catch (JsonParseException e) {
            this.ingredient = Ingredient.EMPTY;
        }
    }

    enum Type {
        MONEY,
        ITEM,
        XP
    }
}
