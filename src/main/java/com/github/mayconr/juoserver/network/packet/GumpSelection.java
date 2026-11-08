package com.github.mayconr.juoserver.network.packet;

import com.github.mayconr.juoserver.infrastructure.server.AbstractPacket;
import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.CorruptedFrameException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import lombok.ToString;

@ToString
@Getter
public class GumpSelection extends AbstractPacket {
    public static final int CODE = (byte) 0xB1;
    public static final int MAX_TEXT_LENGTH = 239;

    private final int serialId;
    private final int gumpId;
    private final int buttonId;
    private final List<Integer> switches;
    private final List<String> textEntries;
    private final Map<Integer, String> textEntriesById;

    public GumpSelection(ByteBuf buf) {
        super(CODE, calculateLength(buf));
        if (getLength() < 23 || buf.readableBytes() != getLength() - 3) {
            throw new CorruptedFrameException("Invalid gump response length");
        }
        serialId = buf.readInt();
        gumpId = buf.readInt();
        buttonId = buf.readInt();
        long switchesCount = buf.readUnsignedInt();
        if (switchesCount > (buf.readableBytes() - 4) / 4) {
            throw new CorruptedFrameException("Invalid gump switch count");
        }
        var selected = new ArrayList<Integer>();
        for (int i = 0; i < switchesCount; i++) selected.add(buf.readInt());
        switches = List.copyOf(selected);
        long textCount = buf.readUnsignedInt();
        if (textCount > buf.readableBytes() / 4) {
            throw new CorruptedFrameException("Invalid gump text count");
        }
        var texts = new LinkedHashMap<Integer, String>();
        for (int i = 0; i < textCount; i++) {
            if (buf.readableBytes() < 4) throw new CorruptedFrameException("Truncated gump text header");
            int id = buf.readUnsignedShort();
            int length = buf.readUnsignedShort();
            if (length > MAX_TEXT_LENGTH || length > buf.readableBytes() / 2) {
                throw new CorruptedFrameException("Invalid gump text length");
            }
            String text = buf.readCharSequence(length * 2, StandardCharsets.UTF_16BE).toString();
            if (texts.putIfAbsent(id, text) != null) {
                throw new CorruptedFrameException("Duplicate gump text entry ID");
            }
        }
        if (buf.isReadable()) throw new CorruptedFrameException("Unexpected gump response data");
        textEntries = List.copyOf(texts.values());
        textEntriesById = Map.copyOf(texts);
    }

    public static int calculateLength(ByteBuf buf) {
        if (buf.readableBytes() < 3) throw new CorruptedFrameException("Truncated gump response header");
        buf.readByte();
        return buf.readUnsignedShort();
    }

    public boolean isSwitchChecked(int switchId) {
        return switches.contains(switchId);
    }

    /** Looks up a text field by the entry ID declared in the gump layout. */
    public String getTextById(int entryId) {
        return textEntriesById.getOrDefault(entryId, "");
    }

    /** Retains positional access for existing handlers. Prefer getTextById for forms. */
    public String getText(int index) {
        return index < 0 || index >= textEntries.size() ? "" : textEntries.get(index);
    }
}
