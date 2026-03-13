package com.github.mayconr.juoserver.network.packet;

import java.nio.charset.StandardCharsets;

import com.github.mayconr.juoserver.game.model.Gender;
import com.github.mayconr.juoserver.game.model.Race;
import com.github.mayconr.juoserver.game.model.UOMobile;
import com.github.mayconr.juoserver.infrastructure.server.AbstractPacket;

import io.netty.buffer.ByteBuf;

public class StatusBarInfo extends AbstractPacket {

    public static final int CODE = (byte) 0x11;

    private final UOMobile mobile;

    public StatusBarInfo(UOMobile mobile) {
        super(CODE, calculateLength(mobile));
        this.mobile = mobile;
    }

    @Override
    public void writesTo(ByteBuf buf) {
        buf.writeByte(CODE); // 1

        buf.writeShort(getLength()); // 3

        buf.writeInt(mobile.getSerialId()); // 7

        buf.writeBytes(padString(mobile.getDisplayName(), 30, StandardCharsets.US_ASCII)); // 37
        // writeFixedString(buf, player.getName(), 30);

        buf.writeShort(mobile.getHitpoints()); // 39
        buf.writeShort(mobile.getMaxHitpoints()); // 41

        buf.writeByte(1); // Name Change Flag (0 = no rename allowed) 42
        buf.writeByte(0x06); // Status Flag (UOML+) 43
        buf.writeByte(getSexRaceByte(mobile)); // Sex + Race 44

        buf.writeShort(mobile.getStrength()); // 46
        buf.writeShort(mobile.getDexterity()); // 48
        buf.writeShort(mobile.getIntelligence()); // 50

        buf.writeShort(mobile.getStamina()); // 52
        buf.writeShort(mobile.getMaxStamina()); // 54

        buf.writeShort(mobile.getMana()); // 56
        buf.writeShort(mobile.getMaxMana()); // 58

        buf.writeInt(mobile.getGold()); // 62

        buf.writeShort(mobile.getPhysicalResist()); // Acts as Armor Rating if AOS enabled 64
        buf.writeShort(mobile.getWeight()); // 66

        // UOML+ extra fields
        buf.writeShort(mobile.getMaxWeight()); // 68
        buf.writeByte(getRaceByte(mobile)); // Race flag: 1 = human, 2 = elf, 3 = gargoyle 69

        // UOR+ fields
        buf.writeShort(mobile.getStatCap()); // 71
        buf.writeByte(mobile.getFollowers()); // 72
        buf.writeByte(mobile.getMaxFollowers()); // 73

        // AOS+ resists
        buf.writeShort(encodeResist(mobile.getFireResist())); // 75
        buf.writeShort(encodeResist(mobile.getColdResist())); // 77
        buf.writeShort(encodeResist(mobile.getPoisonResist())); // 79
        buf.writeShort(encodeResist(mobile.getEnergyResist())); // 81
        buf.writeShort(mobile.getLuck()); // 83
        buf.writeShort(mobile.getDamageMin()); // 85
        buf.writeShort(mobile.getDamageMax()); // 87
        buf.writeInt(mobile.getTithingPoints()); // 91

        // UOKR+ attributes
        buf.writeShort(mobile.getMaxPhysicalResist());
        buf.writeShort(mobile.getMaxFireResist());
        buf.writeShort(mobile.getMaxColdResist());
        buf.writeShort(mobile.getMaxPoisonResist());
        buf.writeShort(mobile.getMaxEnergyResist());
        buf.writeShort(mobile.getDefenseChanceIncrease());
        buf.writeShort(mobile.getMaxDefenseChanceIncrease());
        buf.writeShort(mobile.getHitChanceIncrease());
        buf.writeShort(mobile.getSwingSpeedIncrease());
        buf.writeShort(mobile.getWeaponDamageIncrease());
        buf.writeShort(mobile.getLowerReagentCost());
        buf.writeShort(mobile.getSpellDamageIncrease());
        buf.writeShort(mobile.getFasterCastRecovery());
        buf.writeShort(mobile.getFasterCasting());
        buf.writeShort(mobile.getLowerManaCost());
    }

    private static int calculateLength(UOMobile player) {
        return 121; // 163 bytes (standard for flag 0x05)
    }

    private static byte getSexRaceByte(UOMobile p) {
        if (p.getRace().equals(Race.ELF)) {
            return p.getGender().equals(Gender.HUMAN_FEMALE) ? (byte) 3 : (byte) 2;
        }
        return p.getGender().equals(Gender.HUMAN_FEMALE) ? (byte) 1 : (byte) 0;
    }

    private static byte getRaceByte(UOMobile p) {
        return switch (p.getRace()) {
            case ELF -> 2;
            case GARGOYLE -> 3;
            default -> 1;
        };
    }

    private static short encodeResist(int value) {
        return (short) (value >= 0 ? value : 0x10000 + value); // Encode negatives
    }
}
