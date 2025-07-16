package io.github.flemmli97.flan.config;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.flemmli97.flan.claim.ClaimUtils;
import io.github.flemmli97.flan.gui.ServerScreenHelper;
import io.github.flemmli97.flan.platform.integration.currency.CommandCurrency;
import io.github.flemmli97.flan.player.PlayerClaimData;
import net.minecraft.ChatFormatting;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.Style;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

public class BuySellHandler {

    public static final Codec<ItemStack> ITEM_STACK_CODEC = RecordCodecBuilder.create((instance) ->
            instance.group(BuiltInRegistries.ITEM.byNameCodec().fieldOf("id").forGetter(ItemStack::getItem),
                    DataComponentPatch.CODEC.optionalFieldOf("components").forGetter((stack) -> stack.getComponentsPatch().isEmpty() ? Optional.empty() : Optional.of(stack.getComponentsPatch()))
            ).apply(instance, (item, components) -> {
                ItemStack stack = new ItemStack(item, 1);
                components.ifPresent(stack::applyComponents);
                return stack;
            }));

    private static ItemStack fromResults(List<ItemResult> stacks) {
        ItemStack stack = new ItemStack(Items.EMERALD);
        stack.set(DataComponents.CUSTOM_NAME, Component.translatable("flan.buy_sell.item")
                .setStyle(Style.EMPTY.withItalic(false).applyFormat(ChatFormatting.AQUA)));
        List<Component> stackComp = new ArrayList<>();
        for (ItemResult r : stacks) {
            stackComp.add(Component.translatable("flan.buy_sell.item.amount", r.stack().getItemName(), r.amount(), r.value())
                    .setStyle(Style.EMPTY.withItalic(false).applyFormat(ChatFormatting.GREEN)));
        }
        ServerScreenHelper.addLore(stack, stackComp);
        return stack;
    }

    private static int[] xpCalc;

    private Type buyType = Type.MONEY;
    private Type sellType = Type.MONEY;

    private float buyAmount = -1;
    private final List<BuyItem> buyItems = new ArrayList<>();

