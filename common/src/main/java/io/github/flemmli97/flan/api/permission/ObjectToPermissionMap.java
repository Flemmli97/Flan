package io.github.flemmli97.flan.api.permission;

import com.mojang.datafixers.util.Pair;
import io.github.flemmli97.flan.Flan;
import io.github.flemmli97.flan.config.ConfigHandler;
import io.github.flemmli97.flan.platform.CrossPlatformStuff;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.BoatItem;
import net.minecraft.world.item.BoneMealItem;
import net.minecraft.world.item.BrushItem;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.EnderpearlItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.RecordItem;
import net.minecraft.world.level.block.AnvilBlock;
import net.minecraft.world.level.block.BasePressurePlateBlock;
import net.minecraft.world.level.block.BeaconBlock;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.BellBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BrushableBlock;
import net.minecraft.world.level.block.ButtonBlock;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.ChorusFlowerBlock;
import net.minecraft.world.level.block.DaylightDetectorBlock;
import net.minecraft.world.level.block.DiodeBlock;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.EnchantmentTableBlock;
import net.minecraft.world.level.block.EnderChestBlock;
import net.minecraft.world.level.block.FarmBlock;
import net.minecraft.world.level.block.FenceGateBlock;
import net.minecraft.world.level.block.JukeboxBlock;
import net.minecraft.world.level.block.LeverBlock;
import net.minecraft.world.level.block.NetherPortalBlock;
import net.minecraft.world.level.block.NoteBlock;
import net.minecraft.world.level.block.RedStoneWireBlock;
import net.minecraft.world.level.block.TargetBlock;
import net.minecraft.world.level.block.TntBlock;
import net.minecraft.world.level.block.TrapDoorBlock;
import net.minecraft.world.level.block.TurtleEggBlock;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * Assign items/blocks to a certain permission
 */
public class ObjectToPermissionMap {

    private static final Map<Block, ResourceLocation> blockToPermission = new HashMap<>();
    private static final Map<Predicate<Block>, Supplier<ResourceLocation>> blockPermissionBuilder = new HashMap<>();

    private static final Map<Item, ResourceLocation> itemToPermission = new HashMap<>();
    private static final Map<Predicate<Item>, Supplier<ResourceLocation>> itemPermissionBuilder = new HashMap<>();

    private static final Map<EntityType<?>, ResourceLocation> entityToPermission = new HashMap<>();

    private static final Map<Block, ResourceLocation> leftClickBlockPermission = new HashMap<>();

    public static void reload(MinecraftServer server) {
        blockToPermission.clear();
        itemToPermission.clear();
        entityToPermission.clear();
        leftClickBlockPermission.clear();
        for (Block block : CrossPlatformStuff.INSTANCE.registryBlocks().getIterator()) {
            blockPermissionBuilder.entrySet().stream().filter(e -> e.getKey().test(block)).map(Map.Entry::getValue).findFirst().ifPresent(sub -> blockToPermission.put(block, sub.get()));
        }
        for (Item item : CrossPlatformStuff.INSTANCE.registryItems().getIterator()) {
            itemPermissionBuilder.entrySet().stream().filter(e -> e.getKey().test(item)).map(Map.Entry::getValue).findFirst().ifPresent(sub -> itemToPermission.put(item, sub.get()));
        }
        process(ConfigHandler.CONFIG.itemPermission, BuiltInRegistries.ITEM, itemToPermission);
        process(ConfigHandler.CONFIG.blockPermission, BuiltInRegistries.BLOCK, blockToPermission);
        process(ConfigHandler.CONFIG.entityPermission, BuiltInRegistries.ENTITY_TYPE, entityToPermission);
        process(ConfigHandler.CONFIG.leftClickBlockPermission, BuiltInRegistries.BLOCK, leftClickBlockPermission);
    }

    private static <T> void process(List<String> list, Registry<T> registry, Map<T, ResourceLocation> map) {
        for (String s : list) {
            String[] sub = s.split("-");
            boolean remove = sub[1].equals("NONE");
            if (s.startsWith("@")) {
                ResourceLocation res = new ResourceLocation(sub[0].substring(1));
                processTag(res, registry, b -> {
                    if (remove)
                        map.remove(b);
                    else {
                        ResourceLocation id = BuiltinPermission.tryLegacy(sub[1]);
                        ClaimPermission perm = PermissionManager.INSTANCE.get(id);
                        if (perm == null)
                            Flan.error("Configuring custom permission map: No such permission for {}", sub[1]);
                        map.put(b, id);
                    }
                });
            } else {
                if (remove)
                    map.remove(registry.get(new ResourceLocation(sub[0])));
                else {
                    ResourceLocation id = BuiltinPermission.tryLegacy(sub[1]);
                    ClaimPermission perm = PermissionManager.INSTANCE.get(id);
                    if (perm == null)
                        Flan.error("Configuring custom permission map: No such permission for {} {}", sub[1], id);
                    map.put(registry.get(new ResourceLocation(sub[0])), id);
                }
            }
        }
    }

