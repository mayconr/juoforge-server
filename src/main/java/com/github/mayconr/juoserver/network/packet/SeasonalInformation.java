package com.github.mayconr.juoserver.network.packet;

import com.github.mayconr.juoserver.game.model.Season;
import com.github.mayconr.juoserver.infrastructure.server.AbstractPacket;

import io.netty.buffer.ByteBuf;

public class SeasonalInformation extends AbstractPacket {

    public static final int CODE = (byte) 0xBC;
    private Season season;
    private boolean playSounds;

    public SeasonalInformation(Season season, boolean playSounds) {
        super(CODE, 3);
        this.season = season;
        this.playSounds = playSounds;
    }

    @Override
    public void writesTo(ByteBuf buf) {
        buf.writeByte(CODE);
        buf.writeByte(season.getCode());
        buf.writeByte(playSounds ? 1 : 0);
    }
}
