package com.github.mayconr.juoserver.network.packet;

import java.nio.charset.StandardCharsets;

import com.github.mayconr.juoserver.game.model.UOMobile;
import com.github.mayconr.juoserver.infrastructure.server.AbstractPacket;

import io.netty.buffer.ByteBuf;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
public class OpenPaperdoll extends AbstractPacket {

    public static final int CODE = (byte) 0x88;
    private final UOMobile mobile;
    private final Flag flag;

    public OpenPaperdoll(UOMobile mobile, Flag flag) {
        super(CODE, 66);
        this.mobile = mobile;
        this.flag = flag;
    }

    @Override
    public void writesTo(ByteBuf buf) {
        buf.writeByte(CODE);
        buf.writeInt(mobile.getSerialId());
        buf.writeBytes(padString(mobile.getDisplayName(), 60, StandardCharsets.UTF_8));
        buf.writeByte(flag.getCode());
    }

    @Getter
    @RequiredArgsConstructor
    public enum Flag {
        NORMAL(0x00),
        CAN_ALTER_PAPERDOLL(0x02),
        POISONED(0x04),
        GOLDEN_HEALTH(0x08),
        WAR_MODE(0x40),
        HIDDEN(0x80);
        private final int code;
    }
}
