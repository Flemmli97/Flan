package io.github.flemmli97.flan.claim;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.mojang.datafixers.util.Either;
import io.github.flemmli97.flan.Flan;
import io.github.flemmli97.flan.gui.ServerScreenHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.level.ItemLike;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;

public class AllowedRegistryList<T> {

    public static final Function<EntityType<?>, Item> ENTITY_AS_ITEM = type -> {
        Item egg = SpawnEggItem.byId(type);
        return egg != null ? egg : Items.PIG_SPAWN_EGG;
    };

    private final Map<String, Integer> mapping = new HashMap<>();
    private final List<Either<T, TagKey<T>>> list = new ArrayList<>();
    private final Registry<T> registry;
    private final Claim claim;
    private final Function<T, Item> asItem;

    public AllowedRegistryList(Registry<T> registry, Claim claim, Function<T, Item> asItem) {
        this.registry = registry;
        this.claim = claim;
        this.asItem = asItem;
    }

    public static <T extends ItemLike> AllowedRegistryList<T> ofItemLike(Registry<T> registry, Claim claim) {
        return new AllowedRegistryList<>(registry, claim, ItemLike::asItem);
    }

    public List<ItemStack> asStacks() {
        return this.list.stream().map(e ->
                e.map(v -> new ItemStack(this.asItem.apply(v)), tag -> {
                    ItemStack any = this.registry.get(tag).map(f ->
                            f.stream().map(h -> new ItemStack(this.asItem.apply(h.value()))).findFirst().orElse(this.empty())).orElse(this.empty());
                    any.set(DataComponents.CUSTOM_NAME, ServerScreenHelper.coloredGuiText(String.format("#%s", tag.location()), ChatFormatting.GOLD));
                    return any;
                })
        ).toList();
    }

    public List<String> asString() {
        return this.list.stream().map(this::valueAsString).toList();
    }

    public int size() {
        return this.list.size();
    }

    private ItemStack empty() {
        ItemStack stack = new ItemStack(Items.STICK);
        ServerScreenHelper.addLore(stack, ServerScreenHelper.coloredGuiText("flan.allowListEmptyTag", ChatFormatting.DARK_RED));
        return stack;
    }

    public void addAllowedItem(Either<T, TagKey<T>> allowed) {
        if (this.mapping.put(this.valueAsString(allowed), this.list.size()) == null) {
            this.list.add(allowed);
            this.claim.setDirty(true);
        }
    }

    public void removeAllowedItem(String value) {
        int idx = this.mapping.getOrDefault(value, 0);
        this.removeAllowedItem(idx);
    }

    public void removeAllowedItem(int index) {
        if (index >= 0 && index < this.list.size()) {
            Either<T, TagKey<T>> val = this.list.remove(index);
            this.mapping.remove(this.valueAsString(val));
            this.claim.setDirty(true);
        }
    }

    public boolean matches(Predicate<T> first, Predicate<TagKey<T>> second) {
        return this.list.stream().anyMatch(e -> e.map(first::test, second::test));
    }

    private String valueAsString(Either<T, TagKey<T>> val) {
        return val.map(i -> this.registry.getKey(i).toString(), tag -> "#" + tag.location());
    }

    public JsonElement save() {
        JsonArray array = new JsonArray();
        this.list.forEach(e -> array.add(this.valueAsString(e)));
        return array;
    }

    public void read(JsonArray array) {
        this.list.clear();
        array.forEach(e -> {
            String element = e.getAsString();
            if (element.startsWith("#"))
                this.addAllowedItem(Either.right(TagKey.create(this.registry.key(), ResourceLocation.parse(element.substring(1)))));
            else {
                ResourceLocation id = ResourceLocation.parse(element);
                if (this.registry.containsKey(id)) {
                    this.addAllowedItem(Either.left(this.registry.getValue(id)));
                } else {
                    Flan.LOGGER.error("No such registry item for {} with id: {}", this.registry.key(), id);
                }
            }
        });
    }
}
