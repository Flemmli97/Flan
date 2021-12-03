package io.github.flemmli97.flan.api.data;

import net.minecraft.core.BlockPos;

public interface IPermissionStorage {

    IPermissionContainer getForPermissionCheck(BlockPos pos);
}
