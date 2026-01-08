package com.github.mayconr.juoserver.game;

import java.util.function.Consumer;

import org.junit.jupiter.api.Assertions;

import io.netty.buffer.ByteBuf;

public class PacketExpect {
    private final ByteBuf buf;

    private PacketExpect(ByteBuf buf) {
        this.buf = buf;
    }

    public static PacketExpect fromBuf(ByteBuf buf) {
        return new PacketExpect(buf);
    }

    public PacketExpect packet(int opcode, Consumer<ByteBuf> body) {
        int op = buf.readUnsignedByte();
        Assertions.assertEquals(opcode, op);
        body.accept(buf);
        return this;
    }

    public void end() {
        Assertions.assertEquals(0, buf.readableBytes());
    }
}
