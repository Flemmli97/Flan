package io.github.flemmli97.flan.config;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.JsonOps;
import io.github.flemmli97.flan.Flan;
import io.github.flemmli97.flan.api.permission.BuiltinPermission;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class ConfigUpdater {

    private static final Map<Integer, Updater> UPDATER = Config.createHashMap(map -> {
        map.put(6, new Updater() {
            @Override
            public JsonObject configUpdater(JsonObject oldVals) {
                return oldVals;
            }

            @Override
            public void postUpdater(Config config) {
                config.globalDefaultPerms.computeIfPresent("*", (k, v) -> {
                    v.put(BuiltinPermission.ALLOW_FLIGHT, Config.GlobalType.ALLTRUE);
                    v.put(BuiltinPermission.MAY_FLIGHT, Config.GlobalType.ALLFALSE);
                    return v;
                });
            }
        });
        map.put(5, config -> {
            Flan.debug("Updating config to version 5");
            JsonObject buySellHandler = ConfigHandler.fromJson(config, "buySellHandler");
            JsonArray buyItems = ConfigHandler.arrayFromJson(buySellHandler, "buyIngredients");
            List<JsonElement> toRemove = new ArrayList<>();
            buyItems.forEach(k -> {
                JsonObject o = k.getAsJsonObject();
                if (o.has("ingredient")) {
                    try {
                        Ingredient ingredient = Ingredient.CODEC.parse(JsonOps.INSTANCE, buySellHandler.get("ingredient"))
                                .getOrThrow();
                        ItemPredicate pred = ItemPredicate.Builder.item()
                                .of(Arrays.stream(ingredient.getItems()).map(ItemStack::getItem).toArray(ItemLike[]::new))
                                .build();
                        o.add("predicate", ItemPredicate.CODEC.encodeStart(JsonOps.INSTANCE, pred).getOrThrow());
                        o.remove("ingredient");
                    } catch (Exception ignored) {
                    }
                }
                if (!o.has("predicate") || !o.has("amount")) {
                    Flan.error("Unable to update buy handler ", o);
                    toRemove.add(k);
                }
            });
            toRemove.forEach(buyItems::remove);
            buySellHandler.add("buyItems", buyItems);

            if (buySellHandler.has("ingredient") || buySellHandler.has("sellIngredient")) {
                Ingredient legacy = buySellHandler.has("ingredient") ? Ingredient.CODEC.parse(JsonOps.INSTANCE, buySellHandler.get("ingredient"))
                        .getOrThrow() : Ingredient.EMPTY;
                legacy = buySellHandler.has("sellIngredient") ? Ingredient.CODEC.parse(JsonOps.INSTANCE, buySellHandler.get("sellIngredient"))
                        .getOrThrow() : legacy;
                if (!legacy.isEmpty() && !legacy.getItems()[0].isEmpty()) {
                    buySellHandler.add("sellItems", BuySellHandler.ITEM_STACK_CODEC.encodeStart(JsonOps.INSTANCE, legacy.getItems()[0])
                            .result().map(e -> {
                                JsonArray arr = new JsonArray();
                                JsonObject val = new JsonObject();
                                val.add("amount", buySellHandler.get("sellValue"));
                                val.add("item", e);
                                arr.add(val);
                                return arr;
                            }).orElse(new JsonArray()));
                } else {
                    buySellHandler.add("sellItems", new JsonArray());
                }
            }
            return config;
        });
    });

    public static JsonObject updateConfig(int preVersion, JsonObject config) {
        for (Map.Entry<Integer, Updater> updater : UPDATER.entrySet()) {
            if (updater.getKey() > preVersion) {
                config = updater.getValue().configUpdater(config);
            }
        }
        return config;
    }

    public static void postUpdateConfig(int preVersion, Config config) {
        for (Map.Entry<Integer, Updater> updater : UPDATER.entrySet()) {
            if (updater.getKey() > preVersion) {
                updater.getValue().postUpdater(config);
            }
        }
    }

    interface Updater {

        JsonObject configUpdater(JsonObject oldVals);

        default void postUpdater(Config config) {
        }
    }
}