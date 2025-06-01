package io.github.flemmli97.flan.scoreboard;

import io.github.flemmli97.flan.Flan;
import net.minecraft.world.scores.criteria.ObjectiveCriteria;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

public class ClaimCriterias {

    public static final ObjectiveCriteria AMOUNT = create("flan:total_claimblocks", true, ObjectiveCriteria.RenderType.INTEGER);
    public static final ObjectiveCriteria USED = create("flan:used_claimblocks", true, ObjectiveCriteria.RenderType.INTEGER);
    public static final ObjectiveCriteria FREE = create("flan:free_claimblocks", true, ObjectiveCriteria.RenderType.INTEGER);
    public static final ObjectiveCriteria CLAIMS = create("flan:claim_number", true, ObjectiveCriteria.RenderType.INTEGER);

    public static void init() {
    }

    /**
     * Just reflection cause its only called once during init
     */
    private static ObjectiveCriteria create(String name, boolean readOnly, ObjectiveCriteria.RenderType renderType) {
        try {
            Constructor<ObjectiveCriteria> cons = ObjectiveCriteria.class.getDeclaredConstructor(String.class, boolean.class, ObjectiveCriteria.RenderType.class);
            cons.setAccessible(true);
            return cons.newInstance(name, readOnly, renderType);
        } catch (NoSuchMethodException | InstantiationException | IllegalAccessException |
                 InvocationTargetException e) {
            Flan.LOGGER.error(e);
        }
        return null;
    }
}
