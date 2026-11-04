package com.github.mayconr.juoserver.network.packet;

import com.github.mayconr.juoserver.game.model.TextType;
import com.github.mayconr.juoserver.game.model.event.MobileSpeech;
import com.github.mayconr.juoserver.infrastructure.server.AbstractPacket;
import io.netty.buffer.ByteBuf;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

/** Server Unicode speech (0xAE), read by ClassicUO's UnicodeTalk handler. */
public final class SendUnicodeSpeech extends AbstractPacket {
    public static final int CODE = 0xAE;
    private final TextType type;
    private final int hue;
    private final int serialId;
    private final int modelId;
    private final int font;
    private final String name;
    private final String language;
    private final byte[] text;

    public SendUnicodeSpeech(TextType type, int hue, int serialId, int modelId, int font,
                             String name, String message, String language) {
        this(type, hue, serialId, modelId, font, name,
                message.getBytes(StandardCharsets.UTF_16BE), language);
    }

    private SendUnicodeSpeech(TextType type, int hue, int serialId, int modelId, int font,
                              String name, byte[] text, String language) {
        super(CODE, 50 + text.length);
        if (getLength() > 0xFFFF) {
            throw new IllegalArgumentException("Unicode speech exceeds packet length limit");
        }
        this.type = type;
        this.hue = hue;
        this.serialId = serialId;
        this.modelId = modelId;
        this.font = font;
        this.name = name == null ? "" : name;
        this.language = language == null ? "ENU" : language;
        this.text = text;
    }

    public SendUnicodeSpeech(MobileSpeech event, String language) {
        this(event.context().type(), event.context().hue(), event.mobile().getSerialId(),
                event.mobile().getModelId(), event.context().font(), event.mobile().getDisplayName(),
                event.message(), language);
    }

    @Override
    public void writesTo(ByteBuf buf) {
        buf.writeByte(CODE);
        buf.writeShort(getLength());
        buf.writeInt(serialId == 0 ? 0xFFFFFFFF : serialId);
        buf.writeShort(modelId);
        buf.writeByte(type.getCode());
        buf.writeShort(hue);
        buf.writeShort(font);
        buf.writeBytes(Arrays.copyOf(language.getBytes(StandardCharsets.US_ASCII), 3));
        buf.writeByte(0);
        buf.writeBytes(Arrays.copyOf(name.getBytes(StandardCharsets.US_ASCII), 30));
        buf.writeBytes(text);
        buf.writeShort(0);
    }
}
