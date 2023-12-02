package io.github.flemmli97.flan.forge.platform.integration.claiming;

import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.colony.permissions.Action;
import io.github.flemmli97.flan.Flan;
import io.github.flemmli97.flan.claim.Claim;
import io.github.flemmli97.flan.claim.ClaimStorage;
import io.github.flemmli97.flan.config.ConfigHandler;
import io.github.flemmli97.flan.platform.integration.claiming.FTBChunks;
import io.github.flemmli97.flan.platform.integration.claiming.OtherClaimingModCheck;
import io.github.flemmli97.flan.player.display.DisplayBox;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class OtherClaimingModCheckImpl implements OtherClaimingModCheck {

    public void findConflicts(Claim claim, Set<DisplayBox> set) {
        FTBChunks.findConflicts(claim, set);
        if (Flan.mineColonies && ConfigHandler.config.mineColoniesCheck) {
            ServerLevel level = claim.getWorld();
            int[] chunks = ClaimStorage.getChunkPos(claim);
            Map<IColony, List<ChunkPos>> map = new HashMap<>();
            for (int x = chunks[0]; x <= chunks[1]; x++)
                for (int z = chunks[2]; z <= chunks[3]; z++) {
                    IColony colony = IColonyManager.getInstance().getColonyByPosFromWorld(level, new BlockPos(x << 4, 0, z << 4));
                    if (colony == null)
                        continue;
                    if (!colony.getPermissions().hasPermission(colony.getPermissions().getRank(claim.getOwner()), Action.EDIT_PERMISSIONS)) {
                        map.computeIfAbsent(colony, c -> new ArrayList<>()).add(new ChunkPos(x, z));
                    }
                }
            map.forEach((colony, poss) -> poss.forEach(pos -> {
                int blockX = pos.x << 4;
                int blockZ = pos.z << 4;
                set.add(new DisplayBox(blockX, level.getMinBuildHeight(), blockZ, blockX + 15, level.getMaxBuildHeight(), blockZ + 15, () -> false, neighbors(pos, poss)));
            }));
        }
    }

    private static Direction[] neighbors(ChunkPos pos, List<ChunkPos> others) {
        List<Direction> dirs = new ArrayList<>();
        if (others.contains(new ChunkPos(pos.x, pos.z - 1)))
            dirs.add(Direction.NORTH);
        if (others.contains(new ChunkPos(pos.x, pos.z + 1)))
            dirs.add(Direction.SOUTH);
        if (others.contains(new ChunkPos(pos.x + 1, pos.z)))
            dirs.add(Direction.EAST);
        if (others.contains(new ChunkPos(pos.x - 1, pos.z)))
            dirs.add(Direction.WEST);
        return dirs.toArray(new Direction[0]);
    }
}
