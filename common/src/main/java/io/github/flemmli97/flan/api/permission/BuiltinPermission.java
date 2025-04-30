package io.github.flemmli97.flan.api.permission;

import io.github.flemmli97.flan.Flan;
import io.github.flemmli97.flan.platform.CrossPlatformStuff;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.raid.Raid;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;

/**
 * IDs of all permissions provided by default
 */
public class BuiltinPermission {

    /**
     * For datagen. Do not use!
     */
    public static final Map<ResourceLocation, Function<HolderLookup.Provider, ClaimPermission.Builder>> DATAGEN_DATA = new LinkedHashMap<>();
    /**
     * Mappings to migrate old permission to the new ones
     */
    private static final HashMap<String, ResourceLocation> LEGACY_MIGRATION = new HashMap<>();
    public static int order = 0;

    public static ResourceLocation EDITCLAIM = register("edit_claim", new ItemStack(Items.GOLDEN_HOE), "Gives permission to edit (resize, delete...) the claim");
    public static ResourceLocation EDITPERMS = register("edit_perms", new ItemStack(Items.COMMAND_BLOCK), "Gives permission to change the claims permissions");
    public static ResourceLocation EDITPOTIONS = register("edit_potions", new ItemStack(Items.POTION), "Gives permission to edit the claims potion effect");
    public static ResourceLocation BREAK = register("break", new ItemStack(Items.DIAMOND_PICKAXE), "Permission to break blocks in the claim");
    public static ResourceLocation PLACE = register("place", new ItemStack(Items.GRASS_BLOCK), "Permission to place blocks in the claim");
    public static ResourceLocation OPENCONTAINER = register("open_container", new ItemStack(Items.CHEST), "Permission to open containers", "(chest, furnace etc.)");
    public static ResourceLocation INTERACTBLOCK = register("interact_block", new ItemStack(Items.GOLD_NUGGET), "Generic permission for block interaction.", "Fallback to OPENCONTAINER", "Gets used for all blocks OPENCONTAINER doesn't check for");
    public static ResourceLocation INTERACTSIGN = register("interact_sign", new ItemStack(Items.BIRCH_SIGN), "Permission to edit signs (e.g. dyeing them)");
    public static ResourceLocation ANVIL = register("anvil", new ItemStack(Items.ANVIL), "Permission to use anvils");
    public static ResourceLocation BED = register("bed", new ItemStack(Items.RED_BED), "Permission to use beds");
    public static ResourceLocation BEACON = register("beacon", new ItemStack(Items.BEACON), "Permission to use beacons");
    public static ResourceLocation DOOR = register("door", new ItemStack(Items.OAK_DOOR), "Permission to use doors");
    public static ResourceLocation FENCEGATE = register("fence_gate", new ItemStack(Items.OAK_FENCE_GATE), "Permission to use fence gates");
    public static ResourceLocation TRAPDOOR = register("trapdoor", new ItemStack(Items.OAK_TRAPDOOR), "Permission to use trapdoors");
    public static ResourceLocation BUTTONLEVER = register("button_lever", new ItemStack(Items.LEVER), "Permission to trigger levers and buttons");
    public static ResourceLocation PRESSUREPLATE = register("pressure_plate", new ItemStack(Items.STONE_PRESSURE_PLATE), "Permission to trigger pressure plates");
    public static ResourceLocation NOTEBLOCK = register("noteblock", new ItemStack(Items.NOTE_BLOCK), "Permission to change noteblocks");
    public static ResourceLocation REDSTONE = register("redstone", new ItemStack(Items.REDSTONE), "Permission to change redstone components");
    public static ResourceLocation JUKEBOX = register("jukebox", new ItemStack(Items.JUKEBOX), "Permission to insert/take music discs");
    public static ResourceLocation ENDERCHEST = register("enderchest", new ItemStack(Items.ENDER_CHEST), true, "Permission to use enderchests");
    public static ResourceLocation ENCHANTMENTTABLE = register("enchantment", new ItemStack(Items.ENCHANTING_TABLE), true, "Permission to use enchanting tables");
    public static ResourceLocation ITEMFRAMEROTATE = register("itemframe_rotate", new ItemStack(Items.ITEM_FRAME), "Permission to rotate items in item frames");
    public static ResourceLocation LECTERNTAKE = register("lectern_take", new ItemStack(Items.LECTERN), "Permission to change books in a lectern");
    public static ResourceLocation ENDCRYSTALPLACE = register("endcrystal_place", new ItemStack(Items.END_CRYSTAL), "Permission to place end crystals");
    public static ResourceLocation TARGETBLOCK = register("target_block", new ItemStack(Items.TARGET), "Permission to trigger target blocks");
    public static ResourceLocation PROJECTILES = register("projectiles", new ItemStack(Items.ARROW), "Permission to let shot projectiles", "interact with blocks (e.g. arrow on button)");
    public static ResourceLocation TRAMPLE = register("trample", new ItemStack(Items.FARMLAND), "Permission to enable block trampling", "(farmland, turtle eggs)");
    public static ResourceLocation PORTAL = register("portal", new ItemStack(Items.OBSIDIAN), true, "Permission to use nether portals");
    public static ResourceLocation RAID = register("raid", holder -> Raid.getLeaderBannerInstance(holder.lookupOrThrow(Registries.BANNER_PATTERN)), false, false, "Permission to trigger raids in claim.", "Wont prevent raids (just) outside");
    public static ResourceLocation BOAT = register("boat", new ItemStack(Items.OAK_BOAT), "Permission to use boats");
    public static ResourceLocation MINECART = register("minecart", new ItemStack(Items.MINECART), "Permission to sit in minecarts");
    public static ResourceLocation BUCKET = register("bucket", new ItemStack(Items.BUCKET), "Permission to take liquids with buckets");
    public static ResourceLocation ENDERPEARL = register("ender_pearl", new ItemStack(Items.ENDER_PEARL), "Permission to use enderpearls");
    public static ResourceLocation CHORUSFRUIT = register("chorus_fruit", new ItemStack(Items.CHORUS_FRUIT), "Permission to eat chorus fruits");
    public static ResourceLocation ANIMALINTERACT = register("animal_interact", new ItemStack(Items.CHICKEN_SPAWN_EGG), "Permission to interact with animals", "(e.g. shearing sheeps)");
    public static ResourceLocation HURTANIMAL = register("hurt_animal", new ItemStack(Items.BEEF), "Permission to hurt animals");
    public static ResourceLocation HURTNAMED = register("hurt_named", new ItemStack(Items.NAME_TAG), false, "Permission to hurt named mobs");
    public static ResourceLocation XP = register("xp", new ItemStack(Items.EXPERIENCE_BOTTLE), "Permission to pick up xp orbs");
    public static ResourceLocation TRADING = register("trading", new ItemStack(Items.EMERALD), "Permission to trade with villagers");
    public static ResourceLocation ARMORSTAND = register("armorstand", new ItemStack(Items.ARMOR_STAND), "Permission to interact with armor stands");
    public static ResourceLocation BREAKNONLIVING = register("break_non_living", new ItemStack(Items.COMMAND_BLOCK_MINECART), "Permission to break things like minecarts or armor stands");
    public static ResourceLocation DROP = register("drop", new ItemStack(Items.BOWL), true, "Allow the drop of items");
    public static ResourceLocation PICKUP = register("pickup", new ItemStack(Items.BRICK), true, "Allow the pickup of items");
    public static ResourceLocation ALLOW_FLIGHT = register("flight", new ItemStack(Items.IRON_BLOCK), true, "Allows all non creative flight", "Does not grant flight!");
    public static ResourceLocation MAY_FLIGHT = register("may_flight", new ItemStack(Items.FEATHER), false, "Allows player to fly in this claim.", "Flight permission needs to be true!");
    public static ResourceLocation CANSTAY = register("can_stay", new ItemStack(Items.PAPER), true, "Allow players to enter your claim");
    public static ResourceLocation TELEPORT = register("teleport", new ItemStack(Items.END_PORTAL_FRAME), false, "Allow player to teleport to your claim home position");
    public static ResourceLocation NOHUNGER = register("no_hunger", new ItemStack(Items.COOKED_BEEF), false, "Disable hunger");
    public static ResourceLocation CLAIMMESSAGE = register("claim_message", new ItemStack(Items.OAK_SIGN), false, "Permission to edit the enter/leave message");
    public static ResourceLocation ARCHAEOLOGY = register("archeology", new ItemStack(Items.BRUSH), false, false, "Allow players to brush blocks in this claim");
    public static ResourceLocation WIND_CHARGE = register("wind_charge", new ItemStack(Items.WIND_CHARGE), "Permission to use wind charges");

