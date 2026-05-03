package io.github.flemmli97.flan.claim.attachment;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.datafixers.util.Either;
import io.github.flemmli97.flan.Flan;
import io.github.flemmli97.flan.claim.Claim;
import io.github.flemmli97.flan.gui.ServerScreenHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.Identifier;
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
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;

public class AllowedRegistryList<T> {

    public static final Function<EntityType<?>, Item> ENTITY_AS_ITEM = type -> {
        Optional<Holder<Item>> egg = SpawnEggItem.byId(type);
        return egg.map(Holder::value).orElse(Items.PIG_SPAWN_EGG);
    };

    private final Map<String, Integer> mapping = new HashMap<>();
    private final List<Either<T, TagKey<T>>> list = new ArrayList<>();
    private final Registry<T> registry;
    private final Claim claim;
    private final Function<T, Item> asItem;
    private boolean blacklist;

    public AllowedRegistryList(Registry<T> registry, Claim claim, Function<T, Item> asItem) {
        this.registry = registry;
        this.claim = claim;
        this.asItem = asItem;
    }

    public static <T extends ItemLike> AllowedRegistryList<T> ofItemLike(Registry<T> registry, Claim claim) {
        return new AllowedRegistryList<>(registry, claim, ItemLike::asItem);
    }

    public List<List<ItemStack>> asStacks() {
        return this.list.stream().map(e ->
                e.map(v -> List.of(new ItemStack(this.asItem.apply(v))), tag -> {
                    List<ItemStack> items = this.registry.get(tag).map(f ->
                            f.stream().map(h -> new ItemStack(this.asItem.apply(h.value()))).toList()).orElse(List.of(this.empty()));
                    items.forEach(stack -> stack.set(DataComponents.CUSTOM_NAME, ServerScreenHelper.coloredGuiText(String.format("#%s", tag.location()), ChatFormatting.GOLD)));
                    return items;
                })
        ).toList();
    }

    public List<String> asString() {
        return this.list.stream().map(this::valueAsString).toList();
    }

    public int size() {
        return this.list.size();
    }

    public boolean blacklist() {
        return this.blacklist;
    }

    public void setBlacklist(boolean blacklist) {
        this.blacklist = blacklist;
    }

    private ItemStack empty() {
        ItemStack stack = new ItemStack(Items.STICK);
        ServerScreenHelper.addLore(stack, ServerScreenHelper.coloredGuiText("flan.allowListEmptyTag", ChatFormatting.DARK_RED));
        return stack;
    }

    public void addAllowedItem(String value) {
        if (value.startsWith("#"))
            this.addAllowedItem(Either.right(TagKey.create(this.registry.key(), Identifier.parse(value.substring(1)))));
        else {
            this.registry.getOptional(Identifier.parse(value))
                    .ifPresent(direct -> this.addAllowedItem(Either.left(direct)));
        }
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
        if (this.blacklist) {
            return this.list.stream().noneMatch(e -> e.map(first::test, second::test));
        }
        return this.list.stream().anyMatch(e -> e.map(first::test, second::test));
    }

    private String valueAsString(Either<T, TagKey<T>> val) {
        return val.map(i -> this.registry.getKey(i).toString(), tag -> "#" + tag.location());
    }

    public JsonElement save() {
        JsonObject obj = new JsonObject();
        JsonArray array = new JsonArray();
        this.list.forEach(e -> array.add(this.valueAsString(e)));
        obj.add("entries", array);
        obj.addProperty("blacklist", this.blacklist);
        return obj;
    }

    public AllowedRegistryList<T> read(JsonElement element) {
        this.list.clear();
        JsonArray array;
        if (element.isJsonArray()) {
            array = element.getAsJsonArray();
        } else {
            JsonObject obj = element.getAsJsonObject();
            array = obj.getAsJsonArray("entries");
            this.blacklist = obj.get("blacklist").getAsBoolean();
        }
        array.forEach(e -> {
            String entry = e.getAsString();
            if (entry.startsWith("#"))
                this.addAllowedItem(Either.right(TagKey.create(this.registry.key(), Identifier.parse(entry.substring(1)))));
            else {
                Identifier id = Identifier.parse(entry);
                if (this.registry.containsKey(id)) {
                    this.addAllowedItem(Either.left(this.registry.getValue(id)));
                } else {
                    Flan.LOGGER.error("No such registry item for {} with id: {}", this.registry.key(), id);
                }
            }
        });
        return this;
    }
}
