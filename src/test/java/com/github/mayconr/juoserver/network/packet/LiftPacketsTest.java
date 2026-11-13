package com.github.mayconr.juoserver.network.packet;

import com.github.mayconr.juoserver.game.model.LiftRejectReason;
import io.netty.buffer.Unpooled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.junit.jupiter.api.Assertions.assertEquals;

class LiftPacketsTest {

    @ParameterizedTest
    @EnumSource(LiftRejectReason.class)
    void liftRejectCarriesTheReasonCode(LiftRejectReason reason) {
        var buffer = Unpooled.buffer();
        try {
            new LiftReject(reason).writesTo(buffer);

            assertEquals(2, buffer.readableBytes());
            assertEquals(0x27, buffer.readUnsignedByte());
            assertEquals(reason.code(), buffer.readUnsignedByte());
        } finally {
            buffer.release();
        }
    }

    @Test
    void liftReasonsFollowTheProtocolOrder() {
        assertEquals(0, LiftRejectReason.CANNOT_LIFT.code());
        assertEquals(1, LiftRejectReason.OUT_OF_RANGE.code());
        assertEquals(2, LiftRejectReason.OUT_OF_SIGHT.code());
        assertEquals(3, LiftRejectReason.TRY_TO_STEAL.code());
        assertEquals(4, LiftRejectReason.ARE_HOLDING.code());
        assertEquals(5, LiftRejectReason.INSPECIFIC.code());
    }

    @Test
    void liftAmountIsReadUnsigned() {
        var buffer = Unpooled.buffer();
        buffer.writeByte(0x07);
        buffer.writeInt(0x40000001);
        buffer.writeShort(60000);
        try {
            assertEquals(60000, new UnequipItem(buffer).getAmount());
        } finally {
            buffer.release();
        }
    }
}