    public static ResourceLocation HURTPLAYER = register("hurt_player", new ItemStack(Items.DIAMOND_SWORD), false, true, "Permission to hurt other players");
    public static ResourceLocation EXPLOSIONS = register("explosions", new ItemStack(Items.TNT), false, true, "Toggle explosions in claim");
    public static ResourceLocation WITHER = register("wither", new ItemStack(Items.WITHER_SKELETON_SKULL), false, true, "Toggle wither breaking blocks in claim");
    public static ResourceLocation ENDERMAN = register("enderman", new ItemStack(Items.ENDER_EYE), true, true, "Allow enderman picking and placing blocks");
    public static ResourceLocation SNOWGOLEM = register("snow_golem", new ItemStack(Items.SNOW_BLOCK), true, true, "Allow snowgolems snowlayer");
    public static ResourceLocation FIRESPREAD = register("fire_spread", new ItemStack(Items.BLAZE_POWDER), false, true, "Toggle firespread in claim");
    public static ResourceLocation WATERBORDER = register("water_border", new ItemStack(Items.WATER_BUCKET), false, true, "Toggle water crossing claim borders");
    public static ResourceLocation PISTONBORDER = register("piston_border", new ItemStack(Items.PISTON), false, true, "Toggle piston pull/push across claim borders");
    public static ResourceLocation MOBSPAWN = register("mob_spawn", new ItemStack(Items.ZOMBIE_SPAWN_EGG), false, true, "Prevent hostile mobspawn in claim");
    public static ResourceLocation ANIMALSPAWN = register("animal_spawn", new ItemStack(Items.PIG_SPAWN_EGG), false, true, "Prevent other spawn in claim");
    public static ResourceLocation LIGHTNING = register("lightning", new ItemStack(Items.TRIDENT), false, true, "Allow lightning to affect claims", "e.g. set blocks on fire", "or affect animals (mobs are excluded)");
    public static ResourceLocation LOCKITEMS = register("lock_items", new ItemStack(Items.FIREWORK_STAR), true, true, "If items should be locked on death");
    public static ResourceLocation FAKEPLAYER = register("fake_player", new ItemStack(Items.CARROT_ON_A_STICK), false, true, "Allow fakeplayers to interact in this claim", "Some mods fakeplayer has the users uuid", "For those mods this permission is not needed");
    public static ResourceLocation PLAYERMOBSPAWN = register("player_mob_spawn", new ItemStack(Items.WARDEN_SPAWN_EGG), false, true, "Permission for affected players to spawn mobs with interactions", "E.g. wardens, or endermites with enderpearls");
    public static ResourceLocation SCULK = register("sculk", new ItemStack(Items.SCULK_SENSOR), false, true, "Permission for sculk sensors.", "Shriekers are handled under PLAYERMOBSPAWN");

