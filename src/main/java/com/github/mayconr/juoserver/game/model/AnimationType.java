package com.github.mayconr.juoserver.game.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum AnimationType {
    WALK_UNARMED(0x00),
    WALK_ARMED(0x01),
    RUN_UNARMED(0x02),
    RUN_ARMED(0x03),

    IDLE(0x04),
    FIDGET_1(0x05),
    FIDGET_2(0x06),

    COMBAT_STANCE_ONE_HANDED(0x07),
    COMBAT_STANCE_TWO_HANDED(0x08),

    ATTACK_ONE_HANDED_DOWN(0x09),
    ATTACK_ONE_HANDED_THRUST(0x0A),
    ATTACK_ONE_HANDED_WIDE(0x0B),

    ATTACK_TWO_HANDED_DOWN(0x0C),
    ATTACK_TWO_HANDED_WIDE(0x0D),
    ATTACK_TWO_HANDED_THRUST(0x0E),

    WALK_WAR_MODE(0x0F),

    CAST_DIRECTED(0x10),
    CAST_AREA(0x11),

    ATTACK_BOW(0x12),
    ATTACK_CROSSBOW(0x13),

    GET_HIT(0x14),
    DIE_BACKWARD(0x15),
    DIE_FORWARD(0x16),

    MOUNTED_WALK(0x17),
    MOUNTED_RUN(0x18),
    MOUNTED_IDLE(0x19),

    MOUNTED_ATTACK_MELEE(0x1A),
    MOUNTED_ATTACK_BOW(0x1B),
    MOUNTED_ATTACK_CROSSBOW(0x1C),

    MOUNTED_SPECIAL(0x1D),
    SPECIAL_1(0x1E),
    SPECIAL_2(0x1F),

    BOW(0x20),
    SALUTE(0x21),

    SPECIAL_3(0x22),
    SPECIAL_4(0x23),
    SPECIAL_5(0x24);

    private final int code;
}
