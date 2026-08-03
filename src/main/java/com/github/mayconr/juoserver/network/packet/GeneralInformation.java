package com.github.mayconr.juoserver.network.packet;

import com.github.mayconr.juoserver.infrastructure.server.AbstractPacket;
import io.netty.buffer.ByteBuf;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.HexFormat;

@Slf4j
@Getter
public class GeneralInformation extends AbstractPacket {

    public static final int CODE = (byte)0xBF;
    private final ExtendedCommand command;

    public GeneralInformation(ByteBuf buffer) {
        super(CODE, extractLength(buffer));
        int length = getLength();
        int subCommand = buffer.readUnsignedShort();

        this.command = switch (subCommand) {
            case ClientVersionExtendedCommand.SUB_COMMAND -> new ClientVersionExtendedCommand(buffer, getLength());
            case LanguageExtendedCommand.SUB_COMMAND -> new LanguageExtendedCommand(buffer);
            case ScreenSizeExtendedCommand.SUB_COMMAND -> new ScreenSizeExtendedCommand(buffer);
            case SpellSelectionExtendedCommand.SUB_COMMAND -> new SpellSelectionExtendedCommand(buffer);

            default -> {
                String hex = HexFormat.of().toHexDigits(subCommand);
                log.warn("Unknown sub command {}", hex);
                yield new UnknownExtendedCommand(buffer, length);
            }
        };

        System.out.println(command);
    }

    private static int extractLength(ByteBuf buf) {
        buf.readByte(); // packet id 0xBF
        return buf.readUnsignedShort();
    }

}
