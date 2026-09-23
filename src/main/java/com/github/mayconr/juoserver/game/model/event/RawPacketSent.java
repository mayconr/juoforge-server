package com.github.mayconr.juoserver.game.model.event;

import com.github.mayconr.juoserver.game.model.UOPlayer;
import com.github.mayconr.juoserver.infrastructure.eventbus.GameEvent;
import com.github.mayconr.juoserver.network.packet.Packet;

import java.util.Objects;

/** Requests delivery of a debug packet to a single player's active session. */
public record RawPacketSent(UOPlayer player, Packet packet) implements GameEvent {
    public RawPacketSent {
        Objects.requireNonNull(player, "Packet recipient is required");
        Objects.requireNonNull(packet, "Packet is required");
    }
}
