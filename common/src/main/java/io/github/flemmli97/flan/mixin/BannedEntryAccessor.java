package io.github.flemmli97.flan.mixin;

import net.minecraft.server.BanEntry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Date;

@Mixin(BanEntry.class)
public interface BannedEntryAccessor {

    @Accessor("creationDate")
    Date getCreationDate();
}
