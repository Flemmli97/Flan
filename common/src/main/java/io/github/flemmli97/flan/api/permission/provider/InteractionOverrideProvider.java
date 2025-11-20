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
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
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

    private final Map<ResourceLocation, Builder<?>> data = new HashMap<>();

    private final PackOutput output;

    public InteractionOverrideProvider(PackOutput output) {
        this.output = output;
    }

    protected abstract void add();

    @Override
    public CompletableFuture<?> run(CachedOutput cache) {
        this.add();
        return CompletableFuture.allOf(this.data.entrySet().stream().map(entry -> {
            Path path = this.output.getOutputFolder(PackOutput.Target.DATA_PACK).resolve(entry.getKey().getNamespace() + "/" + InteractionOverrideManager.DIRECTORY + "/" + entry.getKey().getPath() + ".json");
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

    public <T> void override(ResourceLocation res, Builder<T> permission) {
        if (this.data.put(res, permission) != null)
            throw new IllegalStateException("Override already exists" + res);
    }

    public static class Builder<T> {

        private static final Codec<List<Pair<Either<TagKey<Block>, ResourceLocation>, ResourceLocation>>> BLOCK_CODEC = idBasedCodec(Registries.BLOCK);
        private static final Codec<List<Pair<Either<TagKey<Item>, ResourceLocation>, ResourceLocation>>> ITEM_CODEC = idBasedCodec(Registries.ITEM);
        private static final Codec<List<Pair<Either<TagKey<EntityType<?>>, ResourceLocation>, ResourceLocation>>> ENTITY_CODEC = idBasedCodec(Registries.ENTITY_TYPE);

        public final InteractionType<T> type;

        private final Registry<T> registry;

        private final Codec<List<Pair<Either<TagKey<T>, ResourceLocation>, ResourceLocation>>> codec;

        private final List<Pair<Either<TagKey<T>, ResourceLocation>, ResourceLocation>> entries = new ArrayList<>();

        private Builder(InteractionType<T> type, Registry<T> registry, Codec<List<Pair<Either<TagKey<T>, ResourceLocation>, ResourceLocation>>> codec) {
            this.type = type;
            this.registry = registry;
            this.codec = codec;
        }

        public static <T> Codec<List<Pair<Either<TagKey<T>, ResourceLocation>, ResourceLocation>>> idBasedCodec(ResourceKey<? extends Registry<T>> registry) {
            Codec<Either<TagKey<T>, ResourceLocation>> tagOrEntry = Codec.either(TagKey.hashedCodec(registry), ResourceLocation.CODEC);
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

        public Builder<T> addEntry(T value, ResourceLocation permission) {
            this.entries.add(Pair.of(Either.right(this.registry.getKey(value)), permission));
            return this;
        }

        public Builder<T> addEntry(ResourceLocation value, ResourceLocation permission) {
            this.entries.add(Pair.of(Either.right(value), permission));
            return this;
        }

        public Builder<T> addEntry(TagKey<T> value, ResourceLocation permission) {
            this.entries.add(Pair.of(Either.left(value), permission));
            return this;
        }

        protected JsonElement values() {
            return this.codec.encodeStart(JsonOps.INSTANCE, this.entries)
                    .getOrThrow();
        }
    }
}