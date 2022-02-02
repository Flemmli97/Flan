package io.github.flemmli97.flan.config;

import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import io.github.flemmli97.flan.claim.PermHelper;
import io.github.flemmli97.flan.integration.gunpowder.CommandCurrency;
import io.github.flemmli97.flan.player.PlayerClaimData;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.recipe.Ingredient;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.MathHelper;

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

    public boolean buy(ServerPlayerEntity player, int blocks, Consumer<Text> message) {
        if (this.buyAmount == -1) {
            message.accept(PermHelper.simpleColoredText(ConfigHandler.langManager.get("buyDisabled"), Formatting.DARK_RED));
            return false;
        }
        PlayerClaimData data = PlayerClaimData.get(player);
        if (ConfigHandler.config.maxBuyBlocks >= 0 && data.getAdditionalClaims() + blocks > ConfigHandler.config.maxBuyBlocks) {
            message.accept(PermHelper.simpleColoredText(ConfigHandler.langManager.get("buyLimit"), Formatting.DARK_RED));
            return false;
        }
        switch (this.buyType) {
            case MONEY -> {
                return CommandCurrency.buyClaimBlocks(player, blocks, this.buyAmount, message);
            }
            case ITEM -> {
                int deduct = MathHelper.ceil(blocks * this.buyAmount);
                List<ItemStack> matching = new ArrayList<>();
                int i = 0;
                for (ItemStack stack : player.getInventory().main) {
                    if (this.ingredient.test(stack)) {
                        if (stack.isDamageable()) {
                            if (stack.getDamage() != 0) {
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
                    message.accept(PermHelper.simpleColoredText(ConfigHandler.langManager.get("buyFailItem"), Formatting.DARK_RED));
                    return false;
                }
                i = deduct;
                for (ItemStack stack : matching) {
                    if (i > stack.getCount()) {
                        int count = stack.getCount();
                        stack.setCount(0);
                        i -= count;
                    } else {
                        stack.decrement(i);
                        break;
                    }
                }
                data.setAdditionalClaims(data.getAdditionalClaims() + blocks);
                message.accept(PermHelper.simpleColoredText(String.format(ConfigHandler.langManager.get("buySuccessItem"), blocks, deduct)));
                return true;
            }
            case XP -> {
                int deduct = MathHelper.ceil(blocks * this.buyAmount);
                if (deduct < totalXpPointsForLevel(player.experienceLevel) + player.experienceProgress * xpForLevel(player.experienceLevel + 1)) {
                    player.addExperience(-deduct);
                    data.setAdditionalClaims(data.getAdditionalClaims() + blocks);
                    message.accept(PermHelper.simpleColoredText(String.format(ConfigHandler.langManager.get("buySuccessXP"), blocks, deduct)));
                    return true;
                }
                message.accept(PermHelper.simpleColoredText(ConfigHandler.langManager.get("buyFailXP"), Formatting.DARK_RED));
                return false;
            }
        }
        return false;
    }

    public boolean sell(ServerPlayerEntity player, int blocks, Consumer<Text> message) {
        if (this.sellAmount == -1) {
            message.accept(PermHelper.simpleColoredText(ConfigHandler.langManager.get("sellDisabled"), Formatting.DARK_RED));
            return false;
        }
        PlayerClaimData data = PlayerClaimData.get(player);
        if (data.getAdditionalClaims() - Math.max(0, data.usedClaimBlocks() - data.getClaimBlocks()) < blocks) {
            message.accept(PermHelper.simpleColoredText(ConfigHandler.langManager.get("sellFail"), Formatting.DARK_RED));
            return false;
        }
        switch (this.sellType) {
            case MONEY -> {
                return CommandCurrency.sellClaimBlocks(player, blocks, this.sellAmount, message);
            }
            case ITEM -> {
                if (this.ingredient.isEmpty()) {
                    return false;
                }
                int amount = MathHelper.floor(blocks * this.sellAmount);
                ItemStack stack = this.ingredient.getMatchingStacksClient()[0];
                while (amount > 0) {
                    ItemStack toGive = stack.copy();
                    if (amount > 64) {
                        toGive.setCount(64);
                        amount -= 64;
                    } else {
                        toGive.setCount(amount);
                        amount = 0;
                    }
                    boolean bl = player.getInventory().insertStack(toGive);
                    if (!bl || !toGive.isEmpty()) {
                        ItemEntity itemEntity = player.dropItem(toGive, false);
                        if (itemEntity != null) {
                            itemEntity.setPickupDelay(0);
                            itemEntity.setOwner(player.getUuid());
                        }
                    }
                }
                data.setAdditionalClaims(data.getAdditionalClaims() - blocks);
                message.accept(new TranslatableText(ConfigHandler.langManager.get("sellSuccessItem"), blocks, amount, new TranslatableText(stack.getTranslationKey()).formatted(Formatting.AQUA)));
                return true;
            }
            case XP -> {
                int amount = MathHelper.floor(blocks * this.buyAmount);
                player.addExperience(amount);
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
        if (stack.getTag().getKeys()
                .stream().allMatch(s -> s.equals("Damage") || s.equals("RepairCost") || s.equals("display"))) {
            NbtCompound tag = stack.getTag().getCompound("display");
            return tag.contains("Name") && tag.getSize() == 1;
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
     * See {@link PlayerEntity#getNextLevelExperience()}
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
