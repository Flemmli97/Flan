package io.github.flemmli97.flan.fabric.platform.integration.claiming;

import com.jamieswhiteshirt.rtree3i.Box;
import draylar.goml.api.ClaimUtils;
import io.github.flemmli97.flan.Flan;
import io.github.flemmli97.flan.claim.Claim;
import io.github.flemmli97.flan.claim.ClaimBox;
import io.github.flemmli97.flan.config.ConfigHandler;
import io.github.flemmli97.flan.platform.integration.claiming.FTBChunks;
import io.github.flemmli97.flan.platform.integration.claiming.OtherClaimingModCheck;
import io.github.flemmli97.flan.player.display.DisplayBox;
import net.minecraft.core.BlockPos;

import java.util.Set;

public class OtherClaimingModCheckImpl implements OtherClaimingModCheck {

    @Override
    public void findConflicts(Claim claim, Set<DisplayBox> set) {
        FTBChunks.findConflicts(claim, set);
        if (Flan.gomlServer && ConfigHandler.CONFIG.gomlReservedCheck) {
            ClaimBox dim = claim.getDimensions();
            ClaimUtils.getClaimsInBox(claim.getLevel(), new BlockPos(dim.minX() - 1, dim.minY(), dim.minZ() - 1), new BlockPos(dim.maxX() + 1, dim.maxY(), dim.maxZ() + 1))
                    .forEach(e -> {
                        if (!e.getValue().getOwners().contains(claim.getOwner()) && !e.getValue().getTrusted().contains(claim.getOwner()))
                            set.add(convertBox(e.getValue()));
                    });
        }
    }

    private static DisplayBox convertBox(draylar.goml.api.Claim claim) {
        Box box = claim.getClaimBox().toBox();
        return new DisplayBox(box.x1(), box.y1(), box.z1(), box.x2(), box.y2(), box.z2(), claim::isDestroyed);
    }
}
