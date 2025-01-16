package io.github.flemmli97.flan.api.permission;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.mojang.serialization.JsonOps;
import io.github.flemmli97.flan.Flan;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DataProvider;
import net.minecraft.data.HashCache;
import net.minecraft.resources.ResourceLocation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

/**
 * The provider for datagen runs
 */
public abstract class ClaimPermissionProvider implements DataProvider {

    private static final Logger LOGGER = LogManager.getLogger();

    private static final Gson GSON = new GsonBuilder().enableComplexMapKeySerialization().setPrettyPrinting().disableHtmlEscaping().create();

    private final Map<ResourceLocation, ClaimPermission.Builder> data = new HashMap<>();

    private final DataGenerator gen;

    public ClaimPermissionProvider(DataGenerator gen) {
        this.gen = gen;
    }

    protected abstract void add();

    @Override
    public void run(HashCache cache) {
        this.add();
        this.data.forEach((res, data) -> {
            Path path = this.gen.getOutputFolder().resolve("data/" + res.getNamespace() + "/" + PermissionManager.DIRECTORY + "/" + res.getPath() + ".json");
            try {
                JsonElement obj = ClaimPermission.Builder.CODEC.encodeStart(JsonOps.INSTANCE, data)
                        .getOrThrow(false, Flan::error);
                DataProvider.save(GSON, cache, obj, path);
            } catch (IOException e) {
                LOGGER.error("Couldn't save itemstat {}", path, e);
            }
        });
    }

    @Override
    public String getName() {
        return "Permissions";
    }

    public void addPermission(ResourceLocation res, ClaimPermission.Builder permission) {
        this.data.put(res, permission);
    }
}