package io.github.flemmli97.flan.claim.attachment;

import io.github.flemmli97.flan.Flan;
import io.github.flemmli97.flan.claim.Claim;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public record ClaimAllowListKey<T>(ResourceLocation id, String translationKey, ResourceKey<Registry<T>> registry,
                                   Function<Claim, AllowedRegistryList<T>> factory) {

    private static final Map<ResourceLocation, ClaimAllowListKey<?>> MAP = new HashMap<>();

    public static final ClaimAllowListKey<Item> ITEM_USE = new ClaimAllowListKey<>("item", "flan.screenMenuItemUse",
            Registries.ITEM, claim -> AllowedRegistryList.ofItemLike(BuiltInRegistries.ITEM, claim));
    public static final ClaimAllowListKey<Block> BLOCK_BREAK = new ClaimAllowListKey<>("block_break", "flan.screenMenuBlockBreak",
            Registries.BLOCK, claim -> AllowedRegistryList.ofItemLike(BuiltInRegistries.BLOCK, claim));
    public static final ClaimAllowListKey<Block> BLOCK_USE = new ClaimAllowListKey<>("block_use", "flan.screenMenuBlockUse",
            Registries.BLOCK, claim -> AllowedRegistryList.ofItemLike(BuiltInRegistries.BLOCK, claim));
    public static final ClaimAllowListKey<EntityType<?>> ENTITY_ATTACK = new ClaimAllowListKey<>("entity_attack", "flan.screenMenuEntityAttack",
            Registries.ENTITY_TYPE, claim -> new AllowedRegistryList<>(BuiltInRegistries.ENTITY_TYPE, claim, AllowedRegistryList.ENTITY_AS_ITEM));
    public static final ClaimAllowListKey<EntityType<?>> ENTITY_USE = new ClaimAllowListKey<>("entity_use", "flan.screenMenuEntityUse",
            Registries.ENTITY_TYPE, claim -> new AllowedRegistryList<>(BuiltInRegistries.ENTITY_TYPE, claim, AllowedRegistryList.ENTITY_AS_ITEM));

    public ClaimAllowListKey(String path, String translationKey, ResourceKey<Registry<T>> registry, Function<Claim, AllowedRegistryList<T>> factory) {
        this(ResourceLocation.fromNamespaceAndPath(Flan.MODID, path), translationKey, registry, factory);
        if (MAP.put(this.id(), this) != null)
            throw new IllegalStateException("Key with id " + this.id() + " already exists!");
    }

    public static Map<ResourceLocation, ClaimAllowListKey<?>> keys() {
        return Map.copyOf(MAP);
    }

    public static ClaimAllowListKey<?> get(ResourceLocation id) {
        return MAP.get(id);
    }
}
