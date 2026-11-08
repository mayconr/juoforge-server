package com.github.mayconr.juoserver.network;

import com.github.mayconr.juoserver.game.model.UOMobileData;
import com.github.mayconr.juoserver.game.model.UOPlayer;
import com.github.mayconr.juoserver.network.packet.GumpSelection;
import com.github.mayconr.juoserver.network.packet.SendGumpDialog;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.CorruptedFrameException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

class GumpPacketTest {
    private ByteBuf header() {
        return Unpooled.buffer().writeByte(0xB1).writeShort(0).writeInt(1).writeInt(2).writeInt(3);
    }

    @Test
    void preservesTextIdsIndependentlyOfTheirOrder() {
        var buf = header();
        try {
            buf.writeInt(1).writeInt(7).writeInt(2);
            buf.writeShort(42).writeShort(3).writeCharSequence("one", StandardCharsets.UTF_16BE);
            buf.writeShort(10).writeShort(3).writeCharSequence("two", StandardCharsets.UTF_16BE);
            buf.setShort(1, buf.writerIndex());
            var response = new GumpSelection(buf);
            assertEquals("one", response.getTextById(42));
            assertEquals("two", response.getTextById(10));
            assertEquals("", response.getTextById(99));
            assertEquals("one", response.getText(0));
            assertTrue(response.isSwitchChecked(7));
            assertThrows(UnsupportedOperationException.class, () -> response.getTextEntries().add("extra"));
        } finally { buf.release(); }
    }

    @ParameterizedTest
    @ValueSource(ints = {-1, Integer.MAX_VALUE})
    void rejectsSwitchCountsOutsideThePacket(int count) {
        var buf = header();
        try {
            buf.writeInt(count).writeInt(0).setShort(1, 23);
            assertThrows(CorruptedFrameException.class, () -> new GumpSelection(buf));
        } finally { buf.release(); }
    }

    @Test
    void rejectsTruncatedAndOversizedText() {
        for (int length : new int[]{2, 240}) {
            var buf = header();
            try {
                buf.writeInt(0).writeInt(1).writeShort(42).writeShort(length).writeShort('x');
                buf.setShort(1, buf.writerIndex());
                assertThrows(CorruptedFrameException.class, () -> new GumpSelection(buf));
            } finally { buf.release(); }
        }
    }

    @Test
    void rejectsInvalidTextCountsAndDuplicateIds() {
        for (int count : new int[]{-1, Integer.MAX_VALUE}) {
            var buf = header();
            try {
                buf.writeInt(0).writeInt(count).setShort(1, 23);
                assertThrows(CorruptedFrameException.class, () -> new GumpSelection(buf));
            } finally { buf.release(); }
        }
        var buf = header();
        try {
            buf.writeInt(0).writeInt(2);
            buf.writeShort(42).writeShort(0).writeShort(42).writeShort(0);
            buf.setShort(1, buf.writerIndex());
            assertThrows(CorruptedFrameException.class, () -> new GumpSelection(buf));
        } finally { buf.release(); }
    }

    @Test
    void acceptsAnUnsignedPacketLengthAbove32767() {
        var buf = header();
        try {
            buf.writeInt(0).writeInt(70);
            for (int id = 0; id < 70; id++) {
                buf.writeShort(id).writeShort(239);
                buf.writeCharSequence("x".repeat(239), StandardCharsets.UTF_16BE);
            }
            int length = buf.writerIndex();
            buf.setShort(1, length);
            var response = new GumpSelection(buf);
            assertEquals(length, response.getLength());
            assertEquals("x".repeat(239), response.getTextById(69));
        } finally { buf.release(); }
    }

    @Test
    void enforcesTotalPacketSizeAndSnapshotsText() {
        var player = new UOPlayer(new UOMobileData());
        assertThrows(IllegalArgumentException.class,
                () -> new SendGumpDialog(player, 1, 0, 0, "x".repeat(65535), List.of()));
        assertThrows(IllegalArgumentException.class,
                () -> new SendGumpDialog(player, 1, 0, 0, "", List.of("x".repeat(32756))));
        var maximum = new SendGumpDialog(player, 1, 0, 0, "x".repeat(65512), List.of());
        assertEquals(65535, maximum.getLength());
        var texts = new ArrayList<>(List.of("one"));
        var packet = new SendGumpDialog(player, 1, 0, 0, "{page 0}", texts);
        texts.add("two");
        var buffer = Unpooled.buffer();
        try {
            packet.writesTo(buffer);
            assertEquals(packet.getLength(), buffer.readableBytes());
            assertEquals(packet.getLength(), buffer.getUnsignedShort(1));
        } finally { buffer.release(); }
    }
}
