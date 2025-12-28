package io.github.flemmli97.flan.api.permission;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import io.github.flemmli97.flan.Flan;
import io.github.flemmli97.flan.api.permission.interactions.InteractionType;
import io.github.flemmli97.flan.api.permission.interactions.ResolvableEntry;
import io.github.flemmli97.flan.api.permission.interactions.ResolvableHolderSet;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Prob overengineered but anyway...
 * <p></p>
 * Handles permission overrides for various contexts.
 * You can define a context by simply static init an {@link InteractionType}
 * Check an override by using {@link InteractionOverrideManager#getOverride}
 * <p></p>
 * Default applies to block left/right click, item right click, entity left/right click
 */
public class InteractionOverrideManager extends SimpleJsonResourceReloadListener<InteractionOverrideManager.InteractionEntry<?>> {

    public static final ResourceKey<? extends Registry<InteractionOverrideManager.InteractionEntry<?>>> ID =
            ResourceKey.createRegistryKey(Identifier.fromNamespaceAndPath(Flan.MODID, "claim_interactions_override"));

    public static final Codec<ResolvableEntry<Block>> BLOCK_CODEC = ResolvableHolderSet.codec(BuiltInRegistries.BLOCK);
    public static final Codec<ResolvableEntry<Item>> ITEM_CODEC = ResolvableHolderSet.codec(BuiltInRegistries.ITEM);
    public static final Codec<ResolvableEntry<EntityType<?>>> ENTITY_CODEC = ResolvableHolderSet.codec(BuiltInRegistries.ENTITY_TYPE);

    public static final InteractionType<Block> BLOCK_LEFT_CLICK = new InteractionType<>(Identifier.fromNamespaceAndPath(Flan.MODID, "block_left_click"), BLOCK_CODEC, InteractionHolder::new);
    public static final InteractionType<Block> BLOCK_INTERACT = new InteractionType<>(Identifier.fromNamespaceAndPath(Flan.MODID, "block_interact"), BLOCK_CODEC, InteractionHolder::new);
    public static final InteractionType<Item> ITEM_USE = new InteractionType<>(Identifier.fromNamespaceAndPath(Flan.MODID, "item_use"), ITEM_CODEC, InteractionHolder::new);
    public static final InteractionType<EntityType<?>> ENTITY_ATTACK = new InteractionType<>(Identifier.fromNamespaceAndPath(Flan.MODID, "entity_attack"), ENTITY_CODEC, InteractionHolder::new);
    public static final InteractionType<EntityType<?>> ENTITY_INTERACT = new InteractionType<>(Identifier.fromNamespaceAndPath(Flan.MODID, "entity_interact"), ENTITY_CODEC, InteractionHolder::new);
    public static final InteractionType<Block> PROJECTILE_BLOCK_INTERACT = new InteractionType<>(Identifier.fromNamespaceAndPath(Flan.MODID, "projectile_block_interact"), BLOCK_CODEC, InteractionHolder::new);
    public static final InteractionType<EntityType<?>> PROJECTILE_ENTITY_INTERACT = new InteractionType<>(Identifier.fromNamespaceAndPath(Flan.MODID, "projectile_entity_interact"), ENTITY_CODEC, InteractionHolder::new);

    private static InteractionOverrideManager INSTANCE;

    private final Map<InteractionType<?>, InteractionHolder<?>> overrides = new HashMap<>();

    private InteractionOverrideManager(HolderLookup.Provider provider) {
        super(provider, InteractionEntry.CODEC, ID);
    }

    public static InteractionOverrideManager create(HolderLookup.Provider provider) {
        InteractionOverrideManager.INSTANCE = new InteractionOverrideManager(provider);
        return getInstance();
    }

    public static InteractionOverrideManager getInstance() {
        return INSTANCE;
    }

    public Identifier getBlockLeftClick(Block block) {
        return this.getOverride(BLOCK_LEFT_CLICK, block);
    }

    public Identifier getBlockInteract(Block block) {
        return this.getOverride(BLOCK_INTERACT, block);
    }

    public Identifier getItemUse(Item item) {
        return this.getOverride(ITEM_USE, item);
    }

    public Identifier getEntityAttack(EntityType<?> entity) {
        return this.getOverride(ENTITY_ATTACK, entity);
    }

    public Identifier getEntityInteract(EntityType<?> entity) {
        return this.getOverride(ENTITY_INTERACT, entity);
    }

    public Identifier getProjectileBlockInteract(Block block) {
        return this.getOverride(PROJECTILE_BLOCK_INTERACT, block);
    }

    public Identifier getProjectileEntityInteract(Projectile entity) {
        InteractionHolder<EntityType<?>> holder = this.getHolder(PROJECTILE_ENTITY_INTERACT);
        if (!holder.unresolved()) {
            // Needs to be done here because it needs level access for instantiation
            for (EntityType<?> type : BuiltInRegistries.ENTITY_TYPE) {
                InteractionHolder<EntityType<?>> map = this.getHolder(PROJECTILE_ENTITY_INTERACT);
                ObjectToPermissionMap.PROJECTILE_PERMISSION_BUILDER.entrySet().stream().filter(e -> {
                    Entity dummy = type.create(entity.level(), EntitySpawnReason.TRIGGERED);
                    return e.getKey().test(dummy);
                }).map(Map.Entry::getValue).findFirst().ifPresent(sub -> map.defaults.put(type, sub.get()));
            }
        }
        return this.getOverride(PROJECTILE_ENTITY_INTERACT, entity.getType());
    }

    /**
     * Returns the overriden permission for the given type and value
     */
    public <T> Identifier getOverride(InteractionType<T> type, T entry) {
        return this.getHolder(type).get(entry);
    }

    @SuppressWarnings("unchecked")
    private <T> InteractionHolder<T> getHolder(InteractionType<T> type) {
        return (InteractionHolder<T>) this.overrides.computeIfAbsent(type, k -> type.getGen().get());
    }

    @Override
    protected void apply(Map<Identifier, InteractionEntry<?>> data, ResourceManager manager, ProfilerFiller profiler) {
        this.overrides.clear();
        for (Block block : BuiltInRegistries.BLOCK) {
            InteractionHolder<Block> map = this.getHolder(BLOCK_INTERACT);
            ObjectToPermissionMap.BLOCK_PERMISSION_BUILDER.entrySet().stream().filter(e -> e.getKey().test(block))
                    .map(Map.Entry::getValue).findFirst().ifPresent(sub -> map.defaults.put(block, sub.get()));
        }
        for (Item item : BuiltInRegistries.ITEM) {
            InteractionHolder<Item> map = this.getHolder(ITEM_USE);
            ObjectToPermissionMap.ITEM_PERMISSION_BUILDER.entrySet().stream().filter(e -> e.getKey().test(item))
                    .map(Map.Entry::getValue).findFirst().ifPresent(sub -> map.defaults.put(item, sub.get()));
        }
        for (Block block : BuiltInRegistries.BLOCK) {
            InteractionHolder<Block> map = this.getHolder(PROJECTILE_BLOCK_INTERACT);
            ObjectToPermissionMap.PROJECTILE_BLOCK_PERMISSION_BUILDER.entrySet().stream().filter(e -> e.getKey().test(block))
                    .map(Map.Entry::getValue).findFirst().ifPresent(sub -> map.defaults.put(block, sub.get()));
        }
        data.forEach((res, entry) -> this.appendTo(entry));
    }

    private <T> void appendTo(InteractionEntry<T> entry) {
        InteractionHolder<T> map = this.getHolder(entry.type());
        entry.elements().forEach(pair -> map.unresolvedTags.put(pair.getFirst(), pair.getSecond()));
    }

    public static class InteractionHolder<T> {

        private final Map<T, Identifier> direct = new HashMap<>();
        private final Map<ResolvableEntry<T>, Identifier> unresolvedTags = new HashMap<>();
        private final Map<T, Identifier> defaults = new HashMap<>();

        public Identifier get(T val) {
            if (this.unresolved()) {
                this.resolve();
            }
            return this.direct.get(val);
        }

        public boolean unresolved() {
            return !this.unresolvedTags.isEmpty() || !this.defaults.isEmpty();
        }

        private void resolve() {
            this.unresolvedTags.entrySet().stream().sorted(Map.Entry.comparingByKey())
                    .forEach(entry -> entry.getKey().resolve().forEach(item -> {
                        if (!this.direct.containsKey(item))
                            this.direct.put(item, entry.getValue());
                    }));
            this.unresolvedTags.clear();
            this.defaults.forEach((key, value) -> {
                if (!this.direct.containsKey(key))
                    this.direct.put(key, value);
            });
            this.defaults.clear();
        }
    }

    public record InteractionEntry<T>(InteractionType<T> type,
                                      List<Pair<ResolvableEntry<T>, Identifier>> elements) {

        public static final Codec<InteractionEntry<?>> CODEC = Identifier.CODEC.dispatch(e -> e.type().getId(),
                t -> InteractionType.get(t).getCodec());
    }

}
