package io.github.flemmli97.flan.api.permission;

import com.google.common.collect.ImmutableMap;
import io.github.flemmli97.flan.Flan;
import io.github.flemmli97.flan.config.ConfigHandler;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * The permission manager which holds all registered permissions from datapacks
 */
public class PermissionManager extends SimpleJsonResourceReloadListener<ClaimPermission.Builder> {

    public static final ResourceKey<? extends Registry<ClaimPermission.Builder>> ID =
            ResourceKey.createRegistryKey(ResourceLocation.fromNamespaceAndPath(Flan.MODID, "claim_permission"));

    private static PermissionManager INSTANCE;

    private Map<ResourceLocation, ClaimPermission> permissions = ImmutableMap.of();
    private List<ClaimPermission> sorted = List.of();

    private PermissionManager(HolderLookup.Provider provider) {
        super(provider, ClaimPermission.Builder.CODEC, ID);
    }

    public static PermissionManager create(HolderLookup.Provider provider) {
        PermissionManager.INSTANCE = new PermissionManager(provider);
        return getInstance();
    }

    /**
     * The permission manager instance. This will be null if datapacks are not loaded yet.
     */
    public static PermissionManager getInstance() {
        return INSTANCE;
    }

    @Nullable
    public ClaimPermission get(ResourceLocation id) {
        return this.permissions.get(id);
    }

    public Collection<ResourceLocation> getIds() {
        return this.permissions.keySet();
    }

    public Collection<ClaimPermission> getAll() {
        return this.sorted;
    }

    public boolean isGlobalPermission(ResourceLocation id) {
        ClaimPermission perm = this.get(id);
        return perm != null && perm.global;
    }

    @Override
    protected void apply(Map<ResourceLocation, ClaimPermission.Builder> data, ResourceManager manager, ProfilerFiller profiler) {
        ImmutableMap.Builder<ResourceLocation, ClaimPermission> builder = ImmutableMap.builder();
        data.forEach((res, props) -> {
            if (props.verify())
                builder.put(res, props.build(res));
        });
        this.permissions = builder.build();
        this.sorted = this.permissions.values().stream().sorted().toList();
        ConfigHandler.CONFIG.validatePermissionConfigs();
    }
}
