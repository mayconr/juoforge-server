package com.github.mayconr.juoserver.game.model.event;

import com.github.mayconr.juoserver.game.model.UOPlayer;
import com.github.mayconr.juoserver.infrastructure.eventbus.GameEvent;
import com.github.mayconr.juoserver.network.packet.Packet;

import java.util.List;
import java.util.Objects;

/** Requests ordered delivery of debug packets to a single player's active session. */
public record RawPacketsSent(UOPlayer player, List<Packet> packets) implements GameEvent {
    public RawPacketsSent {
        Objects.requireNonNull(player, "Packet recipient is required");
        packets = List.copyOf(packets);
    }
}
