package io.github.flemmli97.flan.claim;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.mojang.datafixers.util.Either;
import io.github.flemmli97.flan.config.ConfigHandler;
import io.github.flemmli97.flan.gui.ServerScreenHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

public class AllowedRegistryList<T extends ItemLike> {

    private final List<Either<T, TagKey<T>>> list = new ArrayList<>();
    private final Registry<T> registry;
    private final Claim claim;

    public AllowedRegistryList(Registry<T> registry, Claim claim) {
        this.registry = registry;
        this.claim = claim;
    }

    public List<ItemStack> asStacks() {
        return this.list.stream().map(e ->
                e.map(ItemStack::new, tag -> {
                    ItemStack any = this.registry.getTag(tag).map(f ->
                            f.stream().map(h -> new ItemStack(h.value())).findFirst().orElse(this.empty())).orElse(this.empty());
                    any.setHoverName(ServerScreenHelper.coloredGuiText(String.format("#%s", tag.location()), ChatFormatting.GOLD));
                    return any;
                })
        ).toList();
    }

    private ItemStack empty() {
        ItemStack stack = new ItemStack(Items.STICK);
        ServerScreenHelper.addLore(stack, ServerScreenHelper.coloredGuiText(ConfigHandler.langManager.get("allowListEmptyTag"), ChatFormatting.DARK_RED));
        return stack;
    }

    public void addAllowedItem(Either<T, TagKey<T>> allowed) {
        this.list.add(allowed);
        this.claim.setDirty(true);
    }

    public void removeAllowedItem(int index) {
        if (index >= 0 && index < this.list.size()) {
            this.list.remove(index);
            this.claim.setDirty(true);
        }
    }

    public boolean matches(Predicate<T> first, Predicate<TagKey<T>> second) {
        return this.list.stream().anyMatch(e -> e.map(first::test, second::test));
    }

    public JsonElement save() {
        JsonArray array = new JsonArray();
        this.list.forEach(e -> {
            String el = e.map(i -> this.registry.getKey(i).toString(), tag -> "#" + tag.location());
            array.add(el);
        });
        return array;
    }

    public void read(JsonArray array) {
        this.list.clear();
        array.forEach(e -> {
            String element = e.getAsString();
            if (element.startsWith("#"))
                this.list.add(Either.right(TagKey.create(this.registry.key(), new ResourceLocation(element.substring(1)))));
            else
                this.list.add(Either.left(this.registry.get(new ResourceLocation(element))));
        });
    }
}
