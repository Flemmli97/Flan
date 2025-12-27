package io.github.flemmli97.flan.claim;

import com.mojang.authlib.yggdrasil.ProfileResult;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.players.NameAndId;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class ClaimUtils {

    public static MutableComponent translatedText(String key, Object... compArgs) {
        List<ChatFormatting> formattings = new ArrayList<>();
        List<Object> args = new ArrayList<>();
        for (Object obj : compArgs) {
            if (obj instanceof ChatFormatting formatting)
                formattings.add(formatting);
            else {
                if (obj instanceof Boolean b) {
                    args.add(b ? Component.translatable("flan.generic.true") : Component.translatable("flan.generic.false"));
                } else if (obj instanceof Component || TranslatableContents.isAllowedPrimitiveArgument(obj))
                    args.add(obj);
                else
                    args.add(obj.toString());
            }
        }
        return Component.translatable(key,
                args.toArray()).setStyle(Style.EMPTY.applyFormats(formattings.toArray(ChatFormatting[]::new)));
    }

    public static Optional<String> fetchUsername(UUID uuid, MinecraftServer server) {
        return fetchUsername(uuid, server, false);
    }

    public static Optional<String> fetchUsername(UUID uuid, MinecraftServer server, boolean fetch) {
        String ownerName = server.services().nameToIdCache().get(uuid).map(NameAndId::name).orElse(null);
        if (ownerName == null && fetch) {
            ProfileResult res = server.services().sessionService().fetchProfile(uuid, true);
            ownerName = res != null ? res.profile().name() : null;
        }
        return Optional.ofNullable(ownerName);
    }
}
