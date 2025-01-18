package io.github.flemmli97.flan.player;

import com.google.common.collect.Sets;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.game.ClientboundBlockUpdatePacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Handles showing the client fake blocks
 */
public class ClientBlockDisplayTracker {

    private final Map<UUID, Set<DisplayData>> fakeBlocks = new HashMap<>();
    /**
     * Reverse lookup. Sometimes displays can use same position.
     * Used to "restore" an overwritten fake block
     */
    private final Map<BlockPos, List<UUID>> lookup = new HashMap<>();

    private final ServerPlayer player;

    public ClientBlockDisplayTracker(ServerPlayer player) {
        this.player = player;
    }

    public void displayFakeBlocks(UUID id, DisplayData... blocks) {
        Set<DisplayData> newData = new HashSet<>(List.of(blocks));
        this.displayFakeBlocks(id, newData);
    }

    public void displayFakeBlocks(UUID id, Set<DisplayData> blocks) {
        Set<DisplayData> current = this.fakeBlocks.get(id);
        Set<DisplayData> diff;
        if (current != null) {
            diff = Sets.difference(current, blocks);
            if (!diff.isEmpty())
                this.resetBlocks(id, diff);
        }
        for (DisplayData data : blocks) {
            this.player.connection.send(new ClientboundBlockUpdatePacket(data.pos, data.state));
            this.lookup.computeIfAbsent(data.pos, k -> new ArrayList<>())
                    .add(id);
        }
        this.fakeBlocks.put(id, blocks);
    }

    public void resetFakeBlocks(UUID id) {
        Set<DisplayData> current = this.fakeBlocks.remove(id);
        if (current != null) {
            this.resetBlocks(id, current);
        }
    }

    private void resetBlocks(UUID id, Set<DisplayData> blocks) {
        blocks.forEach(d -> {
            List<UUID> stateLookup = this.lookup.get(d.pos);
            BlockState state = null;
            // Restore overwritten fakeblocks if possible
            if (stateLookup != null) {
                stateLookup.removeIf(i -> i.equals(id));
                if (!stateLookup.isEmpty()) {
                    Set<DisplayData> others = this.fakeBlocks.get(stateLookup.get(stateLookup.size() - 1));
                    if (others != null) {
                        for (DisplayData o : others) {
                            if (o.pos.equals(d.pos)) {
                                state = o.state;
                                break;
                            }
                        }
                    }
                }
                if (stateLookup.isEmpty()) {
                    this.lookup.remove(d.pos);
                }
            }
            this.player.connection.send(new ClientboundBlockUpdatePacket(d.pos, state == null ? this.player.level().getBlockState(d.pos) : state));
        });
    }

    public record DisplayData(BlockPos pos, BlockState state) {

        @Override
        public int hashCode() {
            return this.pos.hashCode();
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this)
                return true;
            if (!(obj instanceof DisplayData data))
                return false;
            return this.pos.equals(data.pos);
        }
    }
}
