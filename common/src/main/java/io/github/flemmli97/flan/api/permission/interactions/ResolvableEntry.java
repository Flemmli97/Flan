package io.github.flemmli97.flan.api.permission.interactions;

public interface ResolvableEntry<T> extends Comparable<ResolvableEntry<T>> {

    Iterable<T> resolve();
}
