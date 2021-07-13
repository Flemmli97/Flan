package io.github.flemmli97.flan.mixin;

import net.minecraft.scoreboard.ScoreboardCriterion;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(ScoreboardCriterion.class)
public interface ScoreboardCriterionAccessor {

    @Invoker("create")
    static ScoreboardCriterion newCriteria(String string, boolean bl, ScoreboardCriterion.RenderType renderType) {
        throw new IllegalStateException();
    }
}
