package io.github.flemmli97.flan.api.permission;

import com.google.common.collect.ImmutableMap;
import com.google.gson.JsonElement;
import com.mojang.serialization.JsonOps;
import io.github.flemmli97.flan.Flan;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;

import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * The provider for datagen runs
 */
public abstract class ClaimPermissionProvider implements DataProvider {

    private final Map<ResourceLocation, ClaimPermission.Builder> data = new LinkedHashMap<>();

    private final PackOutput output;

    public ClaimPermissionProvider(PackOutput output) {
        this.output = output;
    }

    protected abstract void add();

    @Override
    public CompletableFuture<?> run(CachedOutput cache) {
        this.add();
        return CompletableFuture.allOf(this.data.entrySet().stream().map(entry -> {
            ResourceLocation res = entry.getKey();
            Path path = this.output.getOutputFolder(PackOutput.Target.DATA_PACK).resolve(res.getNamespace() + "/" + PermissionManager.DIRECTORY + "/" + res.getPath() + ".json");
            JsonElement obj = ClaimPermission.Builder.CODEC.encodeStart(JsonOps.INSTANCE, entry.getValue())
                    .getOrThrow(false, Flan::error);
            return DataProvider.saveStable(cache, obj, path);
        }).toArray(CompletableFuture<?>[]::new));
    }

    @Override
    public String getName() {
        return "Permissions";
    }

    public void addPermission(ResourceLocation res, ClaimPermission.Builder permission) {
        if (this.data.put(res, permission) != null) {
            throw new IllegalStateException("Permission already registered for " + res);
        }
    }

    public Map<ResourceLocation, ClaimPermission.Builder> getData() {
        return ImmutableMap.copyOf(this.data);
    }
}