package com.flemmli97.flan.claim;

import com.google.common.collect.Maps;
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
import net.minecraft.block.FarmlandBlock;
import net.minecraft.block.FenceGateBlock;
import net.minecraft.block.JukeboxBlock;
import net.minecraft.block.LecternBlock;
import net.minecraft.block.LeverBlock;
import net.minecraft.block.NetherPortalBlock;
import net.minecraft.block.NoteBlock;
import net.minecraft.block.RedstoneWireBlock;
import net.minecraft.block.TargetBlock;
import net.minecraft.block.TntBlock;
import net.minecraft.block.TrapdoorBlock;
import net.minecraft.block.TurtleEggBlock;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.registry.Registry;

import java.util.Map;

public class BlockToPermissionMap {

    private static final Map<Block, EnumPermission> blockToPermission = Maps.newHashMap();

    public static void reload(MinecraftServer server) {
        blockToPermission.clear();
        for (Block block : Registry.BLOCK) {
            if (block instanceof AnvilBlock)
                blockToPermission.put(block, EnumPermission.ANVIL);
            if (block instanceof BedBlock)
                blockToPermission.put(block, EnumPermission.BED);
            if (block instanceof BeaconBlock)
                blockToPermission.put(block, EnumPermission.BEACON);
            if (block instanceof DoorBlock)
                blockToPermission.put(block, EnumPermission.DOOR);
            if (block instanceof FenceGateBlock)
                blockToPermission.put(block, EnumPermission.FENCEGATE);
            if (block instanceof TrapdoorBlock)
                blockToPermission.put(block, EnumPermission.TRAPDOOR);
            if (block instanceof LeverBlock || block instanceof AbstractButtonBlock)
                blockToPermission.put(block, EnumPermission.BUTTONLEVER);
            if (block instanceof NoteBlock)
                blockToPermission.put(block, EnumPermission.NOTEBLOCK);
            if (block instanceof AbstractRedstoneGateBlock || block instanceof RedstoneWireBlock || block instanceof DaylightDetectorBlock)
                blockToPermission.put(block, EnumPermission.REDSTONE);
            if (block instanceof JukeboxBlock)
                blockToPermission.put(block, EnumPermission.JUKEBOX);
            if (block instanceof AbstractPressurePlateBlock)
                blockToPermission.put(block, EnumPermission.PRESSUREPLATE);
            if (block instanceof NetherPortalBlock)
                blockToPermission.put(block, EnumPermission.PORTAL);
            if (block instanceof TurtleEggBlock || block instanceof FarmlandBlock)
                blockToPermission.put(block, EnumPermission.TRAMPLE);
            if (block instanceof TargetBlock)
                blockToPermission.put(block, EnumPermission.TARGETBLOCK);
            if (block instanceof BellBlock || block instanceof CampfireBlock
                    || block instanceof TntBlock || block instanceof ChorusFlowerBlock)
                blockToPermission.put(block, EnumPermission.PROJECTILES);
        }
    }

    public static EnumPermission getFromBlock(Block block) {
        return blockToPermission.get(block);
    }
}
