package io.github.flemmli97.flan.api.permission;

import io.github.flemmli97.flan.claim.Claim;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.function.Supplier;

public class ClaimPermission {

    private final Supplier<ItemStack> guiItem;
    public final String[] desc;
    public final String id;
    public final ClaimTest test;
    public final boolean defaultVal;

    public ClaimPermission(String id, Supplier<ItemStack> guiItem, String... defaultDescription) {
        this(id, guiItem, false, pass, defaultDescription);
    }

    public ClaimPermission(String id, Supplier<ItemStack> guiItem, boolean defaultVal, String... defaultDescription) {
        this(id, guiItem, defaultVal, pass, defaultDescription);
    }

    public ClaimPermission(String id, Supplier<ItemStack> guiItem, boolean defaultVal, ClaimTest test, String... defaultDescription) {
        this.id = id;
        this.guiItem = guiItem;
        this.desc = defaultDescription;
        this.test = test;
        this.defaultVal = defaultVal;
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
        PermissionFlag test(Claim claim, Player player, BlockPos pos);
    }

    private static final ClaimTest pass = (claim, player, pos) -> PermissionFlag.PASS;

    public enum PermissionFlag {
        YES,
        NO,
        PASS
    }
}
