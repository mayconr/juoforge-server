package com.github.mayconr.juoserver.network.packet;

import com.github.mayconr.juoserver.game.model.TextType;
import io.netty.buffer.Unpooled;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import java.nio.charset.StandardCharsets;
import static org.junit.jupiter.api.Assertions.*;

class SendUnicodeSpeechTest {
    @ParameterizedTest
    @ValueSource(strings = {"Você encontrou minério: ação, coração.", "", "Hello", "Minério \uD83D\uDC8E"})
    void writesUnicodeTalkLayoutWithBigEndianTextAndTerminator(String text) {
        var packet = new SendUnicodeSpeech(TextType.BROADCAST, 0x03B2, 0, 0x0190, 3,
                "Miner", text, "PTB");
        var buf = Unpooled.buffer();
        try {
            packet.writesTo(buf);
            assertEquals(0xAE, buf.getUnsignedByte(0));
            assertEquals(50 + text.getBytes(StandardCharsets.UTF_16BE).length, buf.readableBytes());
            assertEquals(buf.readableBytes(), buf.getUnsignedShort(1));
            assertEquals(-1, buf.getInt(3));
            assertEquals(0x0190, buf.getUnsignedShort(7));
            assertEquals(TextType.BROADCAST.getCode(), buf.getUnsignedByte(9));
            assertEquals(0x03B2, buf.getUnsignedShort(10));
            assertEquals(3, buf.getUnsignedShort(12));
            assertEquals("PTB\0", buf.toString(14, 4, StandardCharsets.US_ASCII));
            assertEquals("Miner", buf.toString(18, 5, StandardCharsets.US_ASCII));
            for (int i = 23; i < 48; i++) assertEquals(0, buf.getByte(i));
            assertEquals(text, buf.toString(48, buf.readableBytes() - 50, StandardCharsets.UTF_16BE));
            assertEquals(0, buf.getUnsignedShort(buf.writerIndex() - 2));
        } finally {
            buf.release();
        }
    }
}
