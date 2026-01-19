package com.github.mayconr.juoserver.network.packet;

import io.netty.buffer.ByteBuf;

public interface Packet {

    int getCode();

    int getLength();

    void writesTo(ByteBuf buf);
}
