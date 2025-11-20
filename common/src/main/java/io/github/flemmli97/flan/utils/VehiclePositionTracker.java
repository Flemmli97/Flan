package io.github.flemmli97.flan.utils;

import net.minecraft.world.phys.Vec3;

/**
 * For client side controlled vehicles such as boats we need to track the last position manually
 * as the vanilla data is incorrect there
 */
public interface VehiclePositionTracker {

    Vec3 flan$updateAndGetLastPosition();
}
