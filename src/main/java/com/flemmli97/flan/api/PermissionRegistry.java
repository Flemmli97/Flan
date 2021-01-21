package com.flemmli97.flan.api;

import com.flemmli97.flan.config.ConfigHandler;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.Identifier;
import net.minecraft.village.raid.Raid;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Register more permissions before ServerLifecycleEvents.SERVER_STARTING
 */
public class PermissionRegistry {

    private static final Map<String, ClaimPermission> permissions = Maps.newLinkedHashMap();
    private static final Map<String, ClaimPermission> globalPermissions = Maps.newLinkedHashMap();

    private static final Map<Identifier, ClaimPermission> interactBlocks = Maps.newHashMap();
    private static final Map<Identifier, ClaimPermission> breakBlocks = Maps.newHashMap();
    private static final Map<Identifier, ClaimPermission> items = Maps.newHashMap();

    private static boolean locked;

    public static ClaimPermission EDITCLAIM = register(new ClaimPermission("EDITCLAIM", () -> new ItemStack(ConfigHandler.config.claimingItem), "Gives permission to edit (resize, delete...) the claim"));
    public static ClaimPermission EDITPERMS = register(new ClaimPermission("EDITPERMS", () -> new ItemStack(Items.COMMAND_BLOCK), "Gives permission to change the claims permissions"));
    public static ClaimPermission BREAK = register(new ClaimPermission("BREAK", () -> new ItemStack(Items.DIAMOND_PICKAXE), "Permission to break blocks in the claim"));
    public static ClaimPermission PLACE = register(new ClaimPermission("PLACE", () -> new ItemStack(Items.GRASS_BLOCK), "Permission to place blocks in the claim"));
    public static ClaimPermission OPENCONTAINER = register(new ClaimPermission("OPENCONTAINER", () -> new ItemStack(Items.CHEST), "Permission to open containers" ,"(chest, furnace etc.)"));
    public static ClaimPermission ANVIL = register(new ClaimPermission("ANVIL", () -> new ItemStack(Items.ANVIL), "Permission to use anvils"));
    public static ClaimPermission BED = register(new ClaimPermission("BED", () -> new ItemStack(Items.RED_BED), "Permission to use beds"));
    public static ClaimPermission BEACON = register(new ClaimPermission("BEACON", () -> new ItemStack(Items.BEACON), "Permission to use beacons"));
    public static ClaimPermission DOOR = register(new ClaimPermission("DOOR", () -> new ItemStack(Items.OAK_DOOR), "Permission to use doors"));
    public static ClaimPermission FENCEGATE = register(new ClaimPermission("FENCEGATE", () -> new ItemStack(Items.OAK_FENCE_GATE), "Permission to use fence gates"));
    public static ClaimPermission TRAPDOOR = register(new ClaimPermission("TRAPDOOR", () -> new ItemStack(Items.OAK_TRAPDOOR), "Permission to use trapdoors"));
    public static ClaimPermission BUTTONLEVER = register(new ClaimPermission("BUTTONLEVER", () -> new ItemStack(Items.LEVER), "Permission to trigger levers and buttons"));
    public static ClaimPermission PRESSUREPLATE = register(new ClaimPermission("PRESSUREPLATE", () -> new ItemStack(Items.STONE_PRESSURE_PLATE), "Permission to trigger pressure plates"));
    public static ClaimPermission NOTEBLOCK = register(new ClaimPermission("NOTEBLOCK", () -> new ItemStack(Items.NOTE_BLOCK), "Permission to change noteblocks"));
    public static ClaimPermission REDSTONE = register(new ClaimPermission("REDSTONE", () -> new ItemStack(Items.REDSTONE), "Permission to change redstone components"));
    public static ClaimPermission JUKEBOX = register(new ClaimPermission("JUKEBOX", () -> new ItemStack(Items.JUKEBOX), "Permission to insert/take music discs"));
    public static ClaimPermission ITEMFRAMEROTATE = register(new ClaimPermission("ITEMFRAMEROTATE", () -> new ItemStack(Items.ITEM_FRAME), "Permission to rotate items in item frames"));
    public static ClaimPermission LECTERNTAKE = register(new ClaimPermission("LECTERNTAKE", () -> new ItemStack(Items.LECTERN), "Permission to change books in a lectern"));
    public static ClaimPermission ENDCRYSTALPLACE = register(new ClaimPermission("ENDCRYSTALPLACE", () -> new ItemStack(Items.END_CRYSTAL), "Permission to place end crystals"));
    public static ClaimPermission TARGETBLOCK = register(new ClaimPermission("TARGETBLOCK", () -> new ItemStack(Items.TARGET), "Permission to trigger target blocks"));
    public static ClaimPermission PROJECTILES = register(new ClaimPermission("PROJECTILES", () -> new ItemStack(Items.ARROW), "Permission to let shot projectiles", "interact with blocks (e.g. arrow on button)"));
    public static ClaimPermission TRAMPLE = register(new ClaimPermission("TRAMPLE", () -> new ItemStack(Items.FARMLAND), "Permission to enable block trampling", "(farmland, turtle eggs)"));
    public static ClaimPermission PORTAL = register(new ClaimPermission("PORTAL", () -> new ItemStack(Items.OBSIDIAN), "Permission to use nether portals"));
    public static ClaimPermission RAID = register(new ClaimPermission("RAID", () -> Raid.getOminousBanner(), "Permission to trigger raids in claim.", "Wont prevent raids (just) outside"));
    public static ClaimPermission BOAT = register(new ClaimPermission("BOAT", () -> new ItemStack(Items.OAK_BOAT), "Permission to sit in boats"));
    public static ClaimPermission MINECART = register(new ClaimPermission("MINECART", () -> new ItemStack(Items.MINECART), "Permission to sit in minecarts"));
    public static ClaimPermission BUCKET = register(new ClaimPermission("BUCKET", () -> new ItemStack(Items.BUCKET), "Permission to take liquids with buckets"));
    public static ClaimPermission ENDERPEARL = register(new ClaimPermission("ENDERPEARL", () -> new ItemStack(Items.ENDER_PEARL), "Permission to use enderpearls"));
    public static ClaimPermission ANIMALINTERACT = register(new ClaimPermission("ANIMALINTERACT", () -> new ItemStack(Items.CHICKEN_SPAWN_EGG), "Permission to interact with animals (e.g. shearing sheeps)"));
    public static ClaimPermission HURTANIMAL = register(new ClaimPermission("HURTANIMAL", () -> new ItemStack(Items.BEEF), "Permission to hurt animals"));
    public static ClaimPermission XP = register(new ClaimPermission("XP", () -> new ItemStack(Items.EXPERIENCE_BOTTLE), "Permission to pick up xp orbs"));
    public static ClaimPermission TRADING = register(new ClaimPermission("TRADING", () -> new ItemStack(Items.EMERALD), "Permission to trade with villagers"));
    public static ClaimPermission ARMORSTAND = register(new ClaimPermission("ARMORSTAND", () -> new ItemStack(Items.ARMOR_STAND), "Permission to interact with armor stands"));
    public static ClaimPermission BREAKNONLIVING = register(new ClaimPermission("BREAKNONLIVING", () -> new ItemStack(Items.COMMAND_BLOCK_MINECART), "Permission to break things like minecarts or armor stands"));
    public static ClaimPermission HURTPLAYER = global(new ClaimPermission("HURTPLAYER", () -> new ItemStack(Items.DIAMOND_SWORD), "Permission to hurt other players"));
    public static ClaimPermission EXPLOSIONS = global(new ClaimPermission("EXPLOSIONS", () -> new ItemStack(Items.TNT), "Toggle explosions in claim"));
    public static ClaimPermission WITHER = global(new ClaimPermission("WITHER", () -> new ItemStack(Items.WITHER_SKELETON_SKULL), "Toggle wither breaking blocks in claim"));
    public static ClaimPermission FIRESPREAD = global(new ClaimPermission("FIRESPREAD", () -> new ItemStack(Items.BLAZE_POWDER), "Toggle firespread in claim"));
    public static ClaimPermission MOBSPAWN = global(new ClaimPermission("MOBSPAWN", () -> new ItemStack(Items.ZOMBIE_SPAWN_EGG), "Prevent mobspawn in claim"));

