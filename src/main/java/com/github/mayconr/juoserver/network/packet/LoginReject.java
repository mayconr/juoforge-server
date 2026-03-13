package com.github.mayconr.juoserver.network.packet;

import com.github.mayconr.juoserver.infrastructure.server.AbstractPacket;

import io.netty.buffer.ByteBuf;

public class LoginReject extends AbstractPacket {

    private static final int CODE = (byte) 0x53;
    private final Reason reason;

    public LoginReject(Reason reason) {
        super(CODE, 2);
        this.reason = reason;
    }

    @Override
    public void writesTo(ByteBuf buf) {
        buf.writeByte(CODE);
        buf.writeByte(reason.code);
    }

    public enum Reason {
        INCORRECT_PASSWORD(0x00),
        CHAR_DOES_NOT_EXIST(0x01),
        CHAR_ALREADY_EXIST(0x02),
        SERVER_IS_DOWN(0x04),
        ANOTHER_CHAR_LOGGED_IN(0x05),
        SYNCHRONIZATION_ERROR(0x06),
        IDLE_TOO_LONG(0x07),
        COULD_NOT_ATTACH_SERVER(0x08),
        CHAR_TRANSFER(0x09);

        private final int code;

        Reason(int code) {
            this.code = code;
        }

        public int getCode() {
            return code;
        }
    }
}
