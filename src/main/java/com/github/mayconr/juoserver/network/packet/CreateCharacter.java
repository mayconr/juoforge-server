package com.github.mayconr.juoserver.network.packet;

import java.net.InetAddress;
import java.nio.charset.StandardCharsets;

import com.github.mayconr.juoserver.game.model.Gender;
import com.github.mayconr.juoserver.infrastructure.server.AbstractPacket;

import io.netty.buffer.ByteBuf;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public class CreateCharacter extends AbstractPacket {

    public static final int CODE = (byte) 0x00;
    private final String characterName;
    private final int loginCount;
    private final int profession;
    private final Gender gender;
    private final int strength;
    private final int dexterity;
    private final int intelligence;
    private final int skill1;
    private final int skill1Value;
    private final int skill2;
    private final int skill2Value;
    private final int skill3;
    private final int skill3Value;
    private final short skinColor;
    private final short hairStyle;
    private final short hairColor;
    private final short beardStyle;
    private final short beardColor;
    private final short locationIndex;
    private final short slot;
    private final InetAddress clientIp;
    private final short shirtColor;
    private final short pantsColor;

    public CreateCharacter(ByteBuf buf) {
        super(CODE, 104);
        buf.readByte(); // CODE
        buf.readBytes(4); // pattern1 (0xedededed)
        buf.readBytes(4); // pattern2 (0xffffffff)
        buf.readByte(); // pattern3 (0x00)
        this.characterName = readStringTrailingZeros(buf, 30, StandardCharsets.UTF_8);
        buf.readBytes(2); // unknown0
        buf.readInt(); // flag
        buf.readBytes(4); // unknown1
        this.loginCount = buf.readInt();
        this.profession = buf.readByte();
        buf.readBytes(15); // unknown2
        this.gender = Gender.fromCode(buf.readByte());
        this.strength = buf.readByte();
        this.dexterity = buf.readByte();
        this.intelligence = buf.readByte();
        this.skill1 = buf.readByte();
        this.skill1Value = buf.readByte();
        this.skill2 = buf.readByte();
        this.skill2Value = buf.readByte();
        this.skill3 = buf.readByte();
        this.skill3Value = buf.readByte();
        this.skinColor = buf.readShort();
        this.hairStyle = buf.readShort();
        this.hairColor = buf.readShort();
        this.beardStyle = buf.readShort();
        this.beardColor = buf.readShort();
        this.locationIndex = buf.readShort();
        buf.readBytes(2); // unknown3
        this.slot = buf.readShort();
        this.clientIp = readInetAddress(buf);
        this.shirtColor = buf.readShort();
        this.pantsColor = buf.readShort();
    }
}
