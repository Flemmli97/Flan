package io.github.flemmli97.flan.platform.integration.create;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.content.contraptions.AbstractContraptionEntity;
import io.github.flemmli97.flan.Flan;
import io.github.flemmli97.flan.api.permission.ClaimPermission;
import io.github.flemmli97.flan.api.permission.PermissionRegistry;
import io.github.flemmli97.flan.claim.Claim;
import io.github.flemmli97.flan.claim.ClaimStorage;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.vehicle.AbstractMinecart;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.Set;

public class CreateCompat {

    private static ClaimPermission CREATE;

    public static void init() {
        Flan.create = true;
        CREATE = PermissionRegistry.global(new ClaimPermission("CREATEMINECART", () -> new ItemStack(AllBlocks.CART_ASSEMBLER.get()), "Gives permission to allow minecart contraptions to pass through claim border.", "Note if this is disabled and your contraption goes out of the claim it can't go back in!"));
    }

    // Checks if a minecart with a contraption can cross a claims border
    public static boolean canMinecartPass(AbstractMinecart minecart) {
        if (!Flan.create || !(minecart.level() instanceof ServerLevel serverLevel))
            return true;
        if (minecart.getFirstPassenger() instanceof AbstractContraptionEntity contraption) {
            ClaimStorage storage = ClaimStorage.get(serverLevel);
            Vec3 delta = minecart.getDeltaMovement();
            AABB aabb = contraption.getContraption().bounds.expandTowards(delta);
            BlockPos pos = minecart.blockPosition();
            int rX = Mth.ceil(aabb.getXsize() * 0.5);
            int rZ = Mth.ceil(aabb.getZsize() * 0.5);

            // All claims different from the current one
            Set<Claim> claims = storage.getNearbyClaims(pos, rX, rZ);
            claims.remove(storage.getClaimAt(pos));
            if (claims.isEmpty())
                return true;
            // Check the east/west border of aabb
            if (delta.x != 0) {
                Iterable<BlockPos> eastWest;
                if (delta.x > 0) {
                    eastWest = BlockPos.betweenClosed(pos.getX() + rX, pos.getY(), pos.getZ() - rZ,
                            pos.getX() + rX, pos.getY(), pos.getZ() + rZ);
                } else {
                    eastWest = BlockPos.betweenClosed(pos.getX() - rX, pos.getY(), pos.getZ() - rZ,
                            pos.getX() - rX, pos.getY(), pos.getZ() + rZ);
                }
                for (BlockPos ipos : eastWest) {
                    for (Claim claim : claims) {
                        if (claim.insideClaim(ipos)) {
                            if (!claim.canInteract(null, CREATE, ipos, false)) {
                                return false;
                            }
                        }
                    }
                }
            }
            // Check the north/south border of aabb
            if (delta.z != 0) {
                Iterable<BlockPos> northSouth;
                if (delta.z > 0) {
                    northSouth = BlockPos.betweenClosed(pos.getX() - rX, pos.getY(), pos.getZ() + rZ,
                            pos.getX() + rX, pos.getY(), pos.getZ() + rZ);
                } else {
                    northSouth = BlockPos.betweenClosed(pos.getX() - rX, pos.getY(), pos.getZ() - rZ,
                            pos.getX() + rX, pos.getY(), pos.getZ() - rZ);
                }
                for (BlockPos ipos : northSouth) {
                    for (Claim claim : claims) {
                        if (claim.insideClaim(ipos)) {
                            if (!claim.canInteract(null, CREATE, ipos, false)) {
                                return false;
                            }
                        }
                    }
                }
            }
            return true;
        }
        return true;
    }
}
