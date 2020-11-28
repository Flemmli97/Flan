package com.flemmli97.flan.api;

import com.flemmli97.flan.claim.Claim;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;

import java.util.function.Supplier;

public class ClaimPermission {

    private final Supplier<ItemStack> guiItem;
    public String desc;
    public final String id;
    public final ClaimTest test;

    public ClaimPermission(String id, Supplier<ItemStack> guiItem, String defaultDescription) {
        this(id, guiItem, defaultDescription, pass);
    }

    public ClaimPermission(String id, Supplier<ItemStack> guiItem, String defaultDescription, ClaimTest test) {
        this.id = id;
        this.guiItem = guiItem;
        this.desc = defaultDescription;
        this.test = test;
    }

    public ItemStack getItem() {
        return this.guiItem.get();
    }

    @Override
    public int hashCode() {
        return this.id.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj instanceof ClaimPermission) {
            return this.id.equals(((ClaimPermission) obj).id);
        }
        return false;
    }

    @Override
    public String toString() {
        return this.id;
    }

    public interface ClaimTest {
        PermissionFlag test(Claim claim, PlayerEntity player, BlockPos pos);
    }

    private static final ClaimTest pass = (claim, player, pos) -> PermissionFlag.PASS;

    public enum PermissionFlag {
        YES,
        NO,
        PASS
    }
}
