package io.github.flemmli97.flan.claim.attachment;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.github.flemmli97.flan.claim.Claim;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;

public class AllowedRegistryListHolder {

    private final Claim claim;
    private final Map<ClaimAllowListKey<?>, AllowedRegistryList<?>> entries = new HashMap<>();

    public AllowedRegistryListHolder(Claim claim) {
        this.claim = claim;
    }

    public <T> boolean isAllowed(ClaimAllowListKey<T> key, Predicate<T> test, Predicate<TagKey<T>> tagCheck) {
        AllowedRegistryList<T> entry = this.tryGet(key);
        return entry != null && entry.matches(test, tagCheck);
    }

    public <T> AllowedRegistryList<T> get(ClaimAllowListKey<T> key) {
        AllowedRegistryList<T> entry = this.tryGet(key);
        if (entry == null) {
            entry = key.factory().apply(this.claim);
            this.entries.put(key, entry);
        }
        return entry;
    }

    @SuppressWarnings("unchecked")
    private <T> AllowedRegistryList<T> tryGet(ClaimAllowListKey<T> key) {
        return (AllowedRegistryList<T>) this.entries.get(key);
    }

    public JsonElement save() {
        JsonObject obj = new JsonObject();
        this.entries.forEach((key, val) -> {
            obj.add(key.id().toString(), val.save());
        });
        return obj;
    }

    public void load(JsonElement element) {
        if (!element.isJsonObject())
            return;
        JsonObject obj = element.getAsJsonObject();
        obj.keySet().forEach(id -> {
            ClaimAllowListKey<?> key = ClaimAllowListKey.get(ResourceLocation.parse(id));
            if (key != null) {
                this.entries.put(key, key.factory().apply(this.claim).read(obj.get(id)));
            }
        });
    }
}
