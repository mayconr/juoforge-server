package com.github.mayconr.juoserver.network.packet;

import com.github.mayconr.juoserver.game.model.SendSkillType;
import com.github.mayconr.juoserver.game.model.SkillLock;
import com.github.mayconr.juoserver.game.model.SkillValue;
import com.github.mayconr.juoserver.game.model.UOMobile;
import com.github.mayconr.juoserver.infrastructure.server.AbstractPacket;
import io.netty.buffer.ByteBuf;
import lombok.Getter;

import java.util.Collection;
import java.util.List;

@Getter
public class SendSkill extends AbstractPacket {

    public static final int CODE = 0x3A;

    private final SendSkillType type;
    private final Collection<SkillValue> skills;

    public SendSkill(SendSkillType type, Collection<SkillValue> skills) {
        super(CODE, computeLength((byte) type.getCode(), skills));
        this.type = type;
        this.skills = skills;
    }

    public SendSkill(UOMobile mobile) {
        super(CODE, computeLength((byte) SendSkillType.SINGLE_UPDATE_WITH_CAP.getCode(), mobile.getSkills().skills()));
        this.type = SendSkillType.SINGLE_UPDATE_WITH_CAP;
        this.skills = mobile.getSkills().skills();
    }

    public SendSkill(ByteBuf buf) {
        super(CODE, -1);
        buf.readByte(); // CODE
        int length = buf.readUnsignedShort(); // LENGTH
        if (length != 6) {
            throw new IllegalStateException(
                    "Invalid SendSkill client packet length: " + length
            );
        }

        int skillId = buf.readUnsignedShort();
        SkillLock lock = SkillLock.fromCode(buf.readUnsignedByte());

        this.type = null;
        final var skill = SkillValue.of(skillId, SkillLock.UP);
        skill.setLock(lock);

        this.skills = List.of(skill);
    }

    @Override
    public void writesTo(ByteBuf buf) {
        byte rawType = (byte) type.getCode();

        buf.writeByte(getCode());
        buf.writeShort(getLength());
        buf.writeByte(rawType);

        for (SkillValue skill : skills) {
            buf.writeShort(skill.getSkillId());
            buf.writeShort((int) skill.getValue() * 10);
            buf.writeShort((int) skill.getBase() * 10);
            buf.writeByte(skill.getLock().getCode());

            if (rawType == 0x02 || (rawType & 0xFF) == 0xDF) {
                buf.writeShort((int) skill.getCap() * 10);
            }
        }

        // Null-terminated ONLY for full list
        if (rawType == 0x00) {
            buf.writeShort(0x0000);
        }
    }

    private static int computeLength(byte type, Collection<SkillValue> skills) {
        int length = 1 + 2 + 1; // command + length + type

        int perSkill =
                2 + // skillId
                        2 + // value
                        2 + // base value
                        1;  // lock

        if (type == 0x02 || (type & 0xFF) == 0xDF) {
            perSkill += 2; // cap
        }

        length += skills.size() * perSkill;

        if (type == 0x00) {
            length += 2; // null terminator
        }

        return length;
    }
}
