package com.flemmli97.flan.claim;

import com.flemmli97.flan.api.ClaimPermission;
import com.flemmli97.flan.api.PermissionRegistry;
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

    private static final Map<Block, ClaimPermission> blockToPermission = Maps.newHashMap();

    public static void reload(MinecraftServer server) {
        blockToPermission.clear();
        for (Block block : Registry.BLOCK) {
            if (block instanceof AnvilBlock)
                blockToPermission.put(block, PermissionRegistry.ANVIL);
            if (block instanceof BedBlock)
                blockToPermission.put(block, PermissionRegistry.BED);
            if (block instanceof BeaconBlock)
                blockToPermission.put(block, PermissionRegistry.BEACON);
            if (block instanceof DoorBlock)
                blockToPermission.put(block, PermissionRegistry.DOOR);
            if (block instanceof FenceGateBlock)
                blockToPermission.put(block, PermissionRegistry.FENCEGATE);
            if (block instanceof TrapdoorBlock)
                blockToPermission.put(block, PermissionRegistry.TRAPDOOR);
            if (block instanceof LeverBlock || block instanceof AbstractButtonBlock)
                blockToPermission.put(block, PermissionRegistry.BUTTONLEVER);
            if (block instanceof NoteBlock)
                blockToPermission.put(block, PermissionRegistry.NOTEBLOCK);
            if (block instanceof AbstractRedstoneGateBlock || block instanceof RedstoneWireBlock || block instanceof DaylightDetectorBlock)
                blockToPermission.put(block, PermissionRegistry.REDSTONE);
            if (block instanceof JukeboxBlock)
                blockToPermission.put(block, PermissionRegistry.JUKEBOX);
            if (block instanceof AbstractPressurePlateBlock)
                blockToPermission.put(block, PermissionRegistry.PRESSUREPLATE);
            if (block instanceof NetherPortalBlock)
                blockToPermission.put(block, PermissionRegistry.PORTAL);
            if (block instanceof TurtleEggBlock || block instanceof FarmlandBlock)
                blockToPermission.put(block, PermissionRegistry.TRAMPLE);
            if (block instanceof TargetBlock)
                blockToPermission.put(block, PermissionRegistry.TARGETBLOCK);
            if (block instanceof BellBlock || block instanceof CampfireBlock
                    || block instanceof TntBlock || block instanceof ChorusFlowerBlock)
                blockToPermission.put(block, PermissionRegistry.PROJECTILES);
        }
    }

    public static ClaimPermission getFromBlock(Block block) {
        return blockToPermission.get(block);
    }
}
