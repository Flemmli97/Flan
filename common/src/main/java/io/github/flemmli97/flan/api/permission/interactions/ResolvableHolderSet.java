package io.github.flemmli97.flan.api.permission.interactions;

import com.mojang.serialization.Codec;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryCodecs;
import org.jetbrains.annotations.NotNull;

import java.util.Iterator;

public record ResolvableHolderSet<T>(HolderSet<T> holders) implements ResolvableEntry<T> {

    public static <T> Codec<ResolvableEntry<T>> codec(Registry<T> registry) {
        return RegistryCodecs.homogeneousList(registry.key())
                .xmap(ResolvableHolderSet::new, h -> ((ResolvableHolderSet<T>) h).holders());
    }

    @Override
    public Iterable<T> resolve() {
        return new Iterable<>() {
            @Override
            public @NotNull Iterator<T> iterator() {
                Iterator<Holder<T>> it = ResolvableHolderSet.this.holders.iterator();
                return new Iterator<>() {
                    @Override
                    public boolean hasNext() {
                        return it.hasNext();
                    }

                    @Override
                    public T next() {
                        return it.next().value();
                    }
                };
            }
        };
    }

    @Override
    public int compareTo(@NotNull ResolvableEntry<T> o) {
        if (o instanceof ResolvableHolderSet<?> other) {
            String key = this.holders().unwrapKey().map(t -> t.location().toString()).orElse("");
            String otherKey = other.holders().unwrapKey().map(t -> t.location().toString()).orElse("");
            return key.compareTo(otherKey);
        }
        return 0;
    }
}
