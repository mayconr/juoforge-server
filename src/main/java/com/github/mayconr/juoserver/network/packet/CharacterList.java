package com.github.mayconr.juoserver.network.packet;

import com.github.mayconr.juoserver.game.model.AccountMobile;
import com.github.mayconr.juoserver.game.model.CharacterListFlag;
import com.github.mayconr.juoserver.infrastructure.region.RegionNode;
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
    private final Map<Integer, RegionNode> startingLocations;
    private final CharacterListFlag[] flags;

    public CharacterList(
            List<AccountMobile> mobiles, Map<Integer, RegionNode> startingLocations, CharacterListFlag... flags) {
        super(CODE, calculateLength(mobiles.size(), startingLocations.size()));
        this.mobiles = mobiles;
        this.startingLocations = startingLocations;
        this.flags = flags;
    }

    private static int calculateLength(int mobileCount, int locationCount) {
        return 1
                + 2
                + 1
                + mobileCount * (30 + 30)
                + 1
                + locationCount * (1 + 32 + 32 + (6 * 4))
                + 4;
    }

    @Override
    public void writesTo(ByteBuf buf) {
        buf.writeByte(CODE);
        buf.writeShort(getLength());
        buf.writeByte(mobiles.size());
        for (AccountMobile mobile : mobiles) {
            buf.writeBytes(padString(mobile.name(), 30, StandardCharsets.UTF_8));
            buf.writeBytes(padString("", 30, StandardCharsets.UTF_8)); // password
        }
        buf.writeByte(startingLocations.size());

        for (Map.Entry<Integer, RegionNode> entry : startingLocations.entrySet()) {
            final int counter = entry.getKey();
            final var region = entry.getValue();

            int cliloc = (int) region.getProperties().getOrDefault("cliloc", 0);
            buf.writeByte(counter);
            buf.writeBytes(padString(region.getDisplayName(), 32, StandardCharsets.UTF_8));
            buf.writeBytes(padString(region.getParent().map(RegionNode::getDisplayName).orElse(""), 32, StandardCharsets.UTF_8));
            buf.writeInt(region.getArea().getCenter().getX()); // x
            buf.writeInt(region.getArea().getCenter().getY()); // y
            buf.writeInt(region.getArea().getCenter().getZ()); // z
            buf.writeInt(0); // map id
            buf.writeInt(cliloc); // Cliloc description
            buf.writeInt(0);
        }
        int flagValue = 0;
        for (CharacterListFlag flag : flags) {
            flagValue |= flag.getCode();
        }
        buf.writeInt(flagValue);
    }
}
