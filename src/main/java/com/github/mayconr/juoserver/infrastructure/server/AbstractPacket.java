package com.github.mayconr.juoserver.infrastructure.server;

import java.io.ByteArrayOutputStream;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import com.github.mayconr.juoserver.network.packet.Packet;

import io.netty.buffer.ByteBuf;

public abstract class AbstractPacket implements Packet {

    private final int code;
    private final int length;

    public AbstractPacket(int code, int length) {
        this.code = code;
        this.length = length;
    }

    public void writesTo(ByteBuf buf) {
        throw new UnsupportedOperationException("Packet does not support serialization");
    }

    @Override
    public int getCode() {
        return code;
    }

    @Override
    public int getLength() {
        return length;
    }

    protected String readStringTrailingZeros(ByteBuf byteBuf, int length, Charset charset) {
        var tempBuffer = byteBuf.readBytes(length);
        int end = tempBuffer.writerIndex() - 1;
        while (end >= tempBuffer.readerIndex() && tempBuffer.getByte(end) == 0) {
            end--;
        }
        int newLength = end - tempBuffer.readerIndex() + 1;

        return tempBuffer.readCharSequence(newLength, charset).toString();
    }

    protected final byte[] padString(String text, int newLength, Charset charset) {
        return Arrays.copyOf(text.getBytes(charset), newLength);
    }

    protected final InetAddress readInetAddress(ByteBuf buf) {
        try {
            byte[] address = new byte[4];
            buf.readBytes(address);
            return InetAddress.getByAddress(address);
        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        }
    }

    protected String readNullTerminatedUnicodeString(ByteBuf buf) {
        StringBuilder sb = new StringBuilder();
        while (true) {
            char c = buf.readChar(); // UTF-16BE
            if (c == 0) break;
            sb.append(c);
        }
        return sb.toString();
    }

    protected String readNullTerminatedAsciiString(ByteBuf buf) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        while (true) {
            byte b = buf.readByte();
            if (b == 0) break;
            baos.write(b);
        }
        return baos.toString(StandardCharsets.US_ASCII);
    }
}
