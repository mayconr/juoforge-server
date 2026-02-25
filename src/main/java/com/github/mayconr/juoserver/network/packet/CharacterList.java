package com.github.mayconr.juoserver.network.packet;

import com.github.mayconr.juoserver.game.model.AccountMobile;
import com.github.mayconr.juoserver.game.model.CharacterListFlag;
import com.github.mayconr.juoserver.game.model.UOCity;
import com.github.mayconr.juoserver.infrastructure.server.AbstractPacket;
import io.netty.buffer.ByteBuf;
import lombok.Getter;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

@Getter
public class CharacterList extends AbstractPacket {
    public static final int CODE = (byte) 0xA9;

    private final List<AccountMobile> mobiles;
    private final Map<Integer, UOCity> cities;
    private final CharacterListFlag[] flags;

    public CharacterList(
            List<AccountMobile> mobiles, Map<Integer, UOCity> cities, CharacterListFlag... flags) {
        super(CODE, calculateLength(mobiles.size(), cities.size()));
        this.mobiles = mobiles;
        this.cities = cities;
        this.flags = flags;
    }

    private static int calculateLength(int mobileCount, int locationCount) {
        return 1
                + 2
                + +1
                + mobileCount * (30 + 30)
                + 1
                + locationCount * (1 + 32 + 32 + 6 * 4)
                + 4;
    }

    @Override
    public void writesTo(ByteBuf buf) {
        buf.writeByte(CODE);
        buf.writeShort(getLength());
        buf.writeByte(mobiles.size());
        for (AccountMobile mobile : mobiles) {
            buf.writeBytes(padString(mobile.name(), 30, StandardCharsets.UTF_8));
            buf.writeBytes(padString("password", 30, StandardCharsets.UTF_8));
        }
        buf.writeByte(cities.size());

        for (Map.Entry<Integer, UOCity> entry : cities.entrySet()) {
            final int counter = entry.getKey();
            final var uoCity = entry.getValue();

            buf.writeByte(counter);
            buf.writeBytes(padString(uoCity.name(), 32, StandardCharsets.UTF_8));
            buf.writeBytes(padString(uoCity.location(), 32, StandardCharsets.UTF_8));
            buf.writeInt(uoCity.startingLocation().getX());
            buf.writeInt(uoCity.startingLocation().getY());
            buf.writeInt(uoCity.startingLocation().getZ());
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
