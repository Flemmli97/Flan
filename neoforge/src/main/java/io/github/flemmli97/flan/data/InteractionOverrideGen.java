package io.github.flemmli97.flan.data;

import io.github.flemmli97.flan.Flan;
import io.github.flemmli97.flan.api.permission.BuiltinPermission;
import io.github.flemmli97.flan.api.permission.InteractionOverrideManager;
import io.github.flemmli97.flan.api.permission.provider.InteractionOverrideProvider;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

public class InteractionOverrideGen extends InteractionOverrideProvider {

    public InteractionOverrideGen(PackOutput output) {
        super(output);
    }

    @Override
    protected void add() {
        this.override(id("storage_drawers"), Builder.blockInteractions(InteractionOverrideManager.BLOCK_LEFT_CLICK)
                .addEntry(blockTag(ResourceLocation.fromNamespaceAndPath("storagedrawers", "drawers")), BuiltinPermission.OPENCONTAINER));
        this.override(id("mekanism_bin"), Builder.blockInteractions(InteractionOverrideManager.BLOCK_LEFT_CLICK)
                .addEntry(ResourceLocation.fromNamespaceAndPath("mekanism", "basic_bin"), BuiltinPermission.OPENCONTAINER)
                .addEntry(ResourceLocation.fromNamespaceAndPath("mekanism", "advanced_bin"), BuiltinPermission.OPENCONTAINER)
                .addEntry(ResourceLocation.fromNamespaceAndPath("mekanism", "ultimate_bin"), BuiltinPermission.OPENCONTAINER)
                .addEntry(ResourceLocation.fromNamespaceAndPath("mekanism", "creative_bin"), BuiltinPermission.OPENCONTAINER));

        this.override(id("wrenches"), Builder.itemInteractions(InteractionOverrideManager.ITEM_USE)
                .addEntry(itemTag(ResourceLocation.fromNamespaceAndPath("c", "wrenches")), BuiltinPermission.INTERACTBLOCK));

        this.override(id("wrenches_ae2"), Builder.itemInteractions(InteractionOverrideManager.ITEM_USE)
                .addEntry(ResourceLocation.fromNamespaceAndPath("appliedenergistics2", "nether_quartz_wrench"), BuiltinPermission.INTERACTBLOCK)
                .addEntry(ResourceLocation.fromNamespaceAndPath("appliedenergistics2", "certus_quartz_wrench"), BuiltinPermission.INTERACTBLOCK));

        this.override(id("npc"), Builder.entityInteractions(InteractionOverrideManager.ENTITY_INTERACT)
                .addEntry(ResourceLocation.fromNamespaceAndPath("taterzens", "npc"), BuiltinPermission.TRADING));
    }

    private static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(Flan.MODID, path);
    }

    private static TagKey<Block> blockTag(ResourceLocation block) {
        return TagKey.create(Registries.BLOCK, block);
    }

    private static TagKey<Item> itemTag(ResourceLocation block) {
        return TagKey.create(Registries.ITEM, block);
    }

    private static TagKey<EntityType<?>> entityTag(ResourceLocation block) {
        return TagKey.create(Registries.ENTITY_TYPE, block);
    }
}
