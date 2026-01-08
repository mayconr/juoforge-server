package com.github.mayconr.juoserver.game.packet;

import java.nio.charset.StandardCharsets;
import java.util.List;

import com.github.mayconr.juoserver.game.core.model.CharacterListFlag;
import com.github.mayconr.juoserver.game.core.model.UOCity;
import com.github.mayconr.juoserver.game.core.model.UOPlayer;
import com.github.mayconr.juoserver.game.server.AbstractPacket;

import io.netty.buffer.ByteBuf;

public class CharacterList extends AbstractPacket {
    public static final int CODE = (byte) 0xA9;

    private final List<UOPlayer> characters;
    private final List<UOCity> cities;
    private final CharacterListFlag[] flags;

    public CharacterList(List<UOPlayer> players, List<UOCity> cities, CharacterListFlag... flags) {
        super(CODE, calculateLength(players, cities));
        this.characters = players;
        this.cities = cities;
        this.flags = flags;
    }

    private static int calculateLength(List<UOPlayer> players, List<UOCity> cities) {
        return 1
                + 2
                + +1
                + players.size() * (30 + 30)
                + 1
                + cities.size() * (1 + 32 + 32 + 6 * 4)
                + 4;
    }

    @Override
    public void writesTo(ByteBuf buf) {
        buf.writeByte(CODE);
        buf.writeShort(getLength());
        buf.writeByte(characters.size());
        for (UOPlayer player : characters) {
            buf.writeBytes(padString(player.getName(), 30, StandardCharsets.UTF_8));
            buf.writeBytes(padString(player.getPassword(), 30, StandardCharsets.UTF_8));
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

    public List<UOPlayer> getCharacters() {
        return characters;
    }

    public List<UOCity> getCities() {
        return cities;
    }
}
