package com.flemmli97.flan.claim;

import com.flemmli97.flan.config.ConfigHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;

import java.util.EnumMap;

public class GlobalClaim implements IPermissionContainer{

    private final ServerWorld world;
    public GlobalClaim(ServerWorld world){
        this.world = world;
    }

    @Override
    public boolean canInteract(ServerPlayerEntity player, EnumPermission perm, BlockPos pos, boolean message) {
        if(ConfigHandler.config.globalDefaultPerms.containsKey(this.world.getRegistryKey().getValue().toString())){
            EnumMap<EnumPermission, Boolean> permMap = ConfigHandler.config.globalDefaultPerms.get(this.world.getRegistryKey().getValue().toString());
            if(permMap.containsKey(perm)) {
                if (permMap.get(perm))
                    return true;
                if (message)
                    player.sendMessage(PermHelper.simpleColoredText(ConfigHandler.lang.noPermissionSimple, Formatting.DARK_RED), true);
                return false;
            }
        }
        return true;
    }
}
