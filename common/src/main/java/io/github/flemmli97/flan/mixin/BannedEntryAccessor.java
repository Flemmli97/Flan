package io.github.flemmli97.flan.mixin;

import net.minecraft.server.players.BanListEntry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Date;

@Mixin(BanListEntry.class)
public interface BannedEntryAccessor {

    @Accessor("created")
    Date getCreationDate();
}
