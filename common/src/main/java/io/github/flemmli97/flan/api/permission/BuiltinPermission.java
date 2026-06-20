package io.github.flemmli97.flan.api.permission;

import io.github.flemmli97.flan.Flan;
import io.github.flemmli97.flan.platform.CrossPlatformStuff;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.raid.Raid;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.Items;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * IDs of all permissions provided by default
 */
public class BuiltinPermission {

    /**
     * For datagen. Do not use!
     */
    public static final Map<Identifier, Function<HolderLookup.Provider, ClaimPermission.Builder>> DATAGEN_DATA = new LinkedHashMap<>();
    /**
     * Mappings to migrate old permission to the new ones
     */
    private static final HashMap<String, Identifier> LEGACY_MIGRATION = new HashMap<>();
    public static int order = 0;

    public static final Identifier EDITCLAIM = register("edit_claim", new ItemStackTemplate(Items.GOLDEN_HOE), "Gives permission to edit (resize, delete...) the claim");
    public static final Identifier EDITPERMS = register("edit_perms", new ItemStackTemplate(Items.COMMAND_BLOCK), "Gives permission to change the claims permissions");
    public static final Identifier EDITPOTIONS = register("edit_potions", new ItemStackTemplate(Items.POTION), "Gives permission to edit the claims potion effect");
    public static final Identifier BREAK = register("break", new ItemStackTemplate(Items.DIAMOND_PICKAXE), "Permission to break blocks in the claim");
    public static final Identifier PLACE = register("place", new ItemStackTemplate(Items.GRASS_BLOCK), "Permission to place blocks in the claim");
    public static final Identifier OPENCONTAINER = register("open_container", new ItemStackTemplate(Items.CHEST), "Permission to open containers", "(chest, furnace etc.)");
    public static final Identifier INTERACTBLOCK = register("interact_block", new ItemStackTemplate(Items.GOLD_NUGGET), "Generic permission for block interaction.", "Fallback to OPENCONTAINER", "Gets used for all blocks OPENCONTAINER doesn't check for");
    public static final Identifier INTERACTSIGN = register("interact_sign", new ItemStackTemplate(Items.BIRCH_SIGN), "Permission to edit signs (e.g. dyeing them)");
    public static final Identifier ANVIL = register("anvil", new ItemStackTemplate(Items.ANVIL), "Permission to use anvils");
    public static final Identifier BED = register("bed", new ItemStackTemplate(Items.BED.red()), "Permission to use beds");
    public static final Identifier BEACON = register("beacon", new ItemStackTemplate(Items.BEACON), "Permission to use beacons");
    public static final Identifier DOOR = register("door", new ItemStackTemplate(Items.OAK_DOOR), "Permission to use doors");
    public static final Identifier FENCEGATE = register("fence_gate", new ItemStackTemplate(Items.OAK_FENCE_GATE), "Permission to use fence gates");
    public static final Identifier TRAPDOOR = register("trapdoor", new ItemStackTemplate(Items.OAK_TRAPDOOR), "Permission to use trapdoors");
    public static final Identifier BUTTONLEVER = register("button_lever", new ItemStackTemplate(Items.LEVER), "Permission to trigger levers and buttons");
    public static final Identifier PRESSUREPLATE = register("pressure_plate", new ItemStackTemplate(Items.STONE_PRESSURE_PLATE), "Permission to trigger pressure plates");
    public static final Identifier NOTEBLOCK = register("noteblock", new ItemStackTemplate(Items.NOTE_BLOCK), "Permission to change noteblocks");
    public static final Identifier REDSTONE = register("redstone", new ItemStackTemplate(Items.REDSTONE), "Permission to change redstone components");
    public static final Identifier JUKEBOX = register("jukebox", new ItemStackTemplate(Items.JUKEBOX), "Permission to insert/take music discs");
    public static final Identifier ENDERCHEST = register("enderchest", new ItemStackTemplate(Items.ENDER_CHEST), true, "Permission to use enderchests");
    public static final Identifier ENCHANTMENTTABLE = register("enchantment", new ItemStackTemplate(Items.ENCHANTING_TABLE), true, "Permission to use enchanting tables");
    public static final Identifier ITEMFRAMEROTATE = register("itemframe_rotate", new ItemStackTemplate(Items.ITEM_FRAME), "Permission to rotate items in item frames");
    public static final Identifier LECTERNTAKE = register("lectern_take", new ItemStackTemplate(Items.LECTERN), "Permission to change books in a lectern");
    public static final Identifier ENDCRYSTALPLACE = register("endcrystal_place", new ItemStackTemplate(Items.END_CRYSTAL), "Permission to place end crystals");
    public static final Identifier TARGETBLOCK = register("target_block", new ItemStackTemplate(Items.TARGET), "Permission to trigger target blocks");
    public static final Identifier PROJECTILES = register("projectiles", new ItemStackTemplate(Items.ARROW), "Permission to let shot projectiles", "interact with blocks (e.g. arrow on button)");
    public static final Identifier TRAMPLE = register("trample", new ItemStackTemplate(Items.FARMLAND), "Permission to enable block trampling", "(farmland, turtle eggs)");
    public static final Identifier PORTAL = register("portal", new ItemStackTemplate(Items.OBSIDIAN), true, "Permission to use nether portals");
    public static final Identifier RAID = register("raid", holder -> Raid.getOminousBannerTemplate(holder.lookupOrThrow(Registries.BANNER_PATTERN)), false, false, "Permission to trigger raids in claim.", "Wont prevent raids (just) outside");
    public static final Identifier BOAT = register("boat", new ItemStackTemplate(Items.OAK_BOAT), "Permission to use boats");
    public static final Identifier MINECART = register("minecart", new ItemStackTemplate(Items.MINECART), "Permission to sit in minecarts");
    public static final Identifier BUCKET = register("bucket", new ItemStackTemplate(Items.BUCKET), "Permission to take liquids with buckets");
    public static final Identifier ENDERPEARL = register("ender_pearl", new ItemStackTemplate(Items.ENDER_PEARL), "Permission to use enderpearls");
    public static final Identifier CHORUSFRUIT = register("chorus_fruit", new ItemStackTemplate(Items.CHORUS_FRUIT), "Permission to eat chorus fruits");
    public static final Identifier ANIMALINTERACT = register("animal_interact", new ItemStackTemplate(Items.CHICKEN_SPAWN_EGG), "Permission to interact with animals", "(e.g. shearing sheeps)");
    public static final Identifier HURTANIMAL = register("hurt_animal", new ItemStackTemplate(Items.BEEF), "Permission to hurt animals");
    public static final Identifier HURTNAMED = register("hurt_named", new ItemStackTemplate(Items.NAME_TAG), false, "Permission to hurt named mobs");
    public static final Identifier XP = register("xp", new ItemStackTemplate(Items.EXPERIENCE_BOTTLE), "Permission to pick up xp orbs");
    public static final Identifier TRADING = register("trading", new ItemStackTemplate(Items.EMERALD), "Permission to trade with villagers");
    public static final Identifier ARMORSTAND = register("armorstand", new ItemStackTemplate(Items.ARMOR_STAND), "Permission to interact with armor stands");
    public static final Identifier BREAKNONLIVING = register("break_non_living", new ItemStackTemplate(Items.COMMAND_BLOCK_MINECART), "Permission to break things like minecarts or armor stands");
    public static final Identifier DROP = register("drop", new ItemStackTemplate(Items.BOWL), true, "Allow the drop of items");
    public static final Identifier PICKUP = register("pickup", new ItemStackTemplate(Items.BRICK), true, "Allow the pickup of items");
    public static final Identifier ALLOW_FLIGHT = register("flight", new ItemStackTemplate(Items.IRON_BLOCK), true, "Allows all non creative flight", "Does not grant flight!");
    public static final Identifier MAY_FLIGHT = register("may_flight", (holder, order) -> new ClaimPermission.Builder(new ItemStackTemplate(Items.FEATHER), order, List.of("Allows player to fly in this claim.", "Flight permission needs to be true!")).globalVal(false).requireExplicitSet(true));
    public static final Identifier CANSTAY = register("can_stay", new ItemStackTemplate(Items.PAPER), true, "Allow players to enter your claim");
    public static final Identifier TELEPORT = register("teleport", new ItemStackTemplate(Items.END_PORTAL_FRAME), false, "Allow player to teleport to your claim home position");
    public static final Identifier NOHUNGER = register("no_hunger", (holder, order) -> new ClaimPermission.Builder(new ItemStackTemplate(Items.COOKED_BEEF), order, List.of("Disable hunger")).globalVal(false).requireExplicitSet(true));
    public static final Identifier CLAIMMESSAGE = register("claim_message", new ItemStackTemplate(Items.OAK_SIGN), false, "Permission to edit the enter/leave message");
    public static final Identifier ARCHAEOLOGY = register("archeology", new ItemStackTemplate(Items.BRUSH), false, false, "Allow players to brush blocks in this claim");
    public static final Identifier WIND_CHARGE = register("wind_charge", new ItemStackTemplate(Items.WIND_CHARGE), "Permission to use wind charges");

    public static final Identifier HURTPLAYER = register("hurt_player", new ItemStackTemplate(Items.DIAMOND_SWORD), false, true, "Permission to hurt other players");
    public static final Identifier EXPLOSIONS = register("explosions", new ItemStackTemplate(Items.TNT), false, true, "Toggle explosions in claim");
    public static final Identifier WITHER = register("wither", new ItemStackTemplate(Items.WITHER_SKELETON_SKULL), false, true, "Toggle wither breaking blocks in claim");
    public static final Identifier ENDERMAN = register("enderman", new ItemStackTemplate(Items.ENDER_EYE), true, true, "Allow enderman picking and placing blocks");
    public static final Identifier SNOWGOLEM = register("snow_golem", new ItemStackTemplate(Items.SNOW_BLOCK), true, true, "Allow snowgolems snowlayer");
    public static final Identifier FIRESPREAD = register("fire_spread", new ItemStackTemplate(Items.BLAZE_POWDER), false, true, "Toggle firespread in claim");
    public static final Identifier WATERBORDER = register("water_border", new ItemStackTemplate(Items.WATER_BUCKET), false, true, "Toggle water crossing claim borders");
    public static final Identifier PISTONBORDER = register("piston_border", new ItemStackTemplate(Items.PISTON), false, true, "Toggle piston pull/push across claim borders");
    public static final Identifier MONSTERSPAWN = register("disable_monster_spawn", (holder, order) -> new ClaimPermission.Builder(new ItemStackTemplate(Items.ZOMBIE_SPAWN_EGG), order, List.of("Prevent hostile mobspawn in claim")).global(true).globalVal(false));
    public static final Identifier MOBSPAWN = register("disable_mob_spawn", (holder, order) -> new ClaimPermission.Builder(new ItemStackTemplate(Items.PIG_SPAWN_EGG), order, List.of("Prevent other spawn in claim")).global(true).globalVal(false));
    public static final Identifier LIGHTNING = register("lightning", new ItemStackTemplate(Items.TRIDENT), false, true, "Allow lightning to affect claims", "e.g. set blocks on fire", "or affect animals (mobs are excluded)");
    public static final Identifier LOCKITEMS = register("lock_items", new ItemStackTemplate(Items.FIREWORK_STAR), true, true, "If items should be locked on death");
    public static final Identifier FAKEPLAYER = register("fake_player", new ItemStackTemplate(Items.CARROT_ON_A_STICK), false, true, "Allow fakeplayers to interact in this claim", "Some mods fakeplayer has the users uuid", "For those mods this permission is not needed");
    public static final Identifier PLAYERMOBSPAWN = register("player_mob_spawn", new ItemStackTemplate(Items.WARDEN_SPAWN_EGG), false, true, "Permission for affected players to spawn mobs with interactions", "E.g. wardens, or endermites with enderpearls");
    public static final Identifier VEHICLE_PASS = register("vehicle_pass", new ItemStackTemplate(Items.CHEST_MINECART), false, true, "Allows vehicles such as minecarts or boats to pass claim borders", "Prevents e.g. hopper minecarts from coming inside claims");
    public static final Identifier SCULK = register("sculk", new ItemStackTemplate(Items.SCULK_SENSOR), false, true, "Permission for sculk sensors.", "Shriekers are handled under PLAYERMOBSPAWN");

