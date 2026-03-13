package com.github.mayconr.juoserver.game.model;

import lombok.Getter;

@Getter
public enum SendSkillType {

    /**
     * Full list of skills (no per-skill cap).
     */
    FULL_LIST(0x00),

    /**
     * Single skill update (no cap).
     */
    SINGLE_UPDATE(0xFF),

    /**
     * Full list of skills including per-skill cap.
     */
    FULL_LIST_WITH_CAP(0x02),

    /**
     * Single skill update including per-skill cap.
     */
    SINGLE_UPDATE_WITH_CAP(0xDF);

    private final int code;

    SendSkillType(int code) {
        this.code = code;
    }

    public static SendSkillType fromCode(int code) {
        for (var type : values()) {
            if (type.code == code) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown SkillPacketType: " + code);
    }
}
