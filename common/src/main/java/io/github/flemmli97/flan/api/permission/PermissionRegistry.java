package io.github.flemmli97.flan.api.permission;

import io.github.flemmli97.flan.config.ConfigHandler;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.raid.Raid;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Register more permissions before ServerLifecycleEvents.SERVER_STARTING
 */
public class PermissionRegistry {

    private static final Map<String, ClaimPermission> permissions = new LinkedHashMap<>();
    private static final Map<String, ClaimPermission> globalPermissions = new LinkedHashMap<>();

    private static final Map<ResourceLocation, ClaimPermission> interactBlocks = new HashMap<>();
    private static final Map<ResourceLocation, ClaimPermission> breakBlocks = new HashMap<>();
    private static final Map<ResourceLocation, ClaimPermission> items = new HashMap<>();

    private static boolean locked;

    public static ClaimPermission EDITCLAIM = register(new ClaimPermission("EDITCLAIM", () -> new ItemStack(ConfigHandler.config.claimingItem), "Gives permission to edit (resize, delete...) the claim"));
    public static ClaimPermission EDITPERMS = register(new ClaimPermission("EDITPERMS", () -> new ItemStack(Items.COMMAND_BLOCK), "Gives permission to change the claims permissions"));
    public static ClaimPermission EDITPOTIONS = register(new ClaimPermission("EDITPOTIONS", () -> new ItemStack(Items.POTION), "Gives permission to edit the claims potion effect"));
    public static ClaimPermission BREAK = register(new ClaimPermission("BREAK", () -> new ItemStack(Items.DIAMOND_PICKAXE), "Permission to break blocks in the claim"));
    public static ClaimPermission PLACE = register(new ClaimPermission("PLACE", () -> new ItemStack(Items.GRASS_BLOCK), "Permission to place blocks in the claim"));
    public static ClaimPermission OPENCONTAINER = register(new ClaimPermission("OPENCONTAINER", () -> new ItemStack(Items.CHEST), "Permission to open containers", "(chest, furnace etc.)"));
    public static ClaimPermission INTERACTBLOCK = register(new ClaimPermission("INTERACTBLOCK", () -> new ItemStack(Items.GOLD_NUGGET), "Generic permission for block interaction.", "Fallback to OPENCONTAINER", "Gets used for all blocks OPENCONTAINER doesn't check for"));
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
    public static ClaimPermission ENDERCHEST = register(new ClaimPermission("ENDERCHEST", () -> new ItemStack(Items.ENDER_CHEST), true, "Permission to use enderchests"));
    public static ClaimPermission ENCHANTMENTTABLE = register(new ClaimPermission("ENCHANTMENT", () -> new ItemStack(Items.ENCHANTING_TABLE), true, "Permission to use enchanting tables"));
    public static ClaimPermission ITEMFRAMEROTATE = register(new ClaimPermission("ITEMFRAMEROTATE", () -> new ItemStack(Items.ITEM_FRAME), "Permission to rotate items in item frames"));
    public static ClaimPermission LECTERNTAKE = register(new ClaimPermission("LECTERNTAKE", () -> new ItemStack(Items.LECTERN), "Permission to change books in a lectern"));
    public static ClaimPermission ENDCRYSTALPLACE = register(new ClaimPermission("ENDCRYSTALPLACE", () -> new ItemStack(Items.END_CRYSTAL), "Permission to place end crystals"));
    public static ClaimPermission TARGETBLOCK = register(new ClaimPermission("TARGETBLOCK", () -> new ItemStack(Items.TARGET), "Permission to trigger target blocks"));
    public static ClaimPermission PROJECTILES = register(new ClaimPermission("PROJECTILES", () -> new ItemStack(Items.ARROW), "Permission to let shot projectiles", "interact with blocks (e.g. arrow on button)"));
    public static ClaimPermission TRAMPLE = register(new ClaimPermission("TRAMPLE", () -> new ItemStack(Items.FARMLAND), "Permission to enable block trampling", "(farmland, turtle eggs)"));
    public static ClaimPermission FROSTWALKER = register(new ClaimPermission("FROSTWALKER", () -> new ItemStack(Items.LEATHER_BOOTS), "Permission for frostwalker to activate"));
    public static ClaimPermission PORTAL = register(new ClaimPermission("PORTAL", () -> new ItemStack(Items.OBSIDIAN), true, "Permission to use nether portals"));
    public static ClaimPermission RAID = register(new ClaimPermission("RAID", Raid::getLeaderBannerInstance, "Permission to trigger raids in claim.", "Wont prevent raids (just) outside"));
    public static ClaimPermission BOAT = register(new ClaimPermission("BOAT", () -> new ItemStack(Items.OAK_BOAT), "Permission to use boats"));
    public static ClaimPermission MINECART = register(new ClaimPermission("MINECART", () -> new ItemStack(Items.MINECART), "Permission to sit in minecarts"));
    public static ClaimPermission BUCKET = register(new ClaimPermission("BUCKET", () -> new ItemStack(Items.BUCKET), "Permission to take liquids with buckets"));
    public static ClaimPermission ENDERPEARL = register(new ClaimPermission("ENDERPEARL", () -> new ItemStack(Items.ENDER_PEARL), "Permission to use enderpearls"));
    public static ClaimPermission CHORUSFRUIT = register(new ClaimPermission("CHORUSFRUIT", () -> new ItemStack(Items.CHORUS_FRUIT), "Permission to eat chorus fruits"));
    public static ClaimPermission ANIMALINTERACT = register(new ClaimPermission("ANIMALINTERACT", () -> new ItemStack(Items.CHICKEN_SPAWN_EGG), "Permission to interact with animals", "(e.g. shearing sheeps)"));
    public static ClaimPermission HURTANIMAL = register(new ClaimPermission("HURTANIMAL", () -> new ItemStack(Items.BEEF), "Permission to hurt animals"));
    public static ClaimPermission XP = register(new ClaimPermission("XP", () -> new ItemStack(Items.EXPERIENCE_BOTTLE), "Permission to pick up xp orbs"));
    public static ClaimPermission TRADING = register(new ClaimPermission("TRADING", () -> new ItemStack(Items.EMERALD), "Permission to trade with villagers"));
    public static ClaimPermission ARMORSTAND = register(new ClaimPermission("ARMORSTAND", () -> new ItemStack(Items.ARMOR_STAND), "Permission to interact with armor stands"));
    public static ClaimPermission BREAKNONLIVING = register(new ClaimPermission("BREAKNONLIVING", () -> new ItemStack(Items.COMMAND_BLOCK_MINECART), "Permission to break things like minecarts or armor stands"));
    public static ClaimPermission DROP = register(new ClaimPermission("DROP", () -> new ItemStack(Items.BOWL), true, "Allow the drop of items"));
    public static ClaimPermission PICKUP = register(new ClaimPermission("PICKUP", () -> new ItemStack(Items.BRICK), true, "Allow the pickup of items"));
    public static ClaimPermission FLIGHT = register(new ClaimPermission("FLIGHT", () -> new ItemStack(Items.FEATHER), true, "Allow non creative flight"));
    public static ClaimPermission CANSTAY = register(new ClaimPermission("CANSTAY", () -> new ItemStack(Items.PAPER), true, "Allow players to enter your claim"));
    public static ClaimPermission TELEPORT = register(new ClaimPermission("TELEPORT", () -> new ItemStack(Items.END_PORTAL_FRAME), false, "Allow player to teleport to your claim home position"));
    public static ClaimPermission NOHUNGER = register(new ClaimPermission("NOHUNGER", () -> new ItemStack(Items.COOKED_BEEF), false, "Disable hunger"));
    public static ClaimPermission CLAIMMESSAGE = register(new ClaimPermission("CLAIMMESSAGE", () -> new ItemStack(Items.OAK_SIGN), false, "Permission to edit the enter/leave message"));
    public static ClaimPermission PLAYERMOBSPAWN = register(new ClaimPermission("PLAYERMOBSPAWN", () -> new ItemStack(Items.WARDEN_SPAWN_EGG), false, "Permission for affected players to spawn mobs with interactions", "E.g. wardens, or endermites with enderpearls"));
    public static ClaimPermission SCULK = register(new ClaimPermission("SCULK", () -> new ItemStack(Items.SCULK_SENSOR), false, "Permission for sculk sensors.", "Shriekers are handled under PLAYERMOBSPAWN"));

