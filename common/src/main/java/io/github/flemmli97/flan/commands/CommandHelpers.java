package io.github.flemmli97.flan.commands;

import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import io.github.flemmli97.flan.api.permission.BuiltinPermission;
import io.github.flemmli97.flan.api.permission.ClaimPermission;
import io.github.flemmli97.flan.api.permission.PermissionManager;
import io.github.flemmli97.flan.claim.Claim;
import io.github.flemmli97.flan.claim.ClaimStorage;
import io.github.flemmli97.flan.claim.ClaimUtils;
import io.github.flemmli97.flan.claim.attachment.ClaimAllowListKey;
import io.github.flemmli97.flan.config.ConfigHandler;
import io.github.flemmli97.flan.player.PlayerClaimData;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.GameProfileArgument;
import net.minecraft.commands.arguments.ResourceOrTagKeyArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.NameAndId;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class CommandHelpers {

    private static final Pattern ALLOWED = Pattern.compile("[a-zA-Z0-9_+.-]+");

    public static CompletableFuture<Suggestions> claimSuggestions(CommandContext<CommandSourceStack> context, SuggestionsBuilder build, UUID owner) {
        return SharedSuggestionProvider.suggest(ClaimStorage.get(context.getSource().getLevel()).allClaimsFromPlayer(owner)
                .stream().map(claim -> claim.getClaimName().isEmpty() ? claim.getClaimID().toString() : claim.getClaimName()).collect(Collectors.toList()), build);
    }

    public static NameAndId singleProfile(CommandContext<CommandSourceStack> context, String arg) throws CommandSyntaxException {
        Collection<NameAndId> profs = GameProfileArgument.getGameProfiles(context, arg);
        if (profs.size() != 1) {
            throw new SimpleCommandExceptionType(ClaimUtils.translatedText("flan.onlyOnePlayer")).create();
        }
        return profs.stream().findFirst().get();
    }

    public static CompletableFuture<Suggestions> permSuggestions(CommandContext<CommandSourceStack> context, SuggestionsBuilder build, boolean group) {
        ServerLevel level = context.getSource().getLevel();
        Claim claim = ClaimStorage.get(level).getClaimAt(BlockPos.containing(context.getSource().getPosition()));
        boolean admin = claim != null && claim.isAdminClaim();
        List<String> allowedPerms = new ArrayList<>();
        for (ClaimPermission perm : PermissionManager.getInstance().getAll()) {
            if (!admin && ConfigHandler.CONFIG.globallyDefined(level, perm.getId())) {
                continue;
            }
            if (!group || !perm.global)
                allowedPerms.add(perm.getId().toString());
        }
        return SharedSuggestionProvider.suggest(allowedPerms, build);
    }

    public static CompletableFuture<Suggestions> groupSuggestion(CommandContext<CommandSourceStack> context, SuggestionsBuilder build) throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();
        List<String> list = new ArrayList<>();
        ClaimStorage storage = ClaimStorage.get(player.level());
        Claim claim = storage.getClaimAt(player.blockPosition());
        if (claim != null && claim.canInteract(player, BuiltinPermission.EDITPERMS, player.blockPosition())) {
            list = claim.groups();
        }
        for (int i = 0; i < list.size(); i++) {
            if (ALLOWED.matcher(list.get(i)).matches())
                continue;
            list.set(i, '\"' + list.get(i) + '\"');
        }
        return SharedSuggestionProvider.suggest(list, build);
    }

    public static CompletableFuture<Suggestions> personalGroupSuggestion(CommandContext<CommandSourceStack> context, SuggestionsBuilder build) throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();
        List<String> list = new ArrayList<>(PlayerClaimData.get(player).playerDefaultGroups().keySet());
        list.sort(null);
        for (int i = 0; i < list.size(); i++) {
            if (ALLOWED.matcher(list.get(i)).matches())
                continue;
            list.set(i, '\"' + list.get(i) + '\"');
        }
        return SharedSuggestionProvider.suggest(list, build);
    }

    public static <T extends Enum<T>> T parseEnum(Class<T> clss, String name, T fallback) {
        try {
            return Enum.valueOf(clss, name);
        } catch (IllegalArgumentException e) {
            return fallback;
        }
    }

    public static <T extends Enum<T>> CompletableFuture<Suggestions> enumSuggestion(Class<T> clss, SuggestionsBuilder build) {
        return SharedSuggestionProvider.suggest(Stream.of(clss.getEnumConstants()).map(Object::toString), build);
    }

    @SuppressWarnings("unchecked")
    public static <T> ResourceOrTagKeyArgument.Result<T> getRegistryType(CommandContext<CommandSourceStack> context, String name, ResourceKey<Registry<T>> registryKey) throws CommandSyntaxException {
        ResourceOrTagKeyArgument.Result<?> result = (ResourceOrTagKeyArgument.Result<T>) context.getArgument(name, ResourceOrTagKeyArgument.Result.class);
        Optional<ResourceOrTagKeyArgument.Result<T>> optional = result.cast(registryKey);
        return optional.orElseThrow(() -> new DynamicCommandExceptionType((object) ->
                ClaimUtils.translatedText("No such entry %1$s", object)).create(result));
    }

    public static CompletableFuture<Suggestions> claimEntryListSuggestion(CommandContext<CommandSourceStack> context, SuggestionsBuilder build, ClaimAllowListKey<?> key) throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();
        List<String> list = new ArrayList<>();
        ClaimStorage storage = ClaimStorage.get(player.level());
        Claim claim = storage.getClaimAt(player.blockPosition());
        if (claim != null && claim.canInteract(player, BuiltinPermission.EDITPERMS, player.blockPosition())) {
            list = claim.allowedEntries.get(key).asString();
        }
        return SharedSuggestionProvider.suggest(list, build);
    }
}
