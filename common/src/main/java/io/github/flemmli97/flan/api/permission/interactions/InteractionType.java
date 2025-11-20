package io.github.flemmli97.flan.api.permission.interactions;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.flemmli97.flan.api.permission.InteractionOverrideManager;
import net.minecraft.resources.ResourceLocation;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

public class InteractionType<T> {

    static final Map<ResourceLocation, InteractionType<?>> LOOKUP = new HashMap<>();

    private final ResourceLocation id;
    private final MapCodec<InteractionOverrideManager.InteractionEntry<T>> codec;
    private final Supplier<InteractionOverrideManager.InteractionHolder<T>> gen;

    public InteractionType(ResourceLocation id, Codec<ResolvableEntry<T>> codec, Supplier<InteractionOverrideManager.InteractionHolder<T>> gen) {
        this.id = id;
        this.codec = valueCodec(codec).fieldOf("values").xmap(l -> new InteractionOverrideManager.InteractionEntry<>(this, l), InteractionOverrideManager.InteractionEntry::elements);
        this.gen = gen;
        if (LOOKUP.put(id, this) != null)
            throw new IllegalStateException("Type already registered");
    }

    public static InteractionType<?> get(ResourceLocation id) {
        return LOOKUP.get(id);
    }

    public static <T> Codec<List<Pair<T, ResourceLocation>>> valueCodec(Codec<T> codec) {
        Codec<Pair<T, ResourceLocation>> valueCodec = RecordCodecBuilder.create(builder -> builder.group(
                        codec.fieldOf("entry").forGetter(Pair::getFirst),
                        ResourceLocation.CODEC.fieldOf("permission").forGetter(Pair::getSecond))
                .apply(builder, Pair::of));
        return valueCodec.listOf();
    }

    public ResourceLocation getId() {
        return this.id;
    }

    public MapCodec<InteractionOverrideManager.InteractionEntry<T>> getCodec() {
        return this.codec;
    }

    public Supplier<InteractionOverrideManager.InteractionHolder<T>> getGen() {
        return this.gen;
    }
}