    public static ClaimPermission HURTPLAYER = global(new ClaimPermission("HURTPLAYER", () -> new ItemStack(Items.DIAMOND_SWORD), "Permission to hurt other players"));
    public static ClaimPermission EXPLOSIONS = global(new ClaimPermission("EXPLOSIONS", () -> new ItemStack(Items.TNT), "Toggle explosions in claim"));
    public static ClaimPermission WITHER = global(new ClaimPermission("WITHER", () -> new ItemStack(Items.WITHER_SKELETON_SKULL), "Toggle wither breaking blocks in claim"));
    public static ClaimPermission ENDERMAN = global(new ClaimPermission("ENDERMAN", () -> new ItemStack(Items.ENDER_EYE), true, "Allow enderman picking and placing blocks"));
    public static ClaimPermission SNOWGOLEM = global(new ClaimPermission("SNOWGOLEM", () -> new ItemStack(Items.SNOW_BLOCK), true, "Allow snowgolems snowlayer"));
    public static ClaimPermission FIRESPREAD = global(new ClaimPermission("FIRESPREAD", () -> new ItemStack(Items.BLAZE_POWDER), "Toggle firespread in claim"));
    public static ClaimPermission WATERBORDER = global(new ClaimPermission("WATERBORDER", () -> new ItemStack(Items.WATER_BUCKET), "Toggle water crossing claim borders"));
    public static ClaimPermission PISTONBORDER = global(new ClaimPermission("PISTONBORDER", () -> new ItemStack(Items.PISTON), "Toggle piston pull/push across claim borders"));
    public static ClaimPermission MOBSPAWN = global(new ClaimPermission("MOBSPAWN", () -> new ItemStack(Items.ZOMBIE_SPAWN_EGG), "Prevent hostile mobspawn in claim"));
    public static ClaimPermission ANIMALSPAWN = global(new ClaimPermission("ANIMALSPAWN", () -> new ItemStack(Items.PIG_SPAWN_EGG), "Prevent other spawn in claim"));
    public static ClaimPermission LIGHTNING = global(new ClaimPermission("LIGHTNING", () -> new ItemStack(Items.TRIDENT), "Allow lightning to affect claims", "e.g. set blocks on fire", "or affect animals (mobs are excluded)"));
    public static ClaimPermission LOCKITEMS = global(new ClaimPermission("LOCKITEMS", () -> new ItemStack(Items.FIREWORK_STAR), true, "If items should be locked on death"));
    public static ClaimPermission FAKEPLAYER = global(new ClaimPermission("FAKEPLAYER", () -> new ItemStack(Items.CARROT_ON_A_STICK), false, "Allow fakeplayers to interact in this claim", "Some mods fakeplayer has the users uuid", "For those mods this permission is not needed"));

