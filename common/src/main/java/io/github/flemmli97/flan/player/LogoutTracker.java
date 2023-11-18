package io.github.flemmli97.flan.player;

import io.github.flemmli97.flan.config.ConfigHandler;
import io.github.flemmli97.flan.utils.LogoutImpl;
import net.minecraft.server.MinecraftServer;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public class LogoutTracker {

    private final Set<LogoutTicket> tracker = new HashSet<>();
    private final Set<UUID> trackerUUID = new HashSet<>();

    public static LogoutTracker getInstance(MinecraftServer server) {
        return ((LogoutImpl) server).getInstance();
    }

    public void track(UUID player) {
        if (ConfigHandler.config.offlineProtectActivation == -1)
            return;
        this.trackerUUID.add(player);
        this.tracker.add(new LogoutTicket(player));
    }

    public boolean justLoggedOut(UUID uuid) {
        return this.trackerUUID.contains(uuid);
    }

    public void tick() {
        this.tracker.stream().filter(LogoutTicket::tick)
                .collect(Collectors.toSet())
                .forEach(ticket -> {
                    this.tracker.remove(ticket);
                    this.trackerUUID.remove(ticket.uuid);
                });
    }

    private static class LogoutTicket {
        private final UUID uuid;
        private int time = ConfigHandler.config.offlineProtectActivation;

        public LogoutTicket(UUID player) {
            this.uuid = player;
        }

        public boolean tick() {
            return --this.time <= 0;
        }

        @Override
        public String toString() {
            return String.format("LogoutTicket: UUID=%s, TimeLeft=%d", this.uuid, this.time);
        }
    }
}
