package io.github.flemmli97.flan.api.permission;

import com.google.common.collect.ImmutableMap;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JsonOps;
import io.github.flemmli97.flan.Flan;
import io.github.flemmli97.flan.config.ConfigHandler;
import net.minecraft.core.HolderLookup;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * The permission manager which holds all registered permissions from datapacks
 */
public class PermissionManager extends SimpleJsonResourceReloadListener {

    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(Flan.MODID, "claim_permission");
    public static final String DIRECTORY = String.format("%s/%s", ID.getNamespace(), ID.getPath());

    private static final Gson GSON = new GsonBuilder().create();

    private static PermissionManager INSTANCE;

    private Map<ResourceLocation, ClaimPermission> permissions = ImmutableMap.of();
    private List<ClaimPermission> sorted = List.of();

    private final HolderLookup.Provider provider;

    private PermissionManager(HolderLookup.Provider provider) {
        super(GSON, DIRECTORY);
        this.provider = provider;
    }

    public static PermissionManager create(HolderLookup.Provider provider) {
        INSTANCE = new PermissionManager(provider);
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
    protected void apply(Map<ResourceLocation, JsonElement> data, ResourceManager manager, ProfilerFiller profiler) {
        ImmutableMap.Builder<ResourceLocation, ClaimPermission> builder = ImmutableMap.builder();
        DynamicOps<JsonElement> ops = this.provider.createSerializationContext(JsonOps.INSTANCE);
        data.forEach((res, el) -> {
            try {
                ClaimPermission.Builder props = ClaimPermission.Builder.CODEC.parse(ops, parseLegacy(res, el))
                        .getOrThrow();
                if (props.verify())
                    builder.put(res, props.build(res));
            } catch (Exception ex) {
                Flan.LOGGER.error("Couldn't parse claim permission json {} {}", res, ex);
                ex.fillInStackTrace();
            }
        });
        this.permissions = builder.build();
        this.sorted = this.permissions.values().stream().sorted().toList();
        ConfigHandler.CONFIG.validatePermissionConfigs();
    }

    private static final Map<String, String> LEGACY = Map.of("requiredMod", "required_mod",
            "guiItem", "gui_item",
            "defaultVal", "default_value");

    private static JsonElement parseLegacy(ResourceLocation id, JsonElement element) {
        if (element.isJsonObject()) {
            JsonObject obj = element.getAsJsonObject();
            List<String> legacy = new ArrayList<>();
            obj.keySet().forEach(key -> {
                if (LEGACY.containsKey(key))
                    legacy.add(key);
            });
            legacy.forEach(key -> {
                JsonElement e = obj.get(key);
                obj.remove(key);
                obj.add(LEGACY.get(key), e);
            });
            if (!legacy.isEmpty())
                Flan.LOGGER.warn("Legacy claim_permission {}. Following keys are outdated {}. Please update them! Refer to the documentations", id, legacy);
            return obj;
        }
        return element;
    }
}
