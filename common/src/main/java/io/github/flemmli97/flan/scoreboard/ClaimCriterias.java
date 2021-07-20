package io.github.flemmli97.flan.scoreboard;

import net.minecraft.scoreboard.ScoreboardCriterion;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

public class ClaimCriterias {

    public static ScoreboardCriterion AMOUNT = create("flan:total_claimblocks", true, ScoreboardCriterion.RenderType.INTEGER);
    public static ScoreboardCriterion USED = create("flan:used_claimblocks", true, ScoreboardCriterion.RenderType.INTEGER);
    public static ScoreboardCriterion FREE = create("flan:free_claimblocks", true, ScoreboardCriterion.RenderType.INTEGER);
    public static ScoreboardCriterion CLAIMS = create("flan:claim_number", true, ScoreboardCriterion.RenderType.INTEGER);

    public static void init() {

    }

    /**
     * Just reflection cause its only called once during init
     */
    private static ScoreboardCriterion create(String name, boolean readOnly, ScoreboardCriterion.RenderType renderType) {
        try {
            Constructor<ScoreboardCriterion> cons = ScoreboardCriterion.class.getDeclaredConstructor(String.class, boolean.class, ScoreboardCriterion.RenderType.class);
            cons.setAccessible(true);
            return cons.newInstance(name, readOnly, renderType);
        } catch (NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }
        return null;
    }
}
