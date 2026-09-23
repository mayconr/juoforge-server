package com.github.mayconr.juoserver.network.packet;

import io.netty.buffer.ByteBuf;

/** A server-to-client 0xBF subcommand. Implementations must keep their payload and length stable. */
public interface OutboundExtendedCommand {
    int subCommand();

    /** Returns the payload size, excluding the five-byte General Information header. */
    int payloadLength();

    /** Writes only the subcommand payload, without the packet header or subcommand ID. */
    void writesTo(ByteBuf buffer);
}
