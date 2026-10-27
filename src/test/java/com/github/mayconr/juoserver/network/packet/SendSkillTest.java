package com.github.mayconr.juoserver.network.packet;

import com.github.mayconr.juoserver.game.model.SendSkillType;
import com.github.mayconr.juoserver.game.model.SkillLock;
import com.github.mayconr.juoserver.game.model.SkillValue;
import io.netty.buffer.Unpooled;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SendSkillTest {
    @ParameterizedTest
    @EnumSource(SendSkillType.class)
    void encodesTheSelectedTypeWithItsIdConventionAndCap(SendSkillType type) {
        var skill = SkillValue.of(0, 20, 100, SkillLock.UP);
        var packet = new SendSkill(type, List.of(skill));
        var buffer = Unpooled.buffer();
        try {
            packet.writesTo(buffer);
            int actualLength = buffer.readableBytes();
            boolean caps = type == SendSkillType.FULL_LIST_WITH_CAP
                    || type == SendSkillType.FULL_LIST_WITH_CAP_AND_OPEN
                    || type == SendSkillType.SINGLE_UPDATE_WITH_CAP;
            boolean legacyList = type == SendSkillType.FULL_LIST || type == SendSkillType.FULL_LIST_WITH_CAP;
            assertEquals(0x3A, buffer.readUnsignedByte());
            assertEquals(actualLength, buffer.readUnsignedShort());
            assertEquals(type.getCode(), buffer.readUnsignedByte());
            assertEquals(legacyList ? 1 : 0, buffer.readUnsignedShort());
            assertEquals(200, buffer.readUnsignedShort());
            assertEquals(200, buffer.readUnsignedShort());
            assertEquals(SkillLock.UP.getCode(), buffer.readUnsignedByte());
            if (caps) assertEquals(1000, buffer.readUnsignedShort());
            if (type == SendSkillType.FULL_LIST) assertEquals(0, buffer.readUnsignedShort());
            assertEquals(0, buffer.readableBytes());
        } finally {
            buffer.release();
        }
    }
}
