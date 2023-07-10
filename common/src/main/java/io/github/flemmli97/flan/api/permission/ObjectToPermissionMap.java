package io.github.flemmli97.flan.api.permission;

import com.mojang.datafixers.util.Pair;
import io.github.flemmli97.flan.config.ConfigHandler;
import io.github.flemmli97.flan.platform.CrossPlatformStuff;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.item.BoatItem;
import net.minecraft.world.item.BoneMealItem;
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
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * Assign items/blocks to a certain permission
 */
public class ObjectToPermissionMap {

    private static final Map<Block, ClaimPermission> blockToPermission = new HashMap<>();
    private static final Map<Predicate<Block>, Supplier<ClaimPermission>> blockPermissionBuilder = new HashMap<>();

    private static final Map<Item, ClaimPermission> itemToPermission = new HashMap<>();
    private static final Map<Predicate<Item>, Supplier<ClaimPermission>> itemPermissionBuilder = new HashMap<>();

    public static void reload(MinecraftServer server) {
        blockToPermission.clear();
        itemToPermission.clear();
        for (Block block : CrossPlatformStuff.INSTANCE.registryBlocks().getIterator()) {
            blockPermissionBuilder.entrySet().stream().filter(e -> e.getKey().test(block)).map(Map.Entry::getValue).findFirst().ifPresent(sub -> blockToPermission.put(block, sub.get()));
        }
        for (Item item : CrossPlatformStuff.INSTANCE.registryItems().getIterator()) {
            itemPermissionBuilder.entrySet().stream().filter(e -> e.getKey().test(item)).map(Map.Entry::getValue).findFirst().ifPresent(sub -> itemToPermission.put(item, sub.get()));
        }
        for (String s : ConfigHandler.config.itemPermission) {
            String[] sub = s.split("-");
            boolean remove = sub[1].equals("NONE");
            if (s.startsWith("@")) {
                ResourceLocation res = new ResourceLocation(sub[0].substring(1));
                Optional<HolderSet.Named<Item>> t = BuiltInRegistries.ITEM.getTags().filter(p -> p.getFirst().location().equals(res))
                        .map(Pair::getSecond).findFirst();
                t.ifPresent(holders -> holders.forEach(i -> {
                    if (remove)
                        itemToPermission.remove(i.value());
                    else
                        itemToPermission.put(i.value(), PermissionRegistry.get(sub[1]));
                }));
            } else {
                if (remove)
                    itemToPermission.remove(CrossPlatformStuff.INSTANCE.registryItems().getFromId(new ResourceLocation(sub[0])));
                else
                    itemToPermission.put(CrossPlatformStuff.INSTANCE.registryItems().getFromId(new ResourceLocation(sub[0])), PermissionRegistry.get(sub[1]));
            }
        }
        for (String s : ConfigHandler.config.blockPermission) {
            String[] sub = s.split("-");
            boolean remove = sub[1].equals("NONE");
            if (s.startsWith("@")) {
                ResourceLocation res = new ResourceLocation(sub[0].substring(1));
                Optional<HolderSet.Named<Block>> t = BuiltInRegistries.BLOCK.getTags().filter(p -> p.getFirst().location().equals(res))
                        .map(Pair::getSecond).findFirst();
                t.ifPresent(holders -> holders.forEach(i -> {
                    if (remove)
                        blockToPermission.remove(i.value());
                    else
                        blockToPermission.put(i.value(), PermissionRegistry.get(sub[1]));
                }));
            } else {
                if (remove)
                    blockToPermission.remove(CrossPlatformStuff.INSTANCE.registryBlocks().getFromId(new ResourceLocation(sub[0])));
                else
                    blockToPermission.put(CrossPlatformStuff.INSTANCE.registryBlocks().getFromId(new ResourceLocation(sub[0])), PermissionRegistry.get(sub[1]));
            }
        }
    }

    public static ClaimPermission getFromBlock(Block block) {
        return blockToPermission.get(block);
    }

    public static ClaimPermission getFromItem(Item item) {
        return itemToPermission.get(item);
    }

