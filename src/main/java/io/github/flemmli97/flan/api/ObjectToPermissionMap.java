package io.github.flemmli97.flan.api;

import net.minecraft.block.AbstractButtonBlock;
import net.minecraft.block.AbstractPressurePlateBlock;
import net.minecraft.block.AbstractRedstoneGateBlock;
import net.minecraft.block.AnvilBlock;
import net.minecraft.block.BeaconBlock;
import net.minecraft.block.BedBlock;
import net.minecraft.block.BellBlock;
import net.minecraft.block.Block;
import net.minecraft.block.CampfireBlock;
import net.minecraft.block.ChorusFlowerBlock;
import net.minecraft.block.DaylightDetectorBlock;
import net.minecraft.block.DoorBlock;
import net.minecraft.block.EnchantingTableBlock;
import net.minecraft.block.EnderChestBlock;
import net.minecraft.block.FarmlandBlock;
import net.minecraft.block.FenceGateBlock;
import net.minecraft.block.JukeboxBlock;
import net.minecraft.block.LeverBlock;
import net.minecraft.block.NetherPortalBlock;
import net.minecraft.block.NoteBlock;
import net.minecraft.block.PointedDripstoneBlock;
import net.minecraft.block.RedstoneWireBlock;
import net.minecraft.block.TargetBlock;
import net.minecraft.block.TntBlock;
import net.minecraft.block.TrapdoorBlock;
import net.minecraft.block.TurtleEggBlock;
import net.minecraft.item.BucketItem;
import net.minecraft.item.EnderPearlItem;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.registry.Registry;

import java.util.HashMap;
import java.util.Map;
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
        for (Block block : Registry.BLOCK) {
            blockPermissionBuilder.entrySet().stream().filter(e -> e.getKey().test(block)).map(Map.Entry::getValue).findFirst().ifPresent(sub -> blockToPermission.put(block, sub.get()));
        }
        for (Item item : Registry.ITEM) {
            itemPermissionBuilder.entrySet().stream().filter(e -> e.getKey().test(item)).map(Map.Entry::getValue).findFirst().ifPresent(sub -> itemToPermission.put(item, sub.get()));
        }
    }

    public static ClaimPermission getFromBlock(Block block) {
        return blockToPermission.get(block);
    }

    public static ClaimPermission getFromItem(Item block) {
        return itemToPermission.get(block);
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
        registerBlockPredicateMap((block) -> block instanceof AnvilBlock, () -> PermissionRegistry.ANVIL);
        registerBlockPredicateMap((block) -> block instanceof BedBlock, () -> PermissionRegistry.BED);
        registerBlockPredicateMap((block) -> block instanceof BeaconBlock, () -> PermissionRegistry.BEACON);
        registerBlockPredicateMap((block) -> block instanceof DoorBlock, () -> PermissionRegistry.DOOR);
        registerBlockPredicateMap((block) -> block instanceof FenceGateBlock, () -> PermissionRegistry.FENCEGATE);
        registerBlockPredicateMap((block) -> block instanceof TrapdoorBlock, () -> PermissionRegistry.TRAPDOOR);
        registerBlockPredicateMap((block) -> block instanceof LeverBlock || block instanceof AbstractButtonBlock, () -> PermissionRegistry.BUTTONLEVER);
        registerBlockPredicateMap((block) -> block instanceof NoteBlock, () -> PermissionRegistry.NOTEBLOCK);
        registerBlockPredicateMap((block) -> block instanceof AbstractRedstoneGateBlock || block instanceof RedstoneWireBlock || block instanceof DaylightDetectorBlock, () -> PermissionRegistry.REDSTONE);
        registerBlockPredicateMap((block) -> block instanceof JukeboxBlock, () -> PermissionRegistry.JUKEBOX);
        registerBlockPredicateMap((block) -> block instanceof AbstractPressurePlateBlock, () -> PermissionRegistry.PRESSUREPLATE);
        registerBlockPredicateMap((block) -> block instanceof NetherPortalBlock, () -> PermissionRegistry.PORTAL);
        registerBlockPredicateMap((block) -> block instanceof TurtleEggBlock || block instanceof FarmlandBlock, () -> PermissionRegistry.TRAMPLE);
        registerBlockPredicateMap((block) -> block instanceof TargetBlock, () -> PermissionRegistry.TARGETBLOCK);
        registerBlockPredicateMap((block) -> block instanceof BellBlock || block instanceof CampfireBlock
                || block instanceof TntBlock || block instanceof ChorusFlowerBlock || block instanceof PointedDripstoneBlock, () -> PermissionRegistry.PROJECTILES);
        registerBlockPredicateMap((block) -> block instanceof EnderChestBlock, () -> PermissionRegistry.ENDERCHEST);
        registerBlockPredicateMap((block) -> block instanceof EnchantingTableBlock, () -> PermissionRegistry.ENCHANTMENTTABLE);

        registerItemPredicateMap(item -> item instanceof EnderPearlItem, () -> PermissionRegistry.ENDERPEARL);
        registerItemPredicateMap(item -> item instanceof BucketItem, () -> PermissionRegistry.BUCKET);
        registerItemPredicateMap(item -> item == Items.END_CRYSTAL, () -> PermissionRegistry.ENDCRYSTALPLACE);
        registerItemPredicateMap(item -> item == Items.CHORUS_FRUIT, () -> PermissionRegistry.CHORUSFRUIT);
    }
}
