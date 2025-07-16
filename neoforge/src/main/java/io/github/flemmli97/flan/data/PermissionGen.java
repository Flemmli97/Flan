package io.github.flemmli97.flan.data;

import io.github.flemmli97.flan.api.permission.BuiltinPermission;
import io.github.flemmli97.flan.api.permission.ClaimPermission;
import io.github.flemmli97.flan.api.permission.provider.ClaimPermissionProvider;
import io.github.flemmli97.flan.platform.integration.create.CreateCompat;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class PermissionGen extends ClaimPermissionProvider {

    public PermissionGen(PackOutput output, CompletableFuture<HolderLookup.Provider> lookup) {
        super(output, lookup);
    }

    @Override
    protected void add(HolderLookup.Provider provider) {
        BuiltinPermission.DATAGEN_DATA.forEach((id, builder) -> this.addPermission(id, builder.apply(provider)));
        this.addPermission(CreateCompat.CREATE, new ClaimPermission.Builder(
                new ClaimPermission.Builder.ItemStackHolder(ResourceLocation.parse("create:cart_assembler")),
                BuiltinPermission.order++,
                List.of("Gives permission to allow minecart contraptions to pass through claim border.",
                        "Note if this is disabled and your contraption goes out of the claim it can't go back in!"))
                .global(false).requiredMod("create"));
    }
}