    private static <T> void processTag(ResourceLocation tag, Registry<T> registry, Consumer<T> action) {
        Optional<HolderSet.Named<T>> t = registry.getTags().filter(p -> p.getFirst().location().equals(tag))
                .map(Pair::getSecond).findFirst();
        t.ifPresent(holders -> holders.forEach(i -> action.accept(i.value())));
    }

    public static ResourceLocation getFromBlock(Block block) {
        return blockToPermission.get(block);
    }

    public static ResourceLocation getFromItem(Item item) {
        return itemToPermission.get(item);
    }

    public static ResourceLocation getFromEntity(EntityType<?> entity) {
        return entityToPermission.get(entity);
    }

    public static ResourceLocation getForLeftClickBlock(Block block) {
        return leftClickBlockPermission.get(block);
    }

    /**
     * Register a custom permission to check for the given blocks. Used when trying to interact with blocks
     * Register before ServerLifecycleEvents.SERVER_STARTING
     *
     * @param pred Predicate for blocks that should return the given permission
     * @param perm The given permission
     */
    public static void registerBlockPredicateMap(Predicate<Block> pred, Supplier<ResourceLocation> perm) {
        blockPermissionBuilder.put(pred, perm);
    }

    /**
     * Register a custom permission to check for the given items. Used when trying to use items.
     * Register before ServerLifecycleEvents.SERVER_STARTING
     *
     * @param pred Predicate for items that should return the given permission
     * @param perm The given permission
     */
    public static void registerItemPredicateMap(Predicate<Item> pred, Supplier<ResourceLocation> perm) {
        itemPermissionBuilder.put(pred, perm);
    }

    static {
        registerBlockPredicateMap(block -> block instanceof AnvilBlock, () -> BuiltinPermission.ANVIL);
        registerBlockPredicateMap(block -> block instanceof BedBlock, () -> BuiltinPermission.BED);
        registerBlockPredicateMap(block -> block instanceof BeaconBlock, () -> BuiltinPermission.BEACON);
        registerBlockPredicateMap(block -> block instanceof DoorBlock, () -> BuiltinPermission.DOOR);
        registerBlockPredicateMap(block -> block instanceof FenceGateBlock, () -> BuiltinPermission.FENCEGATE);
        registerBlockPredicateMap(block -> block instanceof TrapDoorBlock, () -> BuiltinPermission.TRAPDOOR);
        registerBlockPredicateMap(block -> block instanceof LeverBlock || block instanceof ButtonBlock, () -> BuiltinPermission.BUTTONLEVER);
        registerBlockPredicateMap(block -> block instanceof NoteBlock, () -> BuiltinPermission.NOTEBLOCK);
        registerBlockPredicateMap(block -> block instanceof DiodeBlock || block instanceof RedStoneWireBlock || block instanceof DaylightDetectorBlock, () -> BuiltinPermission.REDSTONE);
        registerBlockPredicateMap(block -> block instanceof JukeboxBlock, () -> BuiltinPermission.JUKEBOX);
        registerBlockPredicateMap(block -> block instanceof BasePressurePlateBlock, () -> BuiltinPermission.PRESSUREPLATE);
        registerBlockPredicateMap(block -> block instanceof NetherPortalBlock, () -> BuiltinPermission.PORTAL);
        registerBlockPredicateMap(block -> block instanceof TurtleEggBlock || block instanceof FarmBlock, () -> BuiltinPermission.TRAMPLE);
        registerBlockPredicateMap(block -> block instanceof TargetBlock, () -> BuiltinPermission.TARGETBLOCK);
        registerBlockPredicateMap(block -> block instanceof BellBlock || block instanceof CampfireBlock
                || block instanceof TntBlock || block instanceof ChorusFlowerBlock, () -> BuiltinPermission.PROJECTILES);
        registerBlockPredicateMap(block -> block instanceof EnderChestBlock, () -> BuiltinPermission.ENDERCHEST);
        registerBlockPredicateMap(block -> block instanceof EnchantmentTableBlock, () -> BuiltinPermission.ENCHANTMENTTABLE);
        registerBlockPredicateMap(block -> block instanceof BrushableBlock, () -> BuiltinPermission.ARCHAEOLOGY);

        registerItemPredicateMap(item -> item instanceof EnderpearlItem, () -> BuiltinPermission.ENDERPEARL);
        registerItemPredicateMap(item -> item instanceof BucketItem, () -> BuiltinPermission.BUCKET);
        registerItemPredicateMap(item -> item == Items.END_CRYSTAL, () -> BuiltinPermission.ENDCRYSTALPLACE);
        registerItemPredicateMap(item -> item == Items.CHORUS_FRUIT, () -> BuiltinPermission.CHORUSFRUIT);
        registerItemPredicateMap(item -> item == Items.LILY_PAD, () -> BuiltinPermission.PLACE);
        registerItemPredicateMap(item -> item instanceof BoneMealItem, () -> BuiltinPermission.PLACE);
        registerItemPredicateMap(item -> item instanceof RecordItem, () -> BuiltinPermission.JUKEBOX);
        registerItemPredicateMap(item -> item instanceof BoatItem, () -> BuiltinPermission.BOAT);
        registerItemPredicateMap(item -> item instanceof BrushItem, () -> BuiltinPermission.ARCHAEOLOGY);
    }
}
