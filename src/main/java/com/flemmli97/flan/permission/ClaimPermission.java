package com.flemmli97.flan.permission;

import com.flemmli97.flan.claim.Claim;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;

import java.util.function.Supplier;

/**
 * Unused. might expand on this idea later
 */
public class ClaimPermission {

    public interface ClaimTest{
        boolean test(Claim claim, PlayerEntity player, BlockPos pos, PermissionFlag flag);
    }
    
    private static final ClaimTest alwaysTrue = (claim, player, pos, flag)->true;

    public enum PermissionFlag{
        YES,
        NO,
        PASS
    }

    private final Supplier<ItemStack> guiItem;
    public String desc;
    public final ClaimTest test;
    public final String id;
    
    public ClaimPermission(String id, Supplier<ItemStack> guiItem, String defaultDescription){
        this(id, guiItem, defaultDescription, alwaysTrue);
    }

    public ClaimPermission(String id, Supplier<ItemStack> guiItem, String defaultDescription, ClaimTest test){
        this.id = id;
        this.guiItem = guiItem;
        this.desc = defaultDescription;
        this.test = test;
    }

    public ItemStack getItem(){
        return this.guiItem.get();
    }
}