    private static ResourceLocation register(String id, ItemStack item, String... description) {
        return register(id, item, false, description);
    }

    private static ResourceLocation register(String id, ItemStack item, boolean defaultVal, String... description) {
        return register(id, item, defaultVal, false, description);
    }

    private static ResourceLocation register(String key, ItemStack item, boolean defaultVal, boolean global, String... description) {
        return register(key, h -> item, defaultVal, global, description);
    }

    private static ResourceLocation register(String key, Function<HolderLookup.Provider, ItemStack> item, boolean defaultVal, boolean global, String... description) {
        ResourceLocation id = ResourceLocation.fromNamespaceAndPath(Flan.MODID, key);
        if (CrossPlatformStuff.INSTANCE.isDataGen()) {
            DATAGEN_DATA.put(id, holder -> new ClaimPermission.Builder(item.apply(holder), defaultVal, global, order++, List.of(description)));
        }
        LEGACY_MIGRATION.put(key.replace("_", "").toUpperCase(Locale.ROOT), id);
        return id;
    }

    public static void registerMapping(String key, ResourceLocation newId) {
        if (LEGACY_MIGRATION.containsKey(key)) {
            throw new IllegalArgumentException("A mapping with key " + key + " is already registered!");
        }
        LEGACY_MIGRATION.put(key, newId);
    }

    public static ResourceLocation tryLegacy(String key) {
        return LEGACY_MIGRATION.getOrDefault(key, ResourceLocation.parse(key.toLowerCase(Locale.ROOT)));
    }
}
