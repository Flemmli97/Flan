package io.github.flemmli97.flan.api.data;

import net.minecraft.util.math.BlockPos;

public interface IPermissionStorage {

    IPermissionContainer getForPermissionCheck(BlockPos pos);
}
