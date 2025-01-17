package io.github.flemmli97.flan.api.permission.provider;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import io.github.flemmli97.flan.Flan;
import io.github.flemmli97.flan.api.permission.InteractionOverrideManager;
import net.minecraft.core.Registry;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DataProvider;
import net.minecraft.data.HashCache;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The provider for datagen runs
 */
public abstract class InteractionOverrideProvider implements DataProvider {

    private static final Logger LOGGER = LogManager.getLogger();

    private static final Gson GSON = new GsonBuilder().enableComplexMapKeySerialization().setPrettyPrinting().disableHtmlEscaping().create();

    private final Map<ResourceLocation, Builder<?>> data = new HashMap<>();

    private final DataGenerator gen;

    public InteractionOverrideProvider(DataGenerator gen) {
        this.gen = gen;
    }

    protected abstract void add();

    @Override
    public void run(HashCache cache) {
        this.add();
        this.data.forEach((res, data) -> {
            Path path = this.gen.getOutputFolder().resolve("data/" + res.getNamespace() + "/" + InteractionOverrideManager.DIRECTORY + "/" + res.getPath() + ".json");
            try {
                JsonObject obj = new JsonObject();
                obj.addProperty("type", data.type.id().toString());
                obj.add("values", data.values());
                DataProvider.save(GSON, cache, obj, path);
            } catch (IOException e) {
                LOGGER.error("Couldn't save itemstat {}", path, e);
            }
        });
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

        private static final Codec<List<Pair<Either<TagKey<Block>, ResourceLocation>, ResourceLocation>>> BLOCK_CODEC = InteractionOverrideManager.tagOrEntryCodec(Registry.BLOCK_REGISTRY,
                ResourceLocation.CODEC).listOf();
        private static final Codec<List<Pair<Either<TagKey<Item>, ResourceLocation>, ResourceLocation>>> ITEM_CODEC = InteractionOverrideManager.tagOrEntryCodec(Registry.ITEM_REGISTRY,
                ResourceLocation.CODEC).listOf();
        private static final Codec<List<Pair<Either<TagKey<EntityType<?>>, ResourceLocation>, ResourceLocation>>> ENTITY_CODEC = InteractionOverrideManager.tagOrEntryCodec(Registry.ENTITY_TYPE_REGISTRY,
                ResourceLocation.CODEC).listOf();

        public final InteractionOverrideManager.InteractionType<T> type;

        private final Registry<T> registry;

        private final Codec<List<Pair<Either<TagKey<T>, ResourceLocation>, ResourceLocation>>> codec;

        private final List<Pair<Either<TagKey<T>, ResourceLocation>, ResourceLocation>> entries = new ArrayList<>();

        private Builder(InteractionOverrideManager.InteractionType<T> type, Registry<T> registry, Codec<List<Pair<Either<TagKey<T>, ResourceLocation>, ResourceLocation>>> codec) {
            this.type = type;
            this.registry = registry;
            this.codec = codec;
        }

        public static Builder<Block> blockInteractions(InteractionOverrideManager.InteractionType<Block> type) {
            if (type != InteractionOverrideManager.BLOCK_LEFT_CLICK
                    && type != InteractionOverrideManager.BLOCK_INTERACT) {
                throw new IllegalStateException("Unsupported Type");
            }
            return new Builder<>(type, Registry.BLOCK, BLOCK_CODEC);
        }

        public static Builder<Item> itemInteractions(InteractionOverrideManager.InteractionType<Item> type) {
            if (type != InteractionOverrideManager.ITEM_USE) {
                throw new IllegalStateException("Unsupported Type");
            }
            return new Builder<>(type, Registry.ITEM, ITEM_CODEC);
        }

        public static Builder<EntityType<?>> entityInteractions(InteractionOverrideManager.InteractionType<EntityType<?>> type) {
            if (type != InteractionOverrideManager.ENTITY_ATTACK
                    && type != InteractionOverrideManager.ENTITY_INTERACT) {
                throw new IllegalStateException("Unsupported Type");
            }
            return new Builder<>(type, Registry.ENTITY_TYPE, ENTITY_CODEC);
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
                    .getOrThrow(false, Flan::error);
        }
    }
}