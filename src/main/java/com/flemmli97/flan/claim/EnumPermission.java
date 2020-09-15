package com.flemmli97.flan.claim;

import com.flemmli97.flan.config.ConfigHandler;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.village.raid.Raid;

import java.util.EnumSet;

public enum EnumPermission {

    EDITCLAIM(ConfigHandler.config.claimingItem, "Gives permission to edit (resize, delete...) the claim"),
    EDITPERMS(Items.COMMAND_BLOCK, "Gives permission to change the claims permissions"),
    BREAK(Items.DIAMOND_PICKAXE, "Permission to break blocks in the claim"),
    PLACE(Items.GRASS_BLOCK, "Permission to place blocks in the claim"),
    OPENCONTAINER(Items.CHEST, "Permission to open containers (chest, furnace etc.)"),
    ANVIL(Items.ANVIL, "Permission to use anvils"),
    BED(Items.RED_BED, "Permission to use beds"),
    BEACON(Items.BEACON, "Permission to use beacons"),
    DOOR(Items.OAK_DOOR, "Permission to use doors"),
    FENCEGATE(Items.OAK_FENCE_GATE, "Permission to use fence gates"),
    TRAPDOOR(Items.OAK_TRAPDOOR, "Permission to use trapdoors"),
    BUTTONLEVER(Items.LEVER, "Permission to trigger levers and buttons"),
    PRESSUREPLATE(Items.STONE_PRESSURE_PLATE, "Permission to trigger pressure plates"),
    NOTEBLOCK(Items.NOTE_BLOCK, "Permission to change noteblocks"),
    REDSTONE(Items.REDSTONE, "Permission to change redstone components"),
    JUKEBOX(Items.JUKEBOX, "Permission to insert/take music discs"),
    ITEMFRAMEROTATE(Items.ITEM_FRAME, "Permission to rotate items in item frames"),
    LECTERNTAKE(Items.LECTERN, "Permission to change books in a lectern"),
    ENDCRYSTALPLACE(Items.END_CRYSTAL, "Permission to place end crystals"),
    TARGETBLOCK(Items.TARGET, "Permission to trigger target blocks"),
    PROJECTILES(Items.ARROW, "Permission to let shot projectiles interact with blocks (e.g. arrow on button)"),
    TRAMPLE(Items.FARMLAND, "Permission to enable block trampling (farmland, turtle eggs)"),
    PORTAL(Items.OBSIDIAN, "Permission to use nether portals"),
    RAID(null, "Permission to trigger raids in claim. Wont prevent raids (just) outside"),
    BOAT(Items.OAK_BOAT, "Permission to sit in boats"),
    MINECART(Items.MINECART, "Permission to sit in minecarts"),
    BUCKET(Items.BUCKET, "Permission to take liquids with buckets"),
    ENDERPEARL(Items.ENDER_PEARL, "Permission to use enderpearls"),
    ANIMALINTERACT(Items.CHICKEN_SPAWN_EGG, "Permission to interact with animals (e.g. shearing sheeps)"),
    HURTANIMAL(Items.BEEF, "Permission to hurt animals"),
    XP(Items.EXPERIENCE_BOTTLE, "Permission to pick up xp orbs"),
    TRADING(Items.EMERALD, "Permission to trade with villagers"),
    ARMORSTAND(Items.ARMOR_STAND, "Permission to interact with armor stands"),
    BREAKNONLIVING(Items.COMMAND_BLOCK_MINECART, "Permission to break things like minecarts or armor stands"),
    HURTPLAYER(Items.DIAMOND_SWORD, "Permission to hurt other players"),
    EXPLOSIONS(Items.TNT, "Toggle explosions in claim"),
    WITHER(Items.WITHER_SKELETON_SKULL,"Toggle wither breaking blocks in claim"),
    FIRESPREAD(Items.BLAZE_POWDER, "Toggle firespread in claim");

    private final Item item;

    public String translation;

    private static final EnumSet<EnumPermission> alwaysGlobal = EnumSet.of(HURTPLAYER, EXPLOSIONS, WITHER, FIRESPREAD);

    EnumPermission(Item item, String translation) {
        this.item = item;
        this.translation = translation;
    }

    public ItemStack getItem() {
        if (this == RAID)
            return Raid.getOminousBanner();
        return new ItemStack(this.item);
    }

    public boolean isAlwaysGlobalPerm() {
        return alwaysGlobal.contains(this);
    }

    public static int alwaysGlobalLength() {
        return alwaysGlobal.size();
    }
}
