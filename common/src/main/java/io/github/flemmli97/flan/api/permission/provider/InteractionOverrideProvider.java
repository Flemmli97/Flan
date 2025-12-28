package io.github.flemmli97.flan.api.permission.provider;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import io.github.flemmli97.flan.api.permission.InteractionOverrideManager;
import io.github.flemmli97.flan.api.permission.interactions.InteractionType;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * The provider for datagen runs
 */
public abstract class InteractionOverrideProvider implements DataProvider {

    private final Map<Identifier, Builder<?>> data = new HashMap<>();

    private final PackOutput output;

    public InteractionOverrideProvider(PackOutput output) {
        this.output = output;
    }

    protected abstract void add();

    @Override
    public CompletableFuture<?> run(CachedOutput cache) {
        this.add();
        return CompletableFuture.allOf(this.data.entrySet().stream().map(entry -> {
            Path path = this.output.getOutputFolder(PackOutput.Target.DATA_PACK).resolve(entry.getKey().getNamespace())
                    .resolve(Registries.elementsDirPath(InteractionOverrideManager.ID)).resolve(entry.getKey().getPath() + ".json");
            JsonObject obj = new JsonObject();
            obj.addProperty("type", entry.getValue().type.getId().toString());
            obj.add("values", entry.getValue().values());
            return DataProvider.saveStable(cache, obj, path);
        }).toArray(CompletableFuture<?>[]::new));
    }

    @Override
    public String getName() {
        return "Interaction Overrides";
    }

    public <T> void override(Identifier id, Builder<T> permission) {
        if (this.data.put(id, permission) != null)
            throw new IllegalStateException("Override already exists" + id);
    }

    public static class Builder<T> {

        private static final Codec<List<Pair<Either<TagKey<Block>, Identifier>, Identifier>>> BLOCK_CODEC = idBasedCodec(Registries.BLOCK);
        private static final Codec<List<Pair<Either<TagKey<Item>, Identifier>, Identifier>>> ITEM_CODEC = idBasedCodec(Registries.ITEM);
        private static final Codec<List<Pair<Either<TagKey<EntityType<?>>, Identifier>, Identifier>>> ENTITY_CODEC = idBasedCodec(Registries.ENTITY_TYPE);

        public final InteractionType<T> type;

        private final Registry<T> registry;

        private final Codec<List<Pair<Either<TagKey<T>, Identifier>, Identifier>>> codec;

        private final List<Pair<Either<TagKey<T>, Identifier>, Identifier>> entries = new ArrayList<>();

        private Builder(InteractionType<T> type, Registry<T> registry, Codec<List<Pair<Either<TagKey<T>, Identifier>, Identifier>>> codec) {
            this.type = type;
            this.registry = registry;
            this.codec = codec;
        }

        public static <T> Codec<List<Pair<Either<TagKey<T>, Identifier>, Identifier>>> idBasedCodec(ResourceKey<? extends Registry<T>> registry) {
            Codec<Either<TagKey<T>, Identifier>> tagOrEntry = Codec.either(TagKey.hashedCodec(registry), Identifier.CODEC);
            return InteractionType.valueCodec(tagOrEntry);
        }

        public static Builder<Block> blockInteractions(InteractionType<Block> type) {
            return new Builder<>(type, BuiltInRegistries.BLOCK, BLOCK_CODEC);
        }

        public static Builder<Item> itemInteractions(InteractionType<Item> type) {
            return new Builder<>(type, BuiltInRegistries.ITEM, ITEM_CODEC);
        }

        public static Builder<EntityType<?>> entityInteractions(InteractionType<EntityType<?>> type) {
            return new Builder<>(type, BuiltInRegistries.ENTITY_TYPE, ENTITY_CODEC);
        }

        public Builder<T> addEntry(T value, Identifier permission) {
            this.entries.add(Pair.of(Either.right(this.registry.getKey(value)), permission));
            return this;
        }

        public Builder<T> addEntry(Identifier value, Identifier permission) {
            this.entries.add(Pair.of(Either.right(value), permission));
            return this;
        }

        public Builder<T> addEntry(TagKey<T> value, Identifier permission) {
            this.entries.add(Pair.of(Either.left(value), permission));
            return this;
        }

        protected JsonElement values() {
            return this.codec.encodeStart(JsonOps.INSTANCE, this.entries)
                    .getOrThrow();
        }
    }
}