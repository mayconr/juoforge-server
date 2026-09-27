package com.github.mayconr.juoserver.game.packet;

import com.github.mayconr.juoserver.game.model.SpellbookType;
import com.github.mayconr.juoserver.network.packet.*;
import io.netty.buffer.Unpooled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.HexFormat;

import static org.junit.jupiter.api.Assertions.*;

class NewSpellbookTest {
    @Test
    void serializesExactHealOnlyPacket() {
        var command = new NewSpellbookExtendedCommand(0x40001234, 0x0EFA, SpellbookType.MAGERY, 1L << 3);
        var packet = new GeneralInformation(command);
        var buffer = Unpooled.buffer();
        try {
            packet.writesTo(buffer);
            var actual = new byte[buffer.readableBytes()];
            buffer.readBytes(actual);
            assertEquals(23, packet.getLength());
            assertEquals(0x001B, packet.getSubCommand());
            assertArrayEquals(HexFormat.of().parseHex("bf0017001b0001400012340efa00010800000000000000"), actual);
            assertThrows(IllegalStateException.class, packet::getCommand);
        } finally {
            buffer.release();
        }
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 8, 9, 32, 33, 64})
    void maskMatchesClassicUOByteAndBitOrder(int position) {
        var packet = new GeneralInformation(new NewSpellbookExtendedCommand(0x40000001, 0x0EFA, 1, 1L << (position - 1)));
        var buffer = Unpooled.buffer();
        try {
            packet.writesTo(buffer);
            for (int i = 0; i < 8; i++) {
                int expected = i == (position - 1) / 8 ? 1 << ((position - 1) % 8) : 0;
                assertEquals(expected, buffer.getUnsignedByte(15 + i));
            }
        } finally {
            buffer.release();
        }
    }

    @ParameterizedTest
    @ValueSource(longs = {0L, -1L})
    void serializesEmptyAndFullBooksAtNonzeroWriterIndex(long mask) {
        var buffer = Unpooled.buffer();
        try {
            buffer.writeByte(42);
            new GeneralInformation(new NewSpellbookExtendedCommand(0x40000001, 0x2253, 101, mask)).writesTo(buffer);
            assertEquals(24, buffer.writerIndex());
            assertEquals(101, buffer.getUnsignedShort(14));
            for (int i = 0; i < 8; i++) {
                assertEquals(mask == 0 ? 0 : 255, buffer.getUnsignedByte(16 + i));
            }
        } finally {
            buffer.release();
        }
    }

    @Test
    void preservesIncomingSpellSelection() {
        var input = Unpooled.wrappedBuffer(HexFormat.of().parseHex("bf0009001c00020004"));
        var output = Unpooled.buffer();
        try {
            var packet = new GeneralInformation(input);
            var spell = assertInstanceOf(SpellSelectionExtendedCommand.class, packet.getCommand());
            assertEquals(4, spell.spellId());
            assertEquals(0, input.readableBytes());
            assertThrows(UnsupportedOperationException.class, () -> packet.writesTo(output));
            assertEquals(0, output.writerIndex());
        } finally {
            input.release();
            output.release();
        }
    }

    @Test
    void rejectsTruncatedFieldValues() {
        assertThrows(IllegalArgumentException.class, () -> new NewSpellbookExtendedCommand(1, 65536, 1, 0));
        assertThrows(IllegalArgumentException.class, () -> new NewSpellbookExtendedCommand(1, -1, 1, 0));
        assertThrows(IllegalArgumentException.class, () -> new NewSpellbookExtendedCommand(1, 1, 0, 0));
        assertThrows(IllegalArgumentException.class, () -> new NewSpellbookExtendedCommand(1, 1, 65536, 0));
    }
}
