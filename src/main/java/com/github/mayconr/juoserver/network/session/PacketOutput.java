package com.github.mayconr.juoserver.network.session;

import com.github.mayconr.juoserver.network.packet.Packet;

/** Packet output supplied to an operation running on the channel event loop. */
public interface PacketOutput {
    void write(Packet packet);
    void writeAndFlush(Packet packet);
    void flush();
}
