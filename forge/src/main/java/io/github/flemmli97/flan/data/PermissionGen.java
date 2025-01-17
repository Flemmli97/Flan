package io.github.flemmli97.flan.data;

import io.github.flemmli97.flan.api.permission.BuiltinPermission;
import io.github.flemmli97.flan.api.permission.ClaimPermission;
import io.github.flemmli97.flan.api.permission.provider.ClaimPermissionProvider;
import io.github.flemmli97.flan.platform.integration.create.CreateCompat;
import net.minecraft.data.DataGenerator;
import net.minecraft.resources.ResourceLocation;

import java.util.List;

public class PermissionGen extends ClaimPermissionProvider {

    public PermissionGen(DataGenerator gen) {
        super(gen);
    }

    @Override
    protected void add() {
        BuiltinPermission.DATAGEN_DATA.forEach(this::addPermission);
        this.addPermission(CreateCompat.CREATE, new ClaimPermission.Builder(
                new ClaimPermission.Builder.ItemStackHolder(new ResourceLocation("create:cart_assembler")),
                false, false, BuiltinPermission.order++, "create",
                List.of("Gives permission to allow minecart contraptions to pass through claim border.",
                        "Note if this is disabled and your contraption goes out of the claim it can't go back in!")));
    }
}
