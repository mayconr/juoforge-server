package com.github.mayconr.juoserver.game.packet;

import java.net.InetAddress;
import java.nio.charset.StandardCharsets;

import com.github.mayconr.juoserver.game.server.AbstractPacket;

import io.netty.buffer.ByteBuf;

public class CreateCharacter extends AbstractPacket {

    public static final int CODE = (byte) 0x00;
    private final String characterName;
    private final int loginCount;
    private final int profession;
    private final int sex;
    private int strength;
    private int dexterity;
    private int inteligence;
    private int skill1;
    private int skill1Value;
    private int skill2;
    private int skill2Value;
    private int skill3;
    private int skill3Value;
    private short skinColor;
    private short hairStyle;
    private short hairColor;
    private short beardStyle;
    private short beardColor;
    private short locationIndex;
    private short slot;
    private InetAddress clientIp;
    private short shirtColor;
    private short pantsColor;

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
        this.sex = buf.readByte();
        this.strength = buf.readByte();
        this.dexterity = buf.readByte();
        this.inteligence = buf.readByte();
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

    public String getCharacterName() {
        return characterName;
    }

    public int getLoginCount() {
        return loginCount;
    }

    public int getProfession() {
        return profession;
    }

    public int getSex() {
        return sex;
    }

    public int getStrength() {
        return strength;
    }

    public int getDexterity() {
        return dexterity;
    }

    public int getInteligence() {
        return inteligence;
    }

    public int getSkill1() {
        return skill1;
    }

    public int getSkill1Value() {
        return skill1Value;
    }

    public int getSkill2() {
        return skill2;
    }

    public int getSkill2Value() {
        return skill2Value;
    }

    public int getSkill3() {
        return skill3;
    }

    public int getSkill3Value() {
        return skill3Value;
    }

    public short getSkinColor() {
        return skinColor;
    }

    public short getHairStyle() {
        return hairStyle;
    }

    public short getHairColor() {
        return hairColor;
    }

    public short getBeardStyle() {
        return beardStyle;
    }

    public short getBeardColor() {
        return beardColor;
    }

    public short getLocationIndex() {
        return locationIndex;
    }

    public short getSlot() {
        return slot;
    }

    public InetAddress getClientIp() {
        return clientIp;
    }

    public short getShirtColor() {
        return shirtColor;
    }

    public short getPantsColor() {
        return pantsColor;
    }

    @Override
    public String toString() {
        return "CreateCharacter{" + "characterName='" + characterName + '\'' + '}';
    }
}
