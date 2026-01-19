package com.github.mayconr.juoserver.network.packet;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import com.github.mayconr.juoserver.game.model.TextType;
import com.github.mayconr.juoserver.game.model.UOMobile;
import com.github.mayconr.juoserver.game.model.UONpc;
import com.github.mayconr.juoserver.infrastructure.server.AbstractPacket;

import io.netty.buffer.ByteBuf;

public class SendSpeech extends AbstractPacket {

    public static final int CODE = (byte) 0x1C;
    private TextType type;
    private int hue;
    private int itemId;
    private int modelId;
    private int font;
    private String name;
    private String message;

    public SendSpeech(
            TextType type,
            int hue,
            int itemId,
            int modelId,
            int font,
            String name,
            String message) {
        super(CODE, 44 + message.length());
        this.type = type;
        this.hue = hue;
        this.itemId = itemId;
        this.modelId = modelId;
        this.font = font;
        this.name = name;
        this.message = message;
    }

    public SendSpeech(UONpc npc, String message) {
        this(
                TextType.NORMAL,
                npc.getSpeechHue(),
                npc.getSerialId(),
                0,
                npc.getSpeechFont(),
                npc.getName(),
                message);
    }

    public SendSpeech(UOMobile mobile, UnicodeSpeachRequest request) {
        this(
                request.getType(),
                request.getHue(),
                mobile.getSerialId(),
                mobile.getModelId(),
                request.getFont(),
                mobile.getName(),
                request.getText());
    }

    @Override
    public void writesTo(ByteBuf buf) {
        buf.writeByte(CODE);
        buf.writeShort(getLength());
        buf.writeInt(itemId);
        buf.writeShort(modelId);
        buf.writeByte(type.getCode());
        buf.writeShort(hue);
        buf.writeShort(font);
        buf.writeBytes(Arrays.copyOf(name.getBytes(StandardCharsets.UTF_8), 30));
        buf.writeCharSequence(message, StandardCharsets.UTF_8);
    }
}
