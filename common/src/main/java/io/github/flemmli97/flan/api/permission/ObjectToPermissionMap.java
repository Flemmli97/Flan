package io.github.flemmli97.flan.api.permission;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.ThrownEgg;
import net.minecraft.world.entity.projectile.ThrownEnderpearl;
import net.minecraft.world.entity.projectile.ThrownPotion;
import net.minecraft.world.entity.projectile.windcharge.WindCharge;
import net.minecraft.world.item.BoatItem;
import net.minecraft.world.item.BoneMealItem;
import net.minecraft.world.item.BrushItem;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.EnderpearlItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
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
import net.minecraft.world.level.block.DecoratedPotBlock;
import net.minecraft.world.level.block.DiodeBlock;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.EnchantingTableBlock;
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
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * Assign items/blocks to a certain permission
 */
public class ObjectToPermissionMap {

    public static final Map<Predicate<Block>, Supplier<ResourceLocation>> BLOCK_PERMISSION_BUILDER = new HashMap<>();
    public static final Map<Predicate<Item>, Supplier<ResourceLocation>> ITEM_PERMISSION_BUILDER = new HashMap<>();
    public static final Map<Predicate<Block>, Supplier<ResourceLocation>> PROJECTILE_BLOCK_PERMISSION_BUILDER = new HashMap<>();
    public static final Map<Predicate<Entity>, Supplier<ResourceLocation>> PROJECTILE_PERMISSION_BUILDER = new HashMap<>();

    /**
     * Register a custom permission to check for the given blocks. Used when trying to interact with blocks
     *
     * @param pred Predicate for blocks that should return the given permission
     * @param perm The given permission
     */
    public static void registerBlockPredicateMap(Predicate<Block> pred, Supplier<ResourceLocation> perm) {
        BLOCK_PERMISSION_BUILDER.put(pred, perm);
    }

    /**
     * Register a custom permission to check for the given items. Used when trying to use items.
     *
     * @param pred Predicate for items that should return the given permission
     * @param perm The given permission
     */
    public static void registerItemPredicateMap(Predicate<Item> pred, Supplier<ResourceLocation> perm) {
        ITEM_PERMISSION_BUILDER.put(pred, perm);
    }

    /**
     * Register a custom permission to check for the given block when hit with a projectile
     *
     * @param pred Predicate for blocks that should return the given permission
     * @param perm The given permission
     */
    public static void registerProjectileBlockPredicateMap(Predicate<Block> pred, Supplier<ResourceLocation> perm) {
        PROJECTILE_BLOCK_PERMISSION_BUILDER.put(pred, perm);
    }

    /**
     * Register a custom permission to check for the given projectiles
     *
     * @param pred Predicate for entities that should return the given permission
     * @param perm The given permission
     */
    public static void registerProjectilePredicateMap(Predicate<Entity> pred, Supplier<ResourceLocation> perm) {
        PROJECTILE_PERMISSION_BUILDER.put(pred, perm);
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
        registerBlockPredicateMap(block -> block instanceof EnderChestBlock, () -> BuiltinPermission.ENDERCHEST);
        registerBlockPredicateMap(block -> block instanceof EnchantingTableBlock, () -> BuiltinPermission.ENCHANTMENTTABLE);
        registerBlockPredicateMap(block -> block instanceof BrushableBlock, () -> BuiltinPermission.ARCHAEOLOGY);

        registerItemPredicateMap(item -> item instanceof EnderpearlItem, () -> BuiltinPermission.ENDERPEARL);
        registerItemPredicateMap(item -> item instanceof BucketItem, () -> BuiltinPermission.BUCKET);
        registerItemPredicateMap(item -> item == Items.END_CRYSTAL, () -> BuiltinPermission.ENDCRYSTALPLACE);
        registerItemPredicateMap(item -> item == Items.CHORUS_FRUIT, () -> BuiltinPermission.CHORUSFRUIT);
        registerItemPredicateMap(item -> item == Items.LILY_PAD, () -> BuiltinPermission.PLACE);
        registerItemPredicateMap(item -> item instanceof BoneMealItem, () -> BuiltinPermission.PLACE);
        registerItemPredicateMap(item -> item instanceof BoatItem, () -> BuiltinPermission.BOAT);
        registerItemPredicateMap(item -> item instanceof BrushItem, () -> BuiltinPermission.ARCHAEOLOGY);
        registerItemPredicateMap(item -> item == Items.WIND_CHARGE, () -> BuiltinPermission.WIND_CHARGE);

        registerBlockPredicateMap(block -> block instanceof TargetBlock, () -> BuiltinPermission.TARGETBLOCK);
        registerProjectileBlockPredicateMap(block -> block instanceof BellBlock || block instanceof CampfireBlock
                || block instanceof TntBlock || block instanceof ChorusFlowerBlock
                || block instanceof DecoratedPotBlock, () -> BuiltinPermission.PROJECTILES);

        registerProjectilePredicateMap(entity -> entity instanceof ThrownEnderpearl, () -> BuiltinPermission.ENDERPEARL);
        registerProjectilePredicateMap(entity -> entity instanceof WindCharge, () -> BuiltinPermission.WIND_CHARGE);
        registerProjectilePredicateMap(entity -> entity instanceof ThrownEgg || entity instanceof ThrownPotion, () -> BuiltinPermission.PROJECTILES);
        registerProjectilePredicateMap(entity -> entity instanceof ThrownEnderpearl, () -> BuiltinPermission.ENDERPEARL);
    }
}
