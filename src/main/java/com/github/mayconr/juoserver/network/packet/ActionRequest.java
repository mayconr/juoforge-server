package com.github.mayconr.juoserver.network.packet;

import com.github.mayconr.juoserver.game.model.ActionPayload;
import com.github.mayconr.juoserver.game.model.ActionSubCommand;
import com.github.mayconr.juoserver.infrastructure.server.AbstractPacket;
import io.netty.buffer.ByteBuf;
import lombok.Getter;

@Getter
public class ActionRequest extends AbstractPacket {

    public static final int CODE = (byte) 0xD7;

    private final int playerSerial;
    private final ActionSubCommand subCommand;
    private final ActionPayload payload;

    public ActionRequest(ByteBuf buf) {
        super(CODE, -1);
        buf.readByte(); // CODE
        buf.readShort(); // length
        this.playerSerial = buf.readInt();
        this.subCommand = ActionSubCommand.fromId(buf.readUnsignedShort());
        this.payload = readPayload(subCommand, buf);
    }

    public ActionRequest(ActionSubCommand subCommand) {
        super(CODE, -1);
        this.playerSerial = -1;
        this.subCommand = subCommand;
        this.payload = null;
    }

    private ActionPayload readPayload(ActionSubCommand sub, ByteBuf buf) {
        return switch (sub) {
            case BACKUP, COMMIT, RESTORE, EXIT_HOUSE_TOOL -> new SimpleActionPayload(sub);
            case QUEST_BUTTON -> new QuestActionPayload(buf);
            case COMBAT_ABILITY -> new CombatAbilityPayload(buf);
            case ADD_ITEM -> new AddItemPayload(buf);
            case DELETE_ITEM -> new DeleteItemPayload(buf);
            case CHANGE_FLOOR -> new ChangeFloorPayload(buf);
            default -> new UnknownActionPayload(sub, buf);
        };
    }

    private record SimpleActionPayload(ActionSubCommand subCommand) implements ActionPayload { }

    @Getter
    public static final class CombatAbilityPayload implements ActionPayload {

        private final int abilityId;

        public CombatAbilityPayload(ByteBuf reader) {
            reader.readInt(); // always 0
            this.abilityId = reader.readUnsignedByte();
            reader.readUnsignedByte(); // 0x0A
        }

        @Override
        public ActionSubCommand subCommand() {
            return ActionSubCommand.COMBAT_ABILITY;
        }

    }

    @Getter
    public static final class AddItemPayload implements ActionPayload {

        private final int graphic;
        private final int x;
        private final int y;

        public AddItemPayload(ByteBuf reader) {
            reader.readByte(); // unknown
            reader.readShort(); // unknown
            this.graphic = reader.readUnsignedShort();
            reader.readByte();
            this.x = reader.readInt();
            reader.readByte();
            this.y = reader.readInt();
            reader.readUnsignedByte(); // terminator 0x07
        }

        @Override
        public ActionSubCommand subCommand() {
            return ActionSubCommand.ADD_ITEM;
        }

    }

    @Getter
    public static final class DeleteItemPayload implements ActionPayload {

        private final int itemGraphic;
        private final int x;
        private final int y;
        private final int z;

        public DeleteItemPayload(ByteBuf reader) {
            reader.readByte();          // unknown
            reader.readShort();          // unknown
            this.itemGraphic = reader.readUnsignedShort();
            reader.readByte();          // unknown
            this.x = reader.readInt();
            reader.readByte();          // unknown
            this.y = reader.readInt();
            reader.readByte();          // unknown
            this.z = reader.readInt();
            reader.readUnsignedByte(); // terminator 0x07
        }

        @Override
        public ActionSubCommand subCommand() {
            return ActionSubCommand.DELETE_ITEM;
        }

    }

    @Getter
    public static final class ChangeFloorPayload implements ActionPayload {

        private final int floor;

        public ChangeFloorPayload(ByteBuf reader) {
            reader.readInt();          // unknown (always 0)
            this.floor = reader.readUnsignedByte();
            reader.readUnsignedByte(); // terminator 0x07
        }

        @Override
        public ActionSubCommand subCommand() {
            return ActionSubCommand.CHANGE_FLOOR;
        }

    }

    @Getter
    public static final class UnknownActionPayload implements ActionPayload {

        private final ActionSubCommand subCommand;
        private final ByteBuf data;

        public UnknownActionPayload(ActionSubCommand subCommand, ByteBuf reader) {
            this.subCommand = subCommand;
            this.data = reader.readRetainedSlice(reader.readableBytes());
        }

        @Override
        public ActionSubCommand subCommand() {
            return subCommand;
        }

    }

    private static class QuestActionPayload implements ActionPayload {
        public QuestActionPayload(ByteBuf buf) {
            buf.readByte(); // unknown
        }

        @Override
        public ActionSubCommand subCommand() {
            return ActionSubCommand.QUEST_BUTTON;
        }
    }
}