    static {
        registerMapping("flan:mob_spawn", MONSTERSPAWN);
        registerMapping("flan:animal_spawn", MOBSPAWN);
    }

    private static Identifier register(String id, ItemStackTemplate item, String... description) {
        return register(id, item, false, description);
    }

    private static Identifier register(String id, ItemStackTemplate item, boolean defaultVal, String... description) {
        return register(id, item, defaultVal, false, description);
    }

    private static Identifier register(String key, ItemStackTemplate item, boolean defaultVal, boolean global, String... description) {
        return register(key, h -> item, defaultVal, global, description);
    }

    private static Identifier register(String key, Function<HolderLookup.Provider, ItemStackTemplate> item, boolean defaultVal, boolean global, String... description) {
        return register(key, (holder, order) -> new ClaimPermission.Builder(item.apply(holder), order, List.of(description)).defaultVal(defaultVal).global(global));
    }

    private static Identifier register(String key, BiFunction<HolderLookup.Provider, Integer, ClaimPermission.Builder> builder) {
        Identifier id = Identifier.fromNamespaceAndPath(Flan.MODID, key);
        if (CrossPlatformStuff.INSTANCE.isDataGen()) {
            DATAGEN_DATA.put(id, holder -> builder.apply(holder, order++));
        }
        LEGACY_MIGRATION.put(key.replace("_", "").toUpperCase(Locale.ROOT), id);
        return id;
    }

    public static void registerMapping(String key, Identifier newId) {
        if (LEGACY_MIGRATION.containsKey(key)) {
            throw new IllegalArgumentException("A mapping with key " + key + " is already registered!");
        }
        LEGACY_MIGRATION.put(key, newId);
    }

    public static Identifier tryLegacy(String key) {
        return LEGACY_MIGRATION.getOrDefault(key, Identifier.parse(key.toLowerCase(Locale.ROOT)));
    }
}
