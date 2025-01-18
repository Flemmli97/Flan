package io.github.flemmli97.flan.api.permission;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.flemmli97.flan.platform.CrossPlatformStuff;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Optional;

/**
 * Instance for a permission for claims. The builder is nearly identical to the instance.
 */
public class ClaimPermission implements Comparable<ClaimPermission> {

    /**
     * Item to show in the gui
     */
    private final ItemStack guiItem;
    public final List<String> desc;
    private final ResourceLocation id;
    public final boolean defaultVal;
    /**
     * Whether this permission is a global permission or not.
     * Global permission cannot be configured for player groups
     */
    public final boolean global;
    /**
     * The order in which this permission appears in the gui. If a permission has the same ordering the id will determine the order
     */
    public final int order;

    private ClaimPermission(ResourceLocation id, ItemStack guiItem, boolean defaultVal, boolean global, int order, List<String> defaultDescription) {
        this.id = id;
        this.guiItem = guiItem;
        this.order = order;
        this.desc = defaultDescription;
        this.defaultVal = defaultVal;
        this.global = global;
    }

    public ItemStack getItem() {
        return this.guiItem.copy();
    }

    public ResourceLocation getId() {
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
            return this.id.compareTo(other.id);
        return Integer.compare(this.order, other.order);
    }

    public static class Builder {

        public static final Codec<ClaimPermission.Builder> CODEC = RecordCodecBuilder.create((instance) ->
                instance.group(
                        Codec.INT.fieldOf("order").forGetter(d -> d.order),
                        Codec.STRING.listOf().fieldOf("defaultDescription").forGetter(d -> d.desc),
                        Codec.STRING.optionalFieldOf("requiredMod").forGetter(d -> Optional.ofNullable(d.requiredMod)),

                        ItemStackHolder.CODEC.fieldOf("guiItem").forGetter(d -> d.guiItem),
                        Codec.BOOL.fieldOf("defaultVal").forGetter(d -> d.defaultVal),
                        Codec.BOOL.fieldOf("global").forGetter(d -> d.global)
                ).apply(instance, (order, desc, requiredMod, item, val, global) -> new ClaimPermission.Builder(item, val, global, order, requiredMod.orElse(null), desc)));

        private final ItemStackHolder guiItem;
        private final List<String> desc;
        private final boolean defaultVal;
        private final boolean global;
        private final String requiredMod;

        private final int order;

        public Builder(ItemStack guiItem, boolean defaultVal, boolean global, int order, List<String> desc) {
            this(new ItemStackHolder(guiItem), defaultVal, global, order, desc);
        }

        public Builder(ItemStackHolder guiItem, boolean defaultVal, boolean global, int order, List<String> desc) {
            this(guiItem, defaultVal, global, order, null, desc);
        }

        /**
         * The builder for a claim permission used in datagen. And reloading.
         * See Claimpermission fields what the fields stand for
         */
        public Builder(ItemStackHolder guiItem, boolean defaultVal, boolean global, int order, String requiredMod, List<String> desc) {
            this.guiItem = guiItem;
            this.desc = desc;
            this.defaultVal = defaultVal;
            this.global = global;
            this.order = order;
            this.requiredMod = requiredMod;
        }

        public boolean verify() {
            return !this.guiItem.toStack().isEmpty() && (this.requiredMod == null || CrossPlatformStuff.INSTANCE.isModLoaded(this.requiredMod));
        }

        public ClaimPermission build(ResourceLocation id) {
            return new ClaimPermission(id, this.guiItem.toStack(), this.defaultVal, this.global, this.order, this.desc);
        }

        public record ItemStackHolder(ResourceLocation item, int count, CompoundTag tag) {

            public static final Codec<ItemStackHolder> CODEC = RecordCodecBuilder.create((instance) ->
                    instance.group(ResourceLocation.CODEC.fieldOf("id").forGetter(ItemStackHolder::item),
                            Codec.INT.optionalFieldOf("Count").forGetter(stack -> stack.count() == 1 ? Optional.empty() : Optional.of(stack.count())),
                            CompoundTag.CODEC.optionalFieldOf("tag").forGetter((stack) -> Optional.ofNullable(stack.tag()))
                    ).apply(instance, (item, count, tag) -> new ItemStackHolder(item, count.orElse(1), tag.orElse(null))));

            public ItemStackHolder(ResourceLocation item) {
                this(item, 1, null);
            }

            public ItemStackHolder(ItemStack item) {
                this(BuiltInRegistries.ITEM.getKey(item.getItem()), item.getCount(), item.getTag());
            }

            private ItemStack toStack() {
                ItemStack stack = new ItemStack(BuiltInRegistries.ITEM.get(this.item), this.count);
                stack.setTag(this.tag);
                return stack;
            }
        }
    }
}
