package com.github.mayconr.juoserver.network.packet;

import java.nio.charset.StandardCharsets;
import java.util.List;

import com.github.mayconr.juoserver.game.model.AccountLoginMobile;
import com.github.mayconr.juoserver.game.model.CharacterListFlag;
import com.github.mayconr.juoserver.game.model.UOCity;
import com.github.mayconr.juoserver.infrastructure.server.AbstractPacket;

import io.netty.buffer.ByteBuf;
import lombok.Getter;

@Getter
public class CharacterList extends AbstractPacket {
    public static final int CODE = (byte) 0xA9;

    private final List<AccountLoginMobile> mobiles;
    private final List<UOCity> cities;
    private final CharacterListFlag[] flags;

    public CharacterList(
            List<AccountLoginMobile> mobiles, List<UOCity> cities, CharacterListFlag... flags) {
        super(CODE, calculateLength(mobiles, cities));
        this.mobiles = mobiles;
        this.cities = cities;
        this.flags = flags;
    }

    private static int calculateLength(List<AccountLoginMobile> mobiles, List<UOCity> cities) {
        return 1
                + 2
                + +1
                + mobiles.size() * (30 + 30)
                + 1
                + cities.size() * (1 + 32 + 32 + 6 * 4)
                + 4;
    }

    @Override
    public void writesTo(ByteBuf buf) {
        buf.writeByte(CODE);
        buf.writeShort(getLength());
        buf.writeByte(mobiles.size());
        for (AccountLoginMobile mobile : mobiles) {
            buf.writeBytes(padString(mobile.name(), 30, StandardCharsets.UTF_8));
            buf.writeBytes(padString("password", 30, StandardCharsets.UTF_8));
        }
        buf.writeByte(cities.size());

        int counter = 0;
        for (UOCity uoCity : cities) {
            buf.writeByte(counter++);
            buf.writeBytes(padString(uoCity.getName(), 32, StandardCharsets.UTF_8));
            buf.writeBytes(padString(uoCity.getLocation(), 32, StandardCharsets.UTF_8));
            buf.writeInt(uoCity.getStartingLocation().getX());
            buf.writeInt(uoCity.getStartingLocation().getY());
            buf.writeInt(uoCity.getStartingLocation().getZ());
            buf.writeInt(0);
            buf.writeInt(0);
            buf.writeInt(0);
        }
        int flagValue = 0;
        for (CharacterListFlag flag : flags) {
            flagValue |= flag.getCode();
        }
        buf.writeInt(flagValue);
    }
}
