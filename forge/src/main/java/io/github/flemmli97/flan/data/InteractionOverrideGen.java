package io.github.flemmli97.flan.data;

import io.github.flemmli97.flan.Flan;
import io.github.flemmli97.flan.api.permission.BuiltinPermission;
import io.github.flemmli97.flan.api.permission.InteractionOverrideManager;
import io.github.flemmli97.flan.api.permission.provider.InteractionOverrideProvider;
import net.minecraft.core.Registry;
import net.minecraft.data.DataGenerator;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

public class InteractionOverrideGen extends InteractionOverrideProvider {

    public InteractionOverrideGen(DataGenerator gen) {
        super(gen);
    }

    @Override
    protected void add() {
        this.override(id("storage_drawers"), Builder.blockInteractions(InteractionOverrideManager.BLOCK_LEFT_CLICK)
                .addEntry(blockTag(new ResourceLocation("storagedrawers", "drawers")), BuiltinPermission.OPENCONTAINER));
        this.override(id("mekanism_bin"), Builder.blockInteractions(InteractionOverrideManager.BLOCK_LEFT_CLICK)
                .addEntry(new ResourceLocation("mekanism", "basic_bin"), BuiltinPermission.OPENCONTAINER)
                .addEntry(new ResourceLocation("mekanism", "advanced_bin"), BuiltinPermission.OPENCONTAINER)
                .addEntry(new ResourceLocation("mekanism", "ultimate_bin"), BuiltinPermission.OPENCONTAINER)
                .addEntry(new ResourceLocation("mekanism", "creative_bin"), BuiltinPermission.OPENCONTAINER));

        this.override(id("wrenches"), Builder.itemInteractions(InteractionOverrideManager.ITEM_USE)
                .addEntry(itemTag(new ResourceLocation("c", "wrenches")), BuiltinPermission.INTERACTBLOCK));

        this.override(id("wrenches_ae2"), Builder.itemInteractions(InteractionOverrideManager.ITEM_USE)
                .addEntry(new ResourceLocation("appliedenergistics2", "nether_quartz_wrench"), BuiltinPermission.INTERACTBLOCK)
                .addEntry(new ResourceLocation("appliedenergistics2", "certus_quartz_wrench"), BuiltinPermission.INTERACTBLOCK));

        this.override(id("npc"), Builder.entityInteractions(InteractionOverrideManager.ENTITY_INTERACT)
                .addEntry(new ResourceLocation("taterzens", "npc"), BuiltinPermission.TRADING));
    }

    private static ResourceLocation id(String path) {
        return new ResourceLocation(Flan.MODID, path);
    }

    private static TagKey<Block> blockTag(ResourceLocation block) {
        return TagKey.create(Registry.BLOCK_REGISTRY, block);
    }

    private static TagKey<Item> itemTag(ResourceLocation block) {
        return TagKey.create(Registry.ITEM_REGISTRY, block);
    }

    private static TagKey<EntityType<?>> entityTag(ResourceLocation block) {
        return TagKey.create(Registry.ENTITY_TYPE_REGISTRY, block);
    }
}
