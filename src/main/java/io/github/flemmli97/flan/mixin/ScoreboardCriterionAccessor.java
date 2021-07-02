package io.github.flemmli97.flan.mixin;

import net.minecraft.scoreboard.ScoreboardCriterion;
import org.spongepowered.asm.mixin.gen.Accessor;

public interface ScoreboardCriterionAccessor {

    @Accessor("method_37269")
    static ScoreboardCriterion newCriteria(String string, boolean bl, ScoreboardCriterion.RenderType renderType) {
        throw new IllegalStateException();
    }
}
