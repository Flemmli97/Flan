package io.github.flemmli97.flan.config;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JsonOps;
import io.github.flemmli97.flan.Flan;
import io.github.flemmli97.flan.api.permission.BuiltinPermission;
import io.github.flemmli97.flan.api.permission.PermissionManager;
import net.minecraft.advancements.predicates.DataComponentMatchers;
import net.minecraft.advancements.predicates.ItemPredicate;
import net.minecraft.advancements.predicates.MinMaxBounds;
import net.minecraft.core.HolderSet;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class ConfigUpdater {

    @SuppressWarnings("deprecation")
    private static final Map<Integer, Updater> UPDATER = Config.createHashMap(map -> {
        map.put(7, new Updater() {
            @Override
            public JsonObject configUpdater(MinecraftServer server, JsonObject oldVals) {
                return oldVals;
            }

            @Override
            public void postUpdater(MinecraftServer server, Config config) {
                config.defaultGroups.computeIfPresent("Co-Owner", (k, v) -> {
                    if (v.isEmpty()) {
                        PermissionManager.getInstance().getAll().forEach(p -> v.put(p.getId(), true));
                    }
                    return v;
                });
                config.defaultGroups.computeIfPresent("Visitor", (k, v) -> {
                    if (v.isEmpty()) {
                        v.put(BuiltinPermission.BED, true);
                        v.put(BuiltinPermission.DOOR, true);
                        v.put(BuiltinPermission.FENCEGATE, true);
                        v.put(BuiltinPermission.TRAPDOOR, true);
                        v.put(BuiltinPermission.BUTTONLEVER, true);
                        v.put(BuiltinPermission.PRESSUREPLATE, true);
                        v.put(BuiltinPermission.ENDERCHEST, true);
                        v.put(BuiltinPermission.ENCHANTMENTTABLE, true);
                        v.put(BuiltinPermission.ITEMFRAMEROTATE, true);
                        v.put(BuiltinPermission.PORTAL, true);
                        v.put(BuiltinPermission.TRADING, true);
                    }
                    return v;
                });
            }
        });
        map.put(6, new Updater() {
            @Override
            public JsonObject configUpdater(MinecraftServer server, JsonObject oldVals) {
                return oldVals;
            }

            @Override
            public void postUpdater(MinecraftServer server, Config config) {
                config.globalDefaultPerms.computeIfPresent("*", (k, v) -> {
                    v.put(BuiltinPermission.ALLOW_FLIGHT, Config.GlobalType.ALLTRUE);
                    v.put(BuiltinPermission.MAY_FLIGHT, Config.GlobalType.ALLFALSE);
                    return v;
                });
            }
        });
        map.put(5, (server, config) -> {
            Flan.debug("Updating config to version 5");
            JsonObject buySellHandler = ConfigHandler.fromJson(config, "buySellHandler");
            JsonArray buyItems = ConfigHandler.arrayFromJson(buySellHandler, "buyIngredients");
            List<JsonElement> toRemove = new ArrayList<>();
            DynamicOps<JsonElement> ops = server.registryAccess().createSerializationContext(JsonOps.INSTANCE);
            buyItems.forEach(k -> {
                JsonObject o = k.getAsJsonObject();
                if (o.has("ingredient")) {
                    try {
                        Ingredient ingredient = Ingredient.CODEC.parse(ops, buySellHandler.get("ingredient"))
                                .getOrThrow();
                        ItemPredicate pred = new ItemPredicate(Optional.of(HolderSet.direct(ingredient.items().toList())),
                                MinMaxBounds.Ints.ANY, DataComponentMatchers.ANY);
                        o.add("predicate", ItemPredicate.CODEC.encodeStart(ops, pred).getOrThrow());
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
                Ingredient legacy = buySellHandler.has("ingredient") ? Ingredient.CODEC.parse(ops, buySellHandler.get("ingredient"))
                                                                       .getOrThrow() : Ingredient.of();
                legacy = buySellHandler.has("sellIngredient") ? Ingredient.CODEC.parse(ops, buySellHandler.get("sellIngredient"))
                                                                .getOrThrow() : legacy;
                Optional<ItemStack> stack = legacy.items().map(ItemStack::new).findFirst();
                if (stack.isPresent()) {
                    buySellHandler.add("sellItems", BuySellHandler.ITEM_STACK_CODEC.encodeStart(ops, stack.get())
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

    public static JsonObject updateConfig(int preVersion, MinecraftServer server, JsonObject config) {
        for (Map.Entry<Integer, Updater> updater : UPDATER.entrySet()) {
            if (updater.getKey() > preVersion) {
                config = updater.getValue().configUpdater(server, config);
            }
        }
        return config;
    }

    public static void postUpdateConfig(int preVersion, MinecraftServer server, Config config) {
        for (Map.Entry<Integer, Updater> updater : UPDATER.entrySet()) {
            if (updater.getKey() > preVersion) {
                updater.getValue().postUpdater(server, config);
            }
        }
    }

    interface Updater {

        JsonObject configUpdater(MinecraftServer server, JsonObject oldVals);

        default void postUpdater(MinecraftServer server, Config config) {
        }
    }
}