package io.github.flemmli97.flan.api.permission;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.flemmli97.flan.Flan;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.tags.TagKey;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

/**
 * Prob overengineered but anyway...
 * <p></p>
 * Handles permission overrides for various contexts.
 * You can define a context by simply static init an {@link InteractionType}
 * Check an override by using {@link InteractionOverrideManager#getOverride}
 * <p></p>
 * Default applies to block left/right click, item right click, entity left/right click
 */
public class InteractionOverrideManager extends SimpleJsonResourceReloadListener {

    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(Flan.MODID, "claim_interactions_override");
    public static final String DIRECTORY = String.format("%s/%s", ID.getNamespace(), ID.getPath());

    private static final Gson GSON = new GsonBuilder().create();

    public static final Codec<List<Pair<Either<TagKey<Block>, Block>, ResourceLocation>>> BLOCK_CODEC = tagOrEntryCodec(BuiltInRegistries.BLOCK).listOf();
    public static final Codec<List<Pair<Either<TagKey<Item>, Item>, ResourceLocation>>> ITEM_CODEC = tagOrEntryCodec(BuiltInRegistries.ITEM).listOf();
    public static final Codec<List<Pair<Either<TagKey<EntityType<?>>, EntityType<?>>, ResourceLocation>>> ENTITY_CODEC = tagOrEntryCodec(BuiltInRegistries.ENTITY_TYPE).listOf();

    public static final InteractionType<Block> BLOCK_LEFT_CLICK = new InteractionType<>(ResourceLocation.fromNamespaceAndPath(Flan.MODID, "block_left_click"), () -> new InteractionHolder<>(BuiltInRegistries.BLOCK, BLOCK_CODEC));
    public static final InteractionType<Block> BLOCK_INTERACT = new InteractionType<>(ResourceLocation.fromNamespaceAndPath(Flan.MODID, "block_interact"), () -> new InteractionHolder<>(BuiltInRegistries.BLOCK, BLOCK_CODEC));
    public static final InteractionType<Item> ITEM_USE = new InteractionType<>(ResourceLocation.fromNamespaceAndPath(Flan.MODID, "item_use"), () -> new InteractionHolder<>(BuiltInRegistries.ITEM, ITEM_CODEC));
    public static final InteractionType<EntityType<?>> ENTITY_ATTACK = new InteractionType<>(ResourceLocation.fromNamespaceAndPath(Flan.MODID, "entity_attack"), () -> new InteractionHolder<>(BuiltInRegistries.ENTITY_TYPE, ENTITY_CODEC));
    public static final InteractionType<EntityType<?>> ENTITY_INTERACT = new InteractionType<>(ResourceLocation.fromNamespaceAndPath(Flan.MODID, "entity_interact"), () -> new InteractionHolder<>(BuiltInRegistries.ENTITY_TYPE, ENTITY_CODEC));

    private static InteractionOverrideManager INSTANCE;

    private final Map<InteractionType<?>, InteractionHolder<?>> overrides = new HashMap<>();

    private final HolderLookup.Provider provider;

    private InteractionOverrideManager(HolderLookup.Provider provider) {
        super(GSON, DIRECTORY);
        this.provider = provider;
    }

    public static InteractionOverrideManager create(HolderLookup.Provider provider) {
        INSTANCE = new InteractionOverrideManager(provider);
        return getInstance();
    }

    public static InteractionOverrideManager getInstance() {
        return INSTANCE;
    }

    public static <T> Codec<Pair<Either<TagKey<T>, T>, ResourceLocation>> tagOrEntryCodec(Registry<T> registry) {
        return tagOrEntryCodec(registry.key(), registry.byNameCodec());
    }

    public static <T, O> Codec<Pair<Either<TagKey<T>, O>, ResourceLocation>> tagOrEntryCodec(
            ResourceKey<? extends Registry<T>> key, Codec<O> codec) {
        Codec<Either<TagKey<T>, O>> tagOrEntry = Codec.either(Codec.STRING.flatXmap(
                r -> {
                    if (r.startsWith("#"))
                        return DataResult.success(TagKey.create(key, ResourceLocation.parse(r.substring(1))));
                    return DataResult.error(() -> "Not a tag value" + r);
                },
                l -> DataResult.success("#" + l.location())
        ), codec);
        return RecordCodecBuilder.create(builder -> builder.group(
                        tagOrEntry.fieldOf("entry").forGetter(Pair::getFirst),
                        ResourceLocation.CODEC.fieldOf("permission").forGetter(Pair::getSecond))
                .apply(builder, Pair::of));
    }

    public static <T> List<T> expandTag(Registry<T> registry, TagKey<T> tag) {
        List<T> elements = new ArrayList<>();
        registry.getTag(tag)
                .ifPresent(n -> n.forEach(h -> elements.add(h.value())));
        return elements;
    }

    public ResourceLocation getBlockLeftClick(Block block) {
        return this.getOverride(BLOCK_LEFT_CLICK, block);
    }

    public ResourceLocation getBlockInteract(Block block) {
        return this.getOverride(BLOCK_INTERACT, block);
    }

    public ResourceLocation getItemUse(Item item) {
        return this.getOverride(ITEM_USE, item);
    }

    public ResourceLocation getEntityAttack(EntityType<?> entity) {
        return this.getOverride(ENTITY_ATTACK, entity);
    }

    public ResourceLocation getEntityInteract(EntityType<?> entity) {
        return this.getOverride(ENTITY_INTERACT, entity);
    }

    /**
     * Returns the overriden permission for the given type and value
     */
    public <T> ResourceLocation getOverride(InteractionType<T> type, T entry) {
        return this.getHolder(type).get(entry);
    }

    @SuppressWarnings("unchecked")
    private <T> InteractionHolder<T> getHolder(InteractionType<T> type) {
        return (InteractionHolder<T>) this.overrides.computeIfAbsent(type, k -> type.gen.get());
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> data, ResourceManager manager, ProfilerFiller profiler) {
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
        DynamicOps<JsonElement> ops = this.provider.createSerializationContext(JsonOps.INSTANCE);
        data.forEach((res, el) -> {
            try {
                JsonObject obj = el.getAsJsonObject();
                InteractionType<?> type = InteractionType.LOOKUP.get(ResourceLocation.parse(obj.get("type").getAsString()));
                if (type == null)
                    throw new JsonParseException("InteractionType of value " + obj.get("type").getAsString() + " does not exist");
                JsonElement values = obj.get("values");
                this.appendTo(this.getHolder(type), values, ops);
            } catch (Exception ex) {
                Flan.LOGGER.error("Couldnt parse claim permission json {} {}", res, ex);
                ex.fillInStackTrace();
            }
        });
    }

    private <T> void appendTo(InteractionHolder<T> map, JsonElement element, DynamicOps<JsonElement> ops) {
        List<Pair<Either<TagKey<T>, T>, ResourceLocation>> elements = map.codec.parse(ops, element)
                .getOrThrow();
        elements.forEach(pair -> pair.getFirst().ifLeft(tag -> map.unresolvedTags.put(tag, pair.getSecond()))
                .ifRight(val -> map.direct.put(val, pair.getSecond())));
    }

    public static class InteractionHolder<T> {

        private final Registry<T> registry;
        private final Codec<List<Pair<Either<TagKey<T>, T>, ResourceLocation>>> codec;

        private final Map<T, ResourceLocation> direct = new HashMap<>();
        private final Map<TagKey<T>, ResourceLocation> unresolvedTags = new HashMap<>();
        private final Map<T, ResourceLocation> defaults = new HashMap<>();

        public InteractionHolder(Registry<T> registry, Codec<List<Pair<Either<TagKey<T>, T>, ResourceLocation>>> codec) {
            this.registry = registry;
            this.codec = codec;
        }

        public ResourceLocation get(T val) {
            if (!this.unresolvedTags.isEmpty() || !this.defaults.isEmpty()) {
                this.resolve();
            }
            return this.direct.get(val);
        }

        private void resolve() {
            this.unresolvedTags.entrySet().stream().sorted(Comparator.comparing(e -> e.getKey().location()))
                    .forEach(entry -> expandTag(this.registry, entry.getKey()).forEach(item -> {
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

    public record InteractionType<T>(ResourceLocation id, Supplier<InteractionHolder<T>> gen) {

        static final Map<ResourceLocation, InteractionType<?>> LOOKUP = new HashMap<>();

        public InteractionType(ResourceLocation id, Supplier<InteractionHolder<T>> gen) {
            this.id = id;
            this.gen = gen;
            if (LOOKUP.put(id, this) != null)
                throw new IllegalStateException("Type already registered");
        }
    }
}
