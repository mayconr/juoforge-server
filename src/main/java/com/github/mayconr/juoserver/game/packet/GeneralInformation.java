package com.github.mayconr.juoserver.game.packet;

import com.github.mayconr.juoserver.game.server.AbstractPacket;

import io.netty.buffer.ByteBuf;

public class GeneralInformation extends AbstractPacket {

    public static final int CODE = (byte) 0xBF;
    private SubCommand subCommand;

    public GeneralInformation(ByteBuf buf) {
        super(CODE, computeLength(buf));
        int commandCode = buf.readShort();
        subCommand =
                switch (commandCode) {
                    case 5 -> new ScreenSize(buf);
                    case 12 -> new CloseStatusGump(buf);
                    default -> null;
                };
    }

    private static int computeLength(ByteBuf buf) {
        buf.readByte(); // CODE
        return buf.readShort();
    }

    public SubCommand getSubCommand() {
        return subCommand;
    }

    public interface SubCommand {}

    public static class ScreenSize implements SubCommand {
        private final int x;
        private final int y;

        public ScreenSize(ByteBuf buf) {
            buf.readShort(); // unknown
            this.x = buf.readShort();
            this.y = buf.readShort();
            buf.readShort(); // unknown
        }

        public int getX() {
            return x;
        }

        public int getY() {
            return y;
        }
    }

    public static class CloseStatusGump implements SubCommand {
        private final int serialId;

        public CloseStatusGump(ByteBuf buf) {
            this.serialId = buf.readInt();
        }

        public int getSerialId() {
            return serialId;
        }

        @Override
        public String toString() {
            return "CloseStatusGump{" + "serialId=" + serialId + '}';
        }
    }
}
