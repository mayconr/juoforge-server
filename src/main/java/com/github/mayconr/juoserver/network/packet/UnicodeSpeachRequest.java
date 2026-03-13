package com.github.mayconr.juoserver.network.packet;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.github.mayconr.juoserver.game.model.TextType;
import com.github.mayconr.juoserver.infrastructure.server.AbstractPacket;

import io.netty.buffer.ByteBuf;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public class UnicodeSpeachRequest extends AbstractPacket {

    public static final int CODE = (byte) 0xAD;
    private static final int ENCODED_BITS = 0xC0;
    private final TextType type;
    private final int hue;
    private final int font;
    private final String language;
    private final String text;
    private final int flags;
    private final int numTriggerWords;
    private final List<Integer> triggerWords;

    public UnicodeSpeachRequest(ByteBuf buf) {
        super(CODE, calculateLength(buf));
        final var typeByte = buf.readByte();
        this.flags = typeByte & ENCODED_BITS;
        this.type = TextType.byCode(typeByte & 0x3F); // clean 6 and 7 bits
        this.hue = buf.readUnsignedShort();
        this.font = buf.readUnsignedShort();
        this.language = readStringTrailingZeros(buf, 4, StandardCharsets.UTF_8);

        if ((typeByte & ENCODED_BITS) != 0) {
            // encoded — read trigger words

            // number of trigger words - 12 bits (bits 11..4 = byte 13, bits 7..4 of byte 14 are
            // bits 0..3)
            int numWordsHigh = buf.readUnsignedByte(); // byte 13
            int numWordsLow = buf.readUnsignedByte(); // byte 14

            this.numTriggerWords = ((numWordsHigh << 4) & 0xFF0) | ((numWordsLow >> 4) & 0xF);

            // trigger words indexes - each 12 bits
            // Byte 14 (low nibble) and byte 15
            int indexHighNibble = numWordsLow & 0x0F;
            int indexLowByte = buf.readUnsignedByte(); // byte 15
            int firstIndex = (indexHighNibble << 8) | indexLowByte;

            this.triggerWords = new ArrayList<>();
            this.triggerWords.add(firstIndex);

            // Calculates how many bytes remain to be read for the extra trigger words (if any)
            int unknownBytes = ((numTriggerWords / 2) * 3) + (numTriggerWords % 2) - 1;

            // We have already read 3 bytes for the words (bytes 13, 14, 15)
            // Reads the remaining unknown bytes of the protocol (client hardcoded)
            for (int i = 0; i < unknownBytes; i++) {
                buf.readByte(); // descarta, pois são dados desconhecidos
            }

            // Now reads the ASCII null-terminated message (rest of the packet)
            this.text = readNullTerminatedAsciiString(buf);

        } else {
            // Normal type - reads Unicode null-terminated message (UTF-16BE)
            this.numTriggerWords = 0;
            this.triggerWords = Collections.emptyList();
            this.text = readNullTerminatedUnicodeString(buf);
        }
    }

    private static int calculateLength(ByteBuf buf) {
        buf.readByte(); // CODE
        return buf.readUnsignedShort();
    }

    public boolean isEncrypted() {
        return (flags & 0x80) != 0;
    }

    public boolean isPrivate() {
        return (flags & 0x40) != 0;
    }
}
