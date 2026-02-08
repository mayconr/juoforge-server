package com.github.mayconr.juoserver.network.packet;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import com.github.mayconr.juoserver.game.model.TextType;
import com.github.mayconr.juoserver.game.model.UOMobile;
import com.github.mayconr.juoserver.game.model.event.MobileSpeech;
import com.github.mayconr.juoserver.infrastructure.server.AbstractPacket;

import io.netty.buffer.ByteBuf;

public class SendSpeech extends AbstractPacket {

    public static final int CODE = (byte) 0x1C;
    private final TextType type;
    private final int hue;
    private final int serialId;
    private final int modelId;
    private final int font;
    private final String name;
    private final String message;

    public SendSpeech(
            TextType type,
            int hue,
            int serialId,
            int modelId,
            int font,
            String name,
            String message) {
        super(CODE, 44 + message.length());
        this.type = type;
        this.hue = hue;
        this.serialId = serialId;
        this.modelId = modelId;
        this.font = font;
        this.name = name;
        this.message = message;
    }

    public SendSpeech(UOMobile mobile, UnicodeSpeachRequest request) {
        this(
                request.getType(),
                request.getHue(),
                mobile.getSerialId(),
                mobile.getModelId(),
                request.getFont(),
                mobile.getDisplayName(),
                request.getText());
    }

    public SendSpeech(MobileSpeech mobileSpeech) {
        this(
                mobileSpeech.context().type(),
                mobileSpeech.context().hue(),
                mobileSpeech.player().getSerialId(),
                mobileSpeech.player().getModelId(),
                mobileSpeech.context().font(),
                mobileSpeech.player().getDisplayName(),
                mobileSpeech.message());
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
        buf.writeBytes(Arrays.copyOf(name.getBytes(StandardCharsets.UTF_8), 30));
        buf.writeCharSequence(message, StandardCharsets.UTF_8);
    }
}
