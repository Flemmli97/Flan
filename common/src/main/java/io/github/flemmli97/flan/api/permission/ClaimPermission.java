package io.github.flemmli97.flan.api.permission;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.flemmli97.flan.platform.CrossPlatformStuff;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/**
 * Instance for a permission for claims. The builder is nearly identical to the instance.
 */
public class ClaimPermission implements Comparable<ClaimPermission> {

    public static final Comparator<Identifier> NAMESPACE_FIRST = Comparator.comparing(Identifier::getNamespace)
            .thenComparing(Identifier::getPath);
    /**
     * Item to show in the gui
     */
    private final ItemStack guiItem;
    private final Identifier id;
    public final boolean defaultVal;
    /**
     * Whether this permission is a global permission or not.
     * Global permission cannot be configured for player groups
     */
    public final boolean global;
    /**
     * The default value for the global claim if the config does not override it
     */
    public final boolean globalVal;
    /**
     * Whether this permission needs to have its value set in the claim to function
     * If this is true admin and owner will be treated as normal players (aka the claims permission needs to be true to apply to them)
     */
    public final boolean requireExplicitSet;
    /**
     * The order in which this permission appears in the gui. If a permission has the same ordering the id will determine the order
     */
    public final int order;

    private ClaimPermission(Identifier id, ItemStack guiItem, boolean defaultVal, boolean global, boolean globalVal, boolean requireExplicitSet, int order) {
        this.id = id;
        this.guiItem = guiItem;
        this.globalVal = globalVal;
        this.requireExplicitSet = requireExplicitSet;
        this.order = order;
        this.defaultVal = defaultVal;
        this.global = global;
    }

    public ItemStack getItem() {
        return this.guiItem.copy();
    }

    public Identifier getId() {
        return this.id;
    }

    public String translationKey() {
        return String.format("flan.permission.%s", this.id);
    }

    public String translationKeyDescription() {
        return String.format("flan.permission.%s.desc", this.id);
    }

    @Override
    public int hashCode() {
        return this.id.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj instanceof ClaimPermission) {
            return this.id.equals(((ClaimPermission) obj).id);
        }
        return false;
    }

    @Override
    public String toString() {
        return this.id.toString();
    }

    @Override
    public int compareTo(@NotNull ClaimPermission other) {
        if (this.order == other.order)
            return NAMESPACE_FIRST.compare(this.id, other.id);
        return Integer.compare(this.order, other.order);
    }

    public static class Builder {

        public static final Codec<ClaimPermission.Builder> CODEC = RecordCodecBuilder.create((instance) ->
                instance.group(ItemStackHolder.CODEC.fieldOf("gui_item").forGetter(d -> d.guiItem),
                        Codec.BOOL.fieldOf("default_value").forGetter(d -> d.defaultVal),
                        Codec.BOOL.fieldOf("global").forGetter(d -> d.global),
                        Codec.BOOL.optionalFieldOf("global_default_value").forGetter(d -> d.globalVal ? Optional.empty() : Optional.of(false)),
                        Codec.BOOL.optionalFieldOf("require_explicit").forGetter(d -> !d.requireExplicitSet ? Optional.empty() : Optional.of(true)),
                        Codec.INT.fieldOf("order").forGetter(d -> d.order),
                        Codec.STRING.optionalFieldOf("required_mod").forGetter(d -> Optional.ofNullable(d.requiredMod))
                ).apply(instance, (item, val, global, globalVal, explicit, order, requiredMod)
                        -> new ClaimPermission.Builder(item, order, null).defaultVal(val).global(global).globalVal(globalVal.orElse(true)).requireExplicitSet(explicit.orElse(false)).requiredMod(requiredMod.orElse(null))));

        private final ItemStackHolder guiItem;
        /**
         * Only used for datagen
         */
        public final List<String> desc;
        private boolean defaultVal;
        private boolean global, globalVal = true;
        private String requiredMod;

        private final int order;

        private boolean requireExplicitSet;

        public Builder(ItemStack guiItem, int order, List<String> desc) {
            this(new ItemStackHolder(guiItem), order, desc);
        }

        /**
         * The builder for a claim permission used in datagen. And reloading.
         * See Claimpermission fields what the fields stand for
         */
        public Builder(ItemStackHolder guiItem, int order, List<String> desc) {
            this.guiItem = guiItem;
            this.desc = desc;
            this.order = order;
        }

        public Builder defaultVal(boolean defaultVal) {
            this.defaultVal = defaultVal;
            return this;
        }

        public Builder global(boolean global) {
            this.global = global;
            return this;
        }

        public Builder globalVal(boolean globalVal) {
            this.globalVal = globalVal;
            return this;
        }

        public Builder requireExplicitSet(boolean requireExplicitSet) {
            this.requireExplicitSet = requireExplicitSet;
            return this;
        }

        public Builder requiredMod(String requiredMod) {
            this.requiredMod = requiredMod;
            return this;
        }

        public boolean verify() {
            return !this.guiItem.toStack().isEmpty() && (this.requiredMod == null || CrossPlatformStuff.INSTANCE.isModLoaded(this.requiredMod));
        }

        public ClaimPermission build(Identifier id) {
            return new ClaimPermission(id, this.guiItem.toStack(), this.defaultVal, this.global, this.globalVal, this.requireExplicitSet, this.order);
        }

        public record ItemStackHolder(Identifier item, int count, DataComponentPatch components) {

            public static final Codec<ItemStackHolder> CODEC = RecordCodecBuilder.create((instance) ->
                    instance.group(Identifier.CODEC.fieldOf("id").forGetter(ItemStackHolder::item),
                            Codec.INT.optionalFieldOf("Count").forGetter(stack -> stack.count() == 1 ? Optional.empty() : Optional.of(stack.count())),
                            DataComponentPatch.CODEC.optionalFieldOf("components", DataComponentPatch.EMPTY).forGetter(stack -> stack.components)
                    ).apply(instance, (item, count, tag) -> new ItemStackHolder(item, count.orElse(1), tag)));

            public ItemStackHolder(Identifier item) {
                this(item, 1, DataComponentPatch.EMPTY);
            }

            public ItemStackHolder(ItemStack item) {
                this(BuiltInRegistries.ITEM.getKey(item.getItem()), item.getCount(), item.getComponentsPatch());
            }

            private ItemStack toStack() {
                ItemStack stack = new ItemStack(BuiltInRegistries.ITEM.getValue(this.item), this.count);
                stack.applyComponents(this.components);
                return stack;
            }
        }
    }
}