    private float sellAmount = -1;
    private final List<SellItem> sellItems = new ArrayList<>();

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
                if (this.buyItems.isEmpty()) {
                    message.accept(ClaimUtils.translatedText("flan.buyDisabled", ChatFormatting.DARK_RED));
                    return false;
                }
                int payed = 0;
                List<ItemResult> bought = new ArrayList<>();
                List<Pair<ItemStack, Integer>> matching = new ArrayList<>();
                // Check if player can pay the amount
                check:
                for (BuyItem ing : this.buyItems) {
                    for (ItemStack stack : player.getInventory()) {
                        if (this.matches(ing.predicate(), stack)) {
                            if (stack.isDamageableItem()) {
                                if (stack.getDamageValue() != 0) {
                                    continue;
                                }
                            }
                            float toPay = blocks - payed;
                            int count = Math.min(stack.getCount(), (int) (toPay / ing.amount()));
                            float amount = count * ing.amount();
                            payed += amount;
                            if (count > 0) {
                                bought.add(new ItemResult(stack.copy(), count, ing.amount()));
                                matching.add(Pair.of(stack, count));
                            }
                            if (payed >= blocks)
                                break check;
                        }
                    }
                }
                if (payed == 0 || matching.isEmpty()) {
                    message.accept(ClaimUtils.translatedText("flan.buyFailItem", ChatFormatting.DARK_RED));
                    return false;
                }
                // Finally remove the items
                for (Pair<ItemStack, Integer> stack : matching) {
                    stack.getFirst().shrink(stack.getSecond());
                }
                Component items = Component.translatable("flan.buy_sell.items")
                        .withStyle(Style.EMPTY.applyFormat(ChatFormatting.AQUA)
                                .withHoverEvent(new HoverEvent.ShowItem(fromResults(bought))));
                data.setAdditionalClaims(data.getAdditionalClaims() + payed);
                message.accept(ClaimUtils.translatedText("flan.buySuccessItem", payed, items));
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
        if (this.sellAmount == -1 && this.sellType != Type.ITEM) {
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
                if (this.sellItems.isEmpty()) {
                    message.accept(ClaimUtils.translatedText("flan.sellDisabled", ChatFormatting.DARK_RED));
                    return false;
                }
                int toSell = blocks;
                List<ItemResult> soldStacks = new ArrayList<>();
                for (SellItem item : this.sellItems) {
                    int count = (int) (toSell / item.amount());
                    float amount = count * item.amount();
                    toSell -= amount;
                    while (count > 0) {
                        ItemStack toGive = item.item().copy();
                        if (count > 64) {
                            toGive.setCount(64);
                            count -= 64;
                        } else {
                            toGive.setCount(count);
                            count = 0;
                        }
                        soldStacks.add(new ItemResult(toGive.copy(), toGive.getCount(), item.amount()));
                        boolean bl = player.getInventory().add(toGive);
                        if (!bl || !toGive.isEmpty()) {
                            ItemEntity itemEntity = player.drop(toGive, false);
                            if (itemEntity != null) {
                                itemEntity.setNoPickUpDelay();
                                itemEntity.setTarget(player.getUUID());
                            }
                        }
                    }
                    if (toSell <= 0)
                        break;
                }
                int sold = (blocks - toSell);
                Component items = Component.translatable("flan.buy_sell.items")
                        .withStyle(Style.EMPTY.applyFormat(ChatFormatting.AQUA)
                                .withHoverEvent(new HoverEvent.ShowItem(fromResults(soldStacks))));
                data.setAdditionalClaims(data.getAdditionalClaims() - sold);
                message.accept(ClaimUtils.translatedText("flan.sellSuccessItem", sold, items));
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

    private boolean matches(ItemPredicate predicate, ItemStack stack) {
        if (predicate.components().exact().alwaysMatches() || predicate.components().isEmpty()) {
            if (stack.getComponentsPatch()
                    .entrySet().stream()
                    .anyMatch(e -> e.getKey() != DataComponents.CUSTOM_NAME
                            && e.getKey() != DataComponents.REPAIR_COST && e.getKey() != DataComponents.DAMAGE)) {
                return false;
            }
        }
        return predicate.test(stack);
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

    public JsonObject toJson(MinecraftServer server) {
        JsonObject obj = new JsonObject();
        obj.addProperty("buyType", this.buyType.toString());
        obj.addProperty("buyValue", this.buyAmount);
        JsonArray buyArr = new JsonArray();
        DynamicOps<JsonElement> ops = server.registryAccess().createSerializationContext(JsonOps.INSTANCE);
        this.buyItems.forEach((b -> {
            JsonObject buyObj = new JsonObject();
            buyObj.addProperty("amount", b.amount());
            buyObj.add("predicate", ItemPredicate.CODEC.encodeStart(ops, b.predicate()).getOrThrow());
            buyArr.add(buyObj);
        }));
        obj.add("buyItems", buyArr);

        obj.addProperty("sellType", this.sellType.toString());
        obj.addProperty("sellValue", this.sellAmount);
        JsonArray sellArr = new JsonArray();
        this.sellItems.forEach((b -> {
            JsonObject buyObj = new JsonObject();
            buyObj.addProperty("amount", b.amount());
            buyObj.add("item", ITEM_STACK_CODEC.encodeStart(ops, b.item)
                    .getOrThrow());
            sellArr.add(buyObj);
        }));
        obj.add("sellItems", sellArr);
        return obj;
    }

    public void fromJson(JsonObject object, MinecraftServer server) {
        this.buyType = Type.valueOf(ConfigHandler.fromJson(object, "buyType", this.buyType.toString()));
        this.buyAmount = object.has("buyValue") ? object.get("buyValue").getAsFloat() : this.buyAmount;
        this.buyItems.clear();
        JsonArray buyArr = ConfigHandler.arryFromJson(object, "buyItems");
        DynamicOps<JsonElement> ops = server.registryAccess().createSerializationContext(JsonOps.INSTANCE);
        buyArr.forEach(k -> {
            JsonObject o = k.getAsJsonObject();
            this.buyItems.add(new BuyItem(o.get("amount").getAsFloat(), ItemPredicate.CODEC.parse(ops, o.get("predicate")).getOrThrow()));
        });
        this.buyItems.sort(BuyItem::compareTo);

        this.sellType = Type.valueOf(ConfigHandler.fromJson(object, "sellType", this.sellType.toString()));
        this.sellAmount = object.has("sellValue") ? object.get("sellValue").getAsFloat() : this.sellAmount;
        this.sellItems.clear();
        JsonArray sellArr = ConfigHandler.arryFromJson(object, "sellItems");
        sellArr.forEach(k -> {
            JsonObject o = k.getAsJsonObject();
            this.sellItems.add(new SellItem(o.get("amount").getAsFloat(), ITEM_STACK_CODEC.parse(ops, o.get("item"))
                    .getOrThrow()));
        });
        this.sellItems.sort(SellItem::compareTo);
    }

    enum Type {
        MONEY,
        ITEM,
        XP
    }

    record BuyItem(float amount, ItemPredicate predicate) implements Comparable<BuyItem> {

        @Override
        public int compareTo(@NotNull BuyItem buyItem) {
            return Float.compare(buyItem.amount, this.amount);
        }
    }

    record SellItem(float amount, ItemStack item) implements Comparable<SellItem> {

        @Override
        public int compareTo(@NotNull SellItem item) {
            return Float.compare(item.amount, this.amount);
        }
    }

    record ItemResult(ItemStack stack, int amount, float value) {

    }
}
