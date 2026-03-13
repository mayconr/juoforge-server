package com.github.mayconr.juoserver.game.model;

import lombok.Getter;

@Getter
public enum ActionSubCommand {

    BACKUP(0x02),
    RESTORE(0x03),
    COMMIT(0x04),
    DELETE_ITEM(0x05),
    ADD_ITEM(0x06),
    EXIT_HOUSE_TOOL(0x0C),
    CHANGE_STAIRS(0x0D),
    SYNCH(0x0E),
    CLEAR(0x10),
    CHANGE_FLOOR(0x12),
    REVERT(0x1A),
    COMBAT_ABILITY(0x19),
    GUILD_BUTTON(0x28),
    QUEST_BUTTON(0x32),
    UNKNOWN(-1),
    HELP_BUTTON(-2);

    private final int id;

    ActionSubCommand(int id) {
        this.id = id;
    }

    public static ActionSubCommand fromId(int id) {
        for (var v : values()) {
            if (v.id == id) return v;
        }
        return UNKNOWN;
    }
}