    public static ClaimPermission ARCHAEOLOGY = global(new ClaimPermission("ARCHAEOLOGY",()->new ItemStack(Items.BRUSH), false, "Allow players to brush blocks in this claim"));
    public static ClaimPermission register(ClaimPermission perm) {
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
            throw new NullPointerException("No such permission " + id + " registered");
        return permissions.get(id);
    }

    public static List<ClaimPermission> getPerms() {
        return new ArrayList<>(permissions.values());
    }

    public static Collection<ClaimPermission> globalPerms() {
        return globalPermissions.values();
    }

    public static ClaimPermission registerBreakPerm(ClaimPermission perm, ResourceLocation... affectedBlocks) {
        ClaimPermission reg = register(perm);
        for (ResourceLocation blocks : affectedBlocks)
            breakBlocks.put(blocks, perm);
        return reg;
    }

    public static ClaimPermission registerBlockInteract(ClaimPermission perm, ResourceLocation... affectedBlocks) {
        ClaimPermission reg = register(perm);
        for (ResourceLocation blocks : affectedBlocks)
            interactBlocks.put(blocks, perm);
        return reg;
    }

    public static ClaimPermission registerItemUse(ClaimPermission perm, ResourceLocation... affectedBlocks) {
        ClaimPermission reg = register(perm);
        for (ResourceLocation blocks : affectedBlocks)
            items.put(blocks, perm);
        return reg;
    }
}