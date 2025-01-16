package io.github.flemmli97.flan.config;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.mojang.datafixers.util.Pair;
import io.github.flemmli97.flan.claim.ClaimUtils;
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
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class BuySellHandler {

    private static int[] xpCalc;

    private Type buyType = Type.MONEY;
    private Type sellType = Type.MONEY;

    private float buyAmount = -1;
    private final List<BuyIngredient> buyIngredients = new ArrayList<>();

    private float sellAmount = -1;
    private Ingredient sellIngredient = Ingredient.EMPTY;

    public boolean buy(ServerPlayer player, int blocks, Consumer<Component> message) {
        if (this.buyAmount == -1 && this.buyType != Type.ITEM) {
            message.accept(ClaimUtils.translatedText("flan.buyDisabled", ChatFormatting.DARK_RED));
            return false;
        }
        PlayerClaimData data = PlayerClaimData.get(player);
        if (ConfigHandler.CONFIG.maxBuyBlocks >= 0 && data.getAdditionalClaims() + blocks > ConfigHandler.CONFIG.maxBuyBlocks) {
            message.accept(ClaimUtils.translatedText("flan.buyLimit", ChatFormatting.DARK_RED));
            return false;
        }
        switch (this.buyType) {
            case MONEY -> {
                return CommandCurrency.INSTANCE.buyClaimBlocks(player, blocks, this.buyAmount, message);
            }
            case ITEM -> {
                if (this.buyIngredients.isEmpty()) {
                    message.accept(ClaimUtils.translatedText("flan.buyDisabled", ChatFormatting.DARK_RED));
                    return false;
                }
                float payed = 0;
                List<Pair<ItemStack, Integer>> matching = new ArrayList<>();
                // Check if player can pay the amount
                check:
                for (BuyIngredient ing : this.buyIngredients) {
                    for (ItemStack stack : player.getInventory().items) {
                        if (ing.ingredient().test(stack)) {
                            if (stack.isDamageableItem()) {
                                if (stack.getDamageValue() != 0) {
                                    continue;
                                }
                            }
                            //Ignore "special" items
                            if (!this.isJustRenamedItem(stack)) {
                                continue;
                            }
                            float toPay = blocks - payed;
                            int count = Math.min(stack.getCount(), Mth.ceil(toPay / ing.amount()));
                            float amount = count * ing.amount();
                            payed += amount;
                            matching.add(Pair.of(stack, count));
                            if (payed >= blocks)
                                break check;
                        }
                    }
                }
                if (payed < blocks) {
                    message.accept(ClaimUtils.translatedText("flan.buyFailItem", ChatFormatting.DARK_RED));
                    return false;
                }
                // Finally remove the items
                int count = 0;
                for (Pair<ItemStack, Integer> stack : matching) {
                    stack.getFirst().shrink(stack.getSecond());
                    count += stack.getSecond();
                }
                data.setAdditionalClaims(data.getAdditionalClaims() + blocks);
                message.accept(ClaimUtils.translatedText("flan.buySuccessItem", blocks, count));
                return true;
            }
            case XP -> {
                int deduct = Mth.ceil(blocks * this.buyAmount);
                if (deduct < totalXpPointsForLevel(player.experienceLevel) + player.experienceProgress * xpForLevel(player.experienceLevel + 1)) {
                    player.giveExperiencePoints(-deduct);
                    data.setAdditionalClaims(data.getAdditionalClaims() + blocks);
                    message.accept(ClaimUtils.translatedText("flan.buySuccessXP", blocks, deduct));
                    return true;
                }
                message.accept(ClaimUtils.translatedText("flan.buyFailXP", ChatFormatting.DARK_RED));
                return false;
            }
        }
        return false;
    }

    public boolean sell(ServerPlayer player, int blocks, Consumer<Component> message) {
        if (this.sellAmount == -1) {
            message.accept(ClaimUtils.translatedText("flan.sellDisabled", ChatFormatting.DARK_RED));
            return false;
        }
        PlayerClaimData data = PlayerClaimData.get(player);
        if (data.getAdditionalClaims() - Math.max(0, data.usedClaimBlocks() - data.getClaimBlocks()) < blocks) {
            message.accept(ClaimUtils.translatedText("flan.sellFail", ChatFormatting.DARK_RED));
            return false;
        }
        switch (this.sellType) {
            case MONEY -> {
                return CommandCurrency.INSTANCE.sellClaimBlocks(player, blocks, this.sellAmount, message);
            }
            case ITEM -> {
                ItemStack[] stacks = this.sellIngredient.getItems();
                if (this.sellIngredient.isEmpty()) {
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
                message.accept(ClaimUtils.translatedText("flan.sellSuccessItem", blocks, amount, ClaimUtils.translatedText(stack.getDescriptionId()).withStyle(ChatFormatting.AQUA)));
                return true;
            }
            case XP -> {
                int amount = Mth.floor(blocks * this.buyAmount);
                player.giveExperiencePoints(amount);
                data.setAdditionalClaims(data.getAdditionalClaims() - blocks);
                message.accept(ClaimUtils.translatedText("flan.sellSuccessXP", blocks, amount));
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
        obj.addProperty("buyValue", this.buyAmount);
        JsonArray buyArr = new JsonArray();
        this.buyIngredients.forEach((b -> {
            JsonObject buyObj = new JsonObject();
            buyObj.addProperty("amount", b.amount());
            buyObj.add("ingredient", b.ingredient().toJson());
            buyArr.add(buyObj);
        }));
        obj.add("buyIngredients", buyArr);

        obj.addProperty("sellType", this.sellType.toString());
        obj.addProperty("sellValue", this.sellAmount);
        obj.add("sellIngredient", this.sellIngredient.toJson());
        return obj;
    }

    public void fromJson(JsonObject object) {
        this.buyType = Type.valueOf(ConfigHandler.fromJson(object, "buyType", this.buyType.toString()));
        this.buyAmount = object.has("buyValue") ? object.get("buyValue").getAsFloat() : this.buyAmount;
        this.buyIngredients.clear();
        JsonArray obj = ConfigHandler.arryFromJson(object, "buyIngredients");
        obj.forEach(k -> {
            JsonObject o = k.getAsJsonObject();
            try {
                Ingredient ingredient = o.has("ingredient") ? Ingredient.fromJson(o.get("ingredient"))
                        : Ingredient.EMPTY;
                if (ingredient != Ingredient.EMPTY) {
                    float amount = o.get("amount").getAsFloat();
                    this.buyIngredients.add(new BuyIngredient(amount, ingredient));
                }
            } catch (JsonParseException ignored) {
            }
        });
        this.buyIngredients.sort(BuyIngredient::compareTo);

        this.sellType = Type.valueOf(ConfigHandler.fromJson(object, "sellType", this.sellType.toString()));
        this.sellAmount = object.has("sellValue") ? object.get("sellValue").getAsFloat() : this.sellAmount;
        try {
            Ingredient legacy = object.has("ingredient") ? Ingredient.fromJson(object.get("ingredient"))
                    : Ingredient.EMPTY;
            this.sellIngredient = object.has("sellIngredient") ? Ingredient.fromJson(object.get("sellIngredient"))
                    : legacy;
        } catch (JsonParseException e) {
            this.sellIngredient = Ingredient.EMPTY;
        }
    }

    enum Type {
        MONEY,
        ITEM,
        XP
    }

    record BuyIngredient(float amount, Ingredient ingredient) implements Comparable<BuyIngredient> {
        @Override
        public int compareTo(@NotNull BuySellHandler.BuyIngredient buyIngredient) {
            return Float.compare(buyIngredient.amount, this.amount);
        }
    }
}
