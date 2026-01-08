package com.github.mayconr.juoserver.game.packet;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import com.github.mayconr.juoserver.game.server.AbstractPacket;

import io.netty.buffer.ByteBuf;
import lombok.Getter;
import lombok.ToString;

@ToString
@Getter
public class GumpSelection extends AbstractPacket {
    public static final int CODE = (byte) 0xB1;

    private final int serialId;
    private final int gumpId;
    private final int buttonId;

    private final List<Integer> switches = new ArrayList<>();
    private final List<String> textEntries = new ArrayList<>();

    /** Packet recebido do cliente */
    public GumpSelection(ByteBuf buf) {
        super(CODE, calculateLength(buf));

        // sender serial
        serialId = buf.readInt();

        // gump id
        gumpId = buf.readInt();

        // button pressed
        buttonId = buf.readInt();

        // switches (radio / checkbox)
        int switchesCount = buf.readInt();
        for (int i = 0; i < switchesCount; i++) {
            switches.add(buf.readInt());
        }

        // text entries
        int textCount = buf.readInt();
        for (int i = 0; i < textCount; i++) {
            buf.readUnsignedShort(); // Unused
            final var len = buf.readUnsignedShort();
            final var text =
                    len > 0
                            ? buf.readCharSequence(len * 2, StandardCharsets.UTF_16BE).toString()
                            : "";
            textEntries.add(text);
        }
    }

    public static int calculateLength(ByteBuf buf) {
        buf.readByte(); // Ignore code
        return buf.readShort();
    }

    public boolean isSwitchChecked(int switchId) {
        return switches.contains(switchId);
    }

    public String getText(int index) {
        if (index < 0 || index >= textEntries.size()) {
            return "";
        }
        return textEntries.get(index);
    }
}
