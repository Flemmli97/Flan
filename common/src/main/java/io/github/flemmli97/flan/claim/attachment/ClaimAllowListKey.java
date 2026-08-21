package io.github.flemmli97.flan.claim.attachment;

import com.google.common.collect.ImmutableMap;
import io.github.flemmli97.flan.Flan;
import io.github.flemmli97.flan.claim.Claim;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.block.Block;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

public record ClaimAllowListKey<T>(Identifier id, String translationKey,
                                   Supplier<ItemStack> guiIcon,
                                   ResourceKey<Registry<T>> registry,
                                   Function<Claim, AllowedRegistryList<T>> factory) {

    private static final Map<Identifier, ClaimAllowListKey<?>> MAP = new LinkedHashMap<>();

    public static final ClaimAllowListKey<Item> ITEM_USE = new ClaimAllowListKey<>("item", "flan.screenMenuItemUse",
            () -> PotionContents.createItemStack(Items.POTION, Potions.WATER),
            Registries.ITEM, claim -> AllowedRegistryList.ofItemLike(BuiltInRegistries.ITEM, claim));
    public static final ClaimAllowListKey<Block> BLOCK_BREAK = new ClaimAllowListKey<>("block_break", "flan.screenMenuBlockBreak",
            () -> new ItemStack(Items.DIAMOND_PICKAXE),
            Registries.BLOCK, claim -> AllowedRegistryList.ofItemLike(BuiltInRegistries.BLOCK, claim));
    public static final ClaimAllowListKey<Block> BLOCK_USE = new ClaimAllowListKey<>("block_use", "flan.screenMenuBlockUse",
            () -> new ItemStack(Items.BANNER.red()),
            Registries.BLOCK, claim -> AllowedRegistryList.ofItemLike(BuiltInRegistries.BLOCK, claim));
    public static final ClaimAllowListKey<EntityType<?>> ENTITY_ATTACK = new ClaimAllowListKey<>("entity_attack", "flan.screenMenuEntityAttack",
            () -> new ItemStack(Items.DIAMOND_SWORD),
            Registries.ENTITY_TYPE, claim -> new AllowedRegistryList<>(BuiltInRegistries.ENTITY_TYPE, claim, AllowedRegistryList.ENTITY_AS_ITEM));
    public static final ClaimAllowListKey<EntityType<?>> ENTITY_USE = new ClaimAllowListKey<>("entity_use", "flan.screenMenuEntityUse",
            () -> new ItemStack(Items.SHEARS),
            Registries.ENTITY_TYPE, claim -> new AllowedRegistryList<>(BuiltInRegistries.ENTITY_TYPE, claim, AllowedRegistryList.ENTITY_AS_ITEM));
    public static final ClaimAllowListKey<Item> ITEM_PICKUP = new ClaimAllowListKey<>("item_pickup", "flan.screenMenuItemPickup",
            () -> new ItemStack(Items.DROPPER),
            Registries.ITEM, claim -> AllowedRegistryList.ofItemLike(BuiltInRegistries.ITEM, claim));
    public static final ClaimAllowListKey<Item> ITEM_DROP = new ClaimAllowListKey<>("item_drop", "flan.screenMenuItemDrop",
            () -> new ItemStack(Items.WHEAT_SEEDS),
            Registries.ITEM, claim -> AllowedRegistryList.ofItemLike(BuiltInRegistries.ITEM, claim));

    public ClaimAllowListKey(String path, String translationKey, Supplier<ItemStack> guiIcon, ResourceKey<Registry<T>> registry, Function<Claim, AllowedRegistryList<T>> factory) {
        this(Identifier.fromNamespaceAndPath(Flan.MODID, path), translationKey, guiIcon, registry, factory);
        if (MAP.put(this.id(), this) != null)
            throw new IllegalStateException("Key with id " + this.id() + " already exists!");
    }

    public static Map<Identifier, ClaimAllowListKey<?>> keys() {
        return ImmutableMap.copyOf(MAP);
    }

    public static ClaimAllowListKey<?> get(Identifier id) {
        return MAP.get(id);
    }
}
