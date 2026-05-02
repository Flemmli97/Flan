package io.github.flemmli97.flan.api.economy;

import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CurrencyRegistry {

    private static final List<OrderedResource> ORDERED_IDS = new ArrayList<>();
    private static final Map<Identifier, CurrencyHandler> HANDLERS = new HashMap<>();

    public static synchronized void add(Identifier id, CurrencyHandler impl) {
        add(id, ORDERED_IDS.size(), impl);
    }

    /**
     * Register a new currency handler here
     *
     * @param order Optional ordering. The mod will return the first implementation it finds.
     *              Lower number has higher priority
     */
    public static synchronized void add(Identifier id, int order, CurrencyHandler impl) {
        ORDERED_IDS.add(new OrderedResource(order, id));
        ORDERED_IDS.sort(Collections.reverseOrder());
        HANDLERS.put(id, impl);
    }

    public static CurrencyHandler findFirst() {
        return ORDERED_IDS.stream().findFirst().map(r -> HANDLERS.get(r.res)).orElse(null);
    }

    private record OrderedResource(int order, Identifier res) implements Comparable<OrderedResource> {

        @Override
        public int compareTo(@NotNull OrderedResource o) {
            return this.order == o.order ? this.res.compareTo(o.res) : Integer.compare(this.order, o.order);
        }
    }
}
