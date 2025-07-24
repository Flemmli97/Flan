package io.github.flemmli97.flan.scoreboard;

import net.minecraft.world.scores.criteria.ObjectiveCriteria;

public class ClaimCriterias {

    public static final ObjectiveCriteria AMOUNT = ObjectiveCriteria.registerCustom("flan:total_claimblocks", true, ObjectiveCriteria.RenderType.INTEGER);
    public static final ObjectiveCriteria USED = ObjectiveCriteria.registerCustom("flan:used_claimblocks", true, ObjectiveCriteria.RenderType.INTEGER);
    public static final ObjectiveCriteria FREE = ObjectiveCriteria.registerCustom("flan:free_claimblocks", true, ObjectiveCriteria.RenderType.INTEGER);
    public static final ObjectiveCriteria CLAIMS = ObjectiveCriteria.registerCustom("flan:claim_number", true, ObjectiveCriteria.RenderType.INTEGER);

    public static void init() {
    }
}
