package com.github.mayconr.juoserver.network.packet;

import com.github.mayconr.juoserver.infrastructure.server.AbstractPacket;
import io.netty.buffer.ByteBuf;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.HexFormat;
import java.util.Objects;

/**
 * Handles Ultima Online General Information / Extended Command packet (0xBF).
 *
 * <p>The 0xBF packet is an envelope for multiple protocol operations. Its
 * effective operation is identified by a 16-bit subcommand contained in the
 * packet payload.</p>
 *
 * <pre>
 * BYTE    0xBF
 * USHORT  packet length
 * USHORT  subcommand
 * ...     subcommand-specific payload
 * </pre>
 *
 * <p>This handler should act primarily as a dispatcher. Gameplay rules should
 * not be implemented directly here. Each supported subcommand should be
 * delegated to the appropriate packet/domain handler.</p>
 *
 * <h2>ClassicUO relevant subcommands</h2>
 *
 * <pre>
 * 0x01  Fast Walk Prevention / initialization
 * 0x02  Add Fast Walk Key
 * 0x04  Close Generic Gump
 * 0x05  Screen Size
 * 0x06  Party System
 * 0x08  Set Map / Cursor
 *
 * 0x10  Query Properties / legacy Object Property List request
 *
 * 0x13  Request Context Menu
 * 0x14  Display Context Menu
 * 0x15  Context Menu Selection
 * 0x16  Close UI Window
 *
 * 0x18  Map Diff
 * 0x19  Extended Stats / Stat Locks
 * 0x1A  Change Stat Lock
 *
 * 0x1B  Spellbook Content
 * 0x1C  Cast Spell
 *
 * 0x1D  House Revision
 * 0x1E  House Revision Request
 * 0x20  Custom Housing
 *
 * 0x21  Combat Ability State / Reset
 * 0x25  SE Ability Change
 * 0x26  Mount Speed
 *
 * 0x2A  Change Race
 * 0x2C  Use Item on Target
 * 0x2F  KR Housing Menu
 * 0x32  Toggle Gargoyle Flying
 * </pre>
 *
 * <h2>Recommended implementation priority for JUOForge</h2>
 *
 * <p>Implement only the subset required by ClassicUO and currently supported
 * gameplay features. Do not attempt to implement every historical 0xBF
 * subcommand.</p>
 *
 * <pre>
 * Core/session:
 *   0x05 Screen Size
 *
 * UI:
 *   0x04 Close Gump
 *   0x13 Request Context Menu
 *   0x14 Display Context Menu
 *   0x15 Context Menu Selection
 *   0x16 Close Window
 *
 * Object properties:
 *   0x10 Query Properties
 *
 * Stats:
 *   0x19 Extended Stats
 *   0x1A Change Stat Lock
 *
 * Magic:
 *   0x1B Spellbook Content
 *   0x1C Cast Spell
 *
 * Social:
 *   0x06 Party
 *
 * Combat:
 *   0x21 Ability State
 *   0x25 Ability Change
 *
 * Movement/mounts:
 *   0x26 Mount Speed
 *
 * Housing:
 *   0x1D House Revision
 *   0x1E House Revision Request
 *   0x20 Custom Housing
 * </pre>
 *
 * <h2>Magic note</h2>
 *
 * <p>ClassicUO uses subcommand 0x1C for normal spell casting on modern
 * client versions. This packet represents the player's intention to cast
 * a spell and should be translated into a domain-level cast request.
 * Casting rules, timing, mana consumption, reagents, interruption and
 * spell effects must remain outside the packet handler.</p>
 *
 * <p>The recommended flow is:</p>
 *
 * <pre>
 * 0xBF/0x1C
 *      |
 *      v
 * GeneralInformationHandler
 *      |
 *      v
 * SpellCastService
 *      |
 *      v
 * SpellCastFlow
 *      |
 *      v
 * CastingSession
 *      |
 *      v
 * GameLoop
 *      |
 *      v
 * SpellResolveFlow
 * </pre>
 *
 * <p>Protocol compatibility notes:</p>
 *
 * <ul>
 *   <li>Some subcommands are legacy and depend on client version.</li>
 *   <li>ClassicUO also defines private/internal 0xBF extensions such as
 *       0xBEEF for plugin communication. These are not part of the
 *       standard UO server protocol and should not normally be implemented
 *       by JUOForge.</li>
 *   <li>Unknown subcommands should be logged and safely ignored unless the
 *       protocol requires otherwise.</li>
 * </ul>
 *
 * @see <a href="https://docs.polserver.com/packets/">POL Packet Guide</a>
 * @see <a href="https://github.com/ClassicUO/ClassicUO">ClassicUO</a>
 */
@Slf4j
@Getter
public class GeneralInformation extends AbstractPacket {

    public static final int CODE = (byte)0xBF;
    private final ExtendedCommand command;
    private final OutboundExtendedCommand outboundCommand;
    private final int subCommand;

    /** Creates a server-to-client envelope for a serializable subcommand. */
    public GeneralInformation(OutboundExtendedCommand command) {
        super(CODE, outboundLength(command));
        this.outboundCommand = command;
        this.command = null;
        this.subCommand = command.subCommand();
    }

    private static int outboundLength(OutboundExtendedCommand command) {
        Objects.requireNonNull(command, "Outbound subcommand is required");
        if (command.subCommand() < 0 || command.subCommand() > 0xFFFF) {
            throw new IllegalArgumentException("Subcommand must fit an unsigned short");
        }
        int payloadLength = command.payloadLength();
        if (payloadLength < 0 || payloadLength > 0xFFFF - 5) {
            throw new IllegalArgumentException("Invalid General Information payload length: " + payloadLength);
        }
        return 5 + payloadLength;
    }

    /** Returns the decoded client-to-server command. */
    public ExtendedCommand getCommand() {
        if (command == null) {
            throw new IllegalStateException("Outgoing General Information has no inbound command");
        }
        return command;
    }

    @Override
    public void writesTo(ByteBuf buffer) {
        if (outboundCommand == null) {
            throw new UnsupportedOperationException("Incoming General Information does not support serialization");
        }
        int start = buffer.writerIndex();
        buffer.writeByte(CODE);
        buffer.writeShort(getLength());
        buffer.writeShort(subCommand);
        outboundCommand.writesTo(buffer);
        if (buffer.writerIndex() - start != getLength()) {
            throw new IllegalStateException("Extended command wrote a different length than declared");
        }
    }

    public GeneralInformation(ByteBuf buffer) {
        super(CODE, extractLength(buffer));
        this.outboundCommand = null;
        int length = getLength();
        this.subCommand = buffer.readUnsignedShort();

        this.command = switch (subCommand) {
            case ClientVersionExtendedCommand.SUB_COMMAND -> new ClientVersionExtendedCommand(buffer, getLength());
            case LanguageExtendedCommand.SUB_COMMAND -> new LanguageExtendedCommand(buffer);
            case ScreenSizeExtendedCommand.SUB_COMMAND -> new ScreenSizeExtendedCommand(buffer);
            case SpellSelectionExtendedCommand.SUB_COMMAND -> new SpellSelectionExtendedCommand(buffer);
            case CloseStatusExtendedCommand.SUB_COMMAND -> new CloseStatusExtendedCommand(buffer);

            default -> {
                String hex = HexFormat.of().toHexDigits(subCommand);
                log.warn("Unknown sub command {}", hex);
                yield new UnknownExtendedCommand(buffer, length);
            }
        };
    }

    private static int extractLength(ByteBuf buf) {
        buf.readByte(); // packet id 0xBF
        return buf.readUnsignedShort();
    }

}