    /**
     * Register a custom permission to check for the given blocks. Used when trying to interact with blocks
     * Register before ServerLifecycleEvents.SERVER_STARTING
     *
     * @param pred Predicate for blocks that should return the given permission
     * @param perm The given permission
     */
    public static void registerBlockPredicateMap(Predicate<Block> pred, Supplier<ClaimPermission> perm) {
        blockPermissionBuilder.put(pred, perm);
    }

    /**
     * Register a custom permission to check for the given items. Used when trying to use items.
     * Register before ServerLifecycleEvents.SERVER_STARTING
     *
     * @param pred Predicate for items that should return the given permission
     * @param perm The given permission
     */
    public static void registerItemPredicateMap(Predicate<Item> pred, Supplier<ClaimPermission> perm) {
        itemPermissionBuilder.put(pred, perm);
    }

    static {
        registerBlockPredicateMap(block -> block instanceof AnvilBlock, () -> PermissionRegistry.ANVIL);
        registerBlockPredicateMap(block -> block instanceof BedBlock, () -> PermissionRegistry.BED);
        registerBlockPredicateMap(block -> block instanceof BeaconBlock, () -> PermissionRegistry.BEACON);
        registerBlockPredicateMap(block -> block instanceof DoorBlock, () -> PermissionRegistry.DOOR);
        registerBlockPredicateMap(block -> block instanceof FenceGateBlock, () -> PermissionRegistry.FENCEGATE);
        registerBlockPredicateMap(block -> block instanceof TrapDoorBlock, () -> PermissionRegistry.TRAPDOOR);
        registerBlockPredicateMap(block -> block instanceof LeverBlock || block instanceof ButtonBlock, () -> PermissionRegistry.BUTTONLEVER);
        registerBlockPredicateMap(block -> block instanceof NoteBlock, () -> PermissionRegistry.NOTEBLOCK);
        registerBlockPredicateMap(block -> block instanceof DiodeBlock || block instanceof RedStoneWireBlock || block instanceof DaylightDetectorBlock, () -> PermissionRegistry.REDSTONE);
        registerBlockPredicateMap(block -> block instanceof JukeboxBlock, () -> PermissionRegistry.JUKEBOX);
        registerBlockPredicateMap(block -> block instanceof BasePressurePlateBlock, () -> PermissionRegistry.PRESSUREPLATE);
        registerBlockPredicateMap(block -> block instanceof NetherPortalBlock, () -> PermissionRegistry.PORTAL);
        registerBlockPredicateMap(block -> block instanceof TurtleEggBlock || block instanceof FarmBlock, () -> PermissionRegistry.TRAMPLE);
        registerBlockPredicateMap(block -> block instanceof TargetBlock, () -> PermissionRegistry.TARGETBLOCK);
        registerBlockPredicateMap(block -> block instanceof BellBlock || block instanceof CampfireBlock
                || block instanceof TntBlock || block instanceof ChorusFlowerBlock, () -> PermissionRegistry.PROJECTILES);
        registerBlockPredicateMap(block -> block instanceof EnderChestBlock, () -> PermissionRegistry.ENDERCHEST);
        registerBlockPredicateMap(block -> block instanceof EnchantmentTableBlock, () -> PermissionRegistry.ENCHANTMENTTABLE);

        registerItemPredicateMap(item -> item instanceof EnderpearlItem, () -> PermissionRegistry.ENDERPEARL);
        registerItemPredicateMap(item -> item instanceof BucketItem, () -> PermissionRegistry.BUCKET);
        registerItemPredicateMap(item -> item == Items.END_CRYSTAL, () -> PermissionRegistry.ENDCRYSTALPLACE);
        registerItemPredicateMap(item -> item == Items.CHORUS_FRUIT, () -> PermissionRegistry.CHORUSFRUIT);
        registerItemPredicateMap(item -> item == Items.LILY_PAD, () -> PermissionRegistry.PLACE);
        registerItemPredicateMap(item -> item instanceof BoneMealItem, () -> PermissionRegistry.PLACE);
        registerItemPredicateMap(item -> item instanceof RecordItem, () -> PermissionRegistry.JUKEBOX);
        registerItemPredicateMap(item -> item instanceof BoatItem, () -> PermissionRegistry.BOAT);
    }
}
