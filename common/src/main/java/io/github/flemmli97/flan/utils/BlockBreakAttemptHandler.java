package io.github.flemmli97.flan.utils;

import net.minecraft.core.BlockPos;

public interface BlockBreakAttemptHandler {

    void flan$setBlockBreakAttemptFail(BlockPos pos, boolean instaBreak);

    BlockPos flan$failedPos();

    boolean flan$wasInstabreak();
}
