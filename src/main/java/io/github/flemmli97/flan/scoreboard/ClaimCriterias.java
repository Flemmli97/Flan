package io.github.flemmli97.flan.scoreboard;

import io.github.flemmli97.flan.mixin.ScoreboardCriterionAccessor;
import net.minecraft.scoreboard.ScoreboardCriterion;

public class ClaimCriterias {

    public static ScoreboardCriterion AMOUNT = ScoreboardCriterionAccessor.newCriteria("flan:total_claimblocks", true, ScoreboardCriterion.RenderType.INTEGER);
    public static ScoreboardCriterion USED = ScoreboardCriterionAccessor.newCriteria("flan:used_claimblocks", true, ScoreboardCriterion.RenderType.INTEGER);
    public static ScoreboardCriterion FREE = ScoreboardCriterionAccessor.newCriteria("flan:free_claimblocks", true, ScoreboardCriterion.RenderType.INTEGER);
    public static ScoreboardCriterion CLAIMS = ScoreboardCriterionAccessor.newCriteria("flan:claim_number", true, ScoreboardCriterion.RenderType.INTEGER);

}
