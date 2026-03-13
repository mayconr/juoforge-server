package com.github.mayconr.juoserver.network.packet;

import com.github.mayconr.juoserver.infrastructure.server.AbstractPacket;

import io.netty.buffer.ByteBuf;
import lombok.Getter;

@Getter
public class GeneralInformation extends AbstractPacket {

    public static final int CODE = 0xBF;

    private final int subCommandCode;
    private final SubCommand subCommand;

    public GeneralInformation(ByteBuf buf, int length) {
        super(CODE, length);

        this.subCommandCode = buf.readUnsignedShort();

        this.subCommand = switch (subCommandCode) {
            case 0x0005 -> new ScreenSize(buf);
            case 0x000C -> new CloseStatusGump(buf);
            default     -> new UnknownSubCommand(subCommandCode, buf);
        };
    }

    /* ================= SUBCOMMANDS ================= */

    public interface SubCommand {}

    /**
     * SubCommand 0x0005
     * Client informs screen resolution
     */
    @Getter
    public static final class ScreenSize implements SubCommand {

        private final int width;
        private final int height;

        public ScreenSize(ByteBuf buf) {
            buf.readUnsignedShort(); // unknown / flags
            this.width  = buf.readUnsignedShort();
            this.height = buf.readUnsignedShort();
            buf.readUnsignedShort(); // unknown
        }

        @Override
        public String toString() {
            return "ScreenSize{" +
                    "width=" + width +
                    ", height=" + height +
                    '}';
        }
    }

    /**
     * SubCommand 0x000C
     * Client closed the status gump of a player
     */
    @Getter
    public static final class CloseStatusGump implements SubCommand {

        private final int serialId;

        public CloseStatusGump(ByteBuf buf) {
            this.serialId = buf.readInt();
        }

        @Override
        public String toString() {
            return "CloseStatusGump{" +
                    "serialId=0x" + Integer.toHexString(serialId) +
                    '}';
        }
    }

    /**
     * Fallback for unimplemented subcommands
     */
    @Getter
    public static final class UnknownSubCommand implements SubCommand {

        private final int code;

        public UnknownSubCommand(int code, ByteBuf buf) {
            this.code = code;
            // skip remaining bytes safely
            buf.skipBytes(buf.readableBytes());
        }

        @Override
        public String toString() {
            return "UnknownSubCommand{code=0x" +
                    Integer.toHexString(code) + '}';
        }
    }
}
