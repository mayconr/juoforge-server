package com.github.mayconr.juoserver.network.packet;

import java.net.InetAddress;
import java.net.UnknownHostException;

import org.apache.commons.lang3.ArrayUtils;

import com.github.mayconr.juoserver.infrastructure.server.AbstractPacket;

import io.netty.buffer.ByteBuf;

public class LoginSeedPacket extends AbstractPacket {

    public static final int CODE = (byte) 0xEF;
    private InetAddress address;
    private String clientVersion;

    public LoginSeedPacket(ByteBuf buf) {
        super(CODE, 4);

        // buf.readByte(); // ignore code

        byte[] addressBytes = new byte[4];
        buf.readBytes(addressBytes);
        try {
            ArrayUtils.reverse(addressBytes);
            address = InetAddress.getByAddress(addressBytes);
        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        }
        // clientVersion = buf.readInt()+"."+buf.readInt()+"."+buf.readInt()+"."+buf.readInt();
    }

    public InetAddress getAddress() {
        return address;
    }

    public String getClientVersion() {
        return clientVersion;
    }

    @Override
    public String toString() {
        return "LoginSeedPacket{"
                + "address="
                + address
                + ", clientVersion='"
                + clientVersion
                + '\''
                + '}';
    }
}
