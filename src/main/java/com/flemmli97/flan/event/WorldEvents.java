package com.flemmli97.flan.event;

import com.flemmli97.flan.claim.Claim;
import com.flemmli97.flan.claim.ClaimStorage;
import com.flemmli97.flan.claim.EnumPermission;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.List;

public class WorldEvents {

    public static void modifyExplosion(List<BlockPos> list, World world) {
        if (world.isClient)
            return;
        ClaimStorage storage = ClaimStorage.get((ServerWorld) world);
        list.removeIf(pos -> {
            Claim claim = storage.getClaimAt(pos);
            if (claim != null)
                return !claim.canInteract(null, EnumPermission.EXPLOSIONS, pos);
            return false;
        });
    }
}