    private static ClaimPermission register(ClaimPermission perm) {
        if (locked) {
            throw new IllegalStateException("Registering permissions is locked");
        }
        permissions.put(perm.id, perm);
        return perm;
    }

    public static ClaimPermission global(ClaimPermission perm) {
        if (locked) {
            throw new IllegalStateException("Registering permissions is locked");
        }
        globalPermissions.put(perm.id, perm);
        return register(perm);
    }

    public static void lock() {
        locked = true;
    }

    public static ClaimPermission get(String id) {
        if (!permissions.containsKey(id))
            throw new NullPointerException("No such permission registered");
        return permissions.get(id);
    }

    public static List<ClaimPermission> getPerms() {
        return Lists.newArrayList(permissions.values());
    }

    public static Collection<ClaimPermission> globalPerms() {
        return globalPermissions.values();
    }

    public static ClaimPermission registerBreakPerm(ClaimPermission perm, Identifier... affectedBlocks) {
        ClaimPermission reg = register(perm);
        for (Identifier blocks : affectedBlocks)
            breakBlocks.put(blocks, perm);
        return reg;
    }

    public static ClaimPermission registerBlockInteract(ClaimPermission perm, Identifier... affectedBlocks) {
        ClaimPermission reg = register(perm);
        for (Identifier blocks : affectedBlocks)
            interactBlocks.put(blocks, perm);
        return reg;
    }

    public static ClaimPermission registerItemUse(ClaimPermission perm, Identifier... affectedBlocks) {
        ClaimPermission reg = register(perm);
        for (Identifier blocks : affectedBlocks)
            items.put(blocks, perm);
        return reg;
    }
}