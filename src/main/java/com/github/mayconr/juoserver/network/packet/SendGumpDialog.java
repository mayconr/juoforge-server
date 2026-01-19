package com.github.mayconr.juoserver.network.packet;

import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Objects;

import com.github.mayconr.juoserver.game.model.UOMobile;
import com.github.mayconr.juoserver.infrastructure.server.AbstractPacket;

import io.netty.buffer.ByteBuf;
import io.netty.util.CharsetUtil;

public class SendGumpDialog extends AbstractPacket {

    public static final int CODE = 0xB0;
    private final UOMobile mobile;
    private final int gumpId;
    private final int x;
    private final int y;
    private final String
            layout; // ex: "page 0\nresizepic 0 0 5054 200 150\nbutton 20 20 4005 4007 1 0 1\ntext
    // 50 50 0 0"
    private final List<String> texts; // textos referenciados pelo layout (indices 0..N-1)

    public SendGumpDialog(
            UOMobile mobile, int gumpId, int x, int y, String layout, List<String> texts) {
        super(CODE, computeLength(Objects.requireNonNull(layout), Objects.requireNonNull(texts)));
        this.mobile = Objects.requireNonNull(mobile);
        this.gumpId = gumpId;
        this.x = x;
        this.y = y;
        this.layout = Objects.requireNonNull(layout);
        this.texts = Objects.requireNonNull(texts);
    }

    /** Calcula o tamanho total do pacote (incluindo opcode e length). */
    private static int computeLength(String layout, List<String> texts) {
        Objects.requireNonNull(layout);
        Objects.requireNonNull(texts);

        // opcode(1) + size(2) + sender(4) + gumpId(4) + x(4) + y(4)
        // + cmdLen(2)
        int len = 1 + 2 + 4 + 4 + 4 + 4 + 2;

        byte[] layoutBytes = layout.getBytes(CharsetUtil.US_ASCII);
        if (layoutBytes.length > 0xFFFF) {
            throw new IllegalArgumentException("Layout excede 65535 bytes");
        }
        len += layoutBytes.length;

        // textLinesCount (2)
        if (texts.size() > 0xFFFF) {
            throw new IllegalArgumentException("Quantidade de textos excede ushort");
        }
        len += 2;

        // textos Unicode
        for (String s : texts) {
            Objects.requireNonNull(s);

            byte[] utf16 = s.getBytes(StandardCharsets.UTF_16BE);
            int charCount = utf16.length / 2;

            if (charCount > 0xFFFF) {
                throw new IllegalArgumentException("Texto Unicode excede ushort");
            }

            // length(2) + dados UTF-16BE
            len += 2 + utf16.length;
        }

        return len;
    }

    @Override
    public void writesTo(ByteBuf buf) {
        // Garantia explícita de BIG-ENDIAN
        buf = buf.order(ByteOrder.BIG_ENDIAN);

        // opcode + tamanho
        buf.writeByte(CODE);
        buf.writeShort(getLength());

        // cabeçalho
        buf.writeInt(mobile.getSerialId()); // serial UO do mobile
        buf.writeInt(gumpId);
        buf.writeInt(x);
        buf.writeInt(y);

        // layout (cmd)
        byte[] cmd = layout.getBytes(CharsetUtil.US_ASCII);
        buf.writeShort(cmd.length);
        buf.writeBytes(cmd);

        // textos
        buf.writeShort(texts.size());
        for (String s : texts) {
            byte[] utf16 = s.getBytes(StandardCharsets.UTF_16BE);
            buf.writeShort(utf16.length / 2); // número real de chars
            buf.writeBytes(utf16);
        }
    }
}
