package com.github.mayconr.juoserver.game.model;

/** Related animations, with a mounted variant where one is known. */
public enum AnimationGroup {
    WALK_UNARMED(AnimationType.WALK_UNARMED, AnimationType.MOUNTED_WALK),
    WALK_ARMED(AnimationType.WALK_ARMED, AnimationType.MOUNTED_WALK),
    RUN_UNARMED(AnimationType.RUN_UNARMED, AnimationType.MOUNTED_RUN),
    RUN_ARMED(AnimationType.RUN_ARMED, AnimationType.MOUNTED_RUN),
    IDLE(AnimationType.IDLE, AnimationType.MOUNTED_IDLE),
    FIDGET_1(AnimationType.FIDGET_1),
    FIDGET_2(AnimationType.FIDGET_2),
    COMBAT_STANCE_ONE_HANDED(AnimationType.COMBAT_STANCE_ONE_HANDED),
    COMBAT_STANCE_TWO_HANDED(AnimationType.COMBAT_STANCE_TWO_HANDED),
    ATTACK_ONE_HANDED_DOWN(AnimationType.ATTACK_ONE_HANDED_DOWN, AnimationType.MOUNTED_ATTACK_MELEE),
    ATTACK_ONE_HANDED_THRUST(AnimationType.ATTACK_ONE_HANDED_THRUST, AnimationType.MOUNTED_ATTACK_MELEE),
    ATTACK_ONE_HANDED_WIDE(AnimationType.ATTACK_ONE_HANDED_WIDE, AnimationType.MOUNTED_ATTACK_MELEE),
    ATTACK_TWO_HANDED_DOWN(AnimationType.ATTACK_TWO_HANDED_DOWN, AnimationType.MOUNTED_ATTACK_MELEE),
    ATTACK_TWO_HANDED_WIDE(AnimationType.ATTACK_TWO_HANDED_WIDE, AnimationType.MOUNTED_ATTACK_MELEE),
    ATTACK_TWO_HANDED_THRUST(AnimationType.ATTACK_TWO_HANDED_THRUST, AnimationType.MOUNTED_ATTACK_MELEE),
    WALK_WAR_MODE(AnimationType.WALK_WAR_MODE, AnimationType.MOUNTED_WALK),
    CAST_DIRECTED(AnimationType.CAST_DIRECTED),
    CAST_AREA(AnimationType.CAST_AREA),
    ATTACK_BOW(AnimationType.ATTACK_BOW, AnimationType.MOUNTED_ATTACK_BOW),
    ATTACK_CROSSBOW(AnimationType.ATTACK_CROSSBOW, AnimationType.MOUNTED_ATTACK_CROSSBOW),
    GET_HIT(AnimationType.GET_HIT),
    DIE_BACKWARD(AnimationType.DIE_BACKWARD),
    DIE_FORWARD(AnimationType.DIE_FORWARD),
    MOUNTED_SPECIAL(AnimationType.MOUNTED_SPECIAL),
    SPECIAL_1(AnimationType.SPECIAL_1),
    SPECIAL_2(AnimationType.SPECIAL_2),
    BOW(AnimationType.BOW),
    SALUTE(AnimationType.SALUTE),
    SPECIAL_3(AnimationType.SPECIAL_3),
    SPECIAL_4(AnimationType.SPECIAL_4),
    SPECIAL_5(AnimationType.SPECIAL_5);

    private final AnimationType onFootAnimation;
    private final AnimationType mountedAnimation;

    AnimationGroup(AnimationType animation) {
        this(animation, animation);
    }

    AnimationGroup(AnimationType onFootAnimation, AnimationType mountedAnimation) {
        this.onFootAnimation = onFootAnimation;
        this.mountedAnimation = mountedAnimation;
    }

    public AnimationType animationFor(boolean mounted) {
        return mounted ? mountedAnimation : onFootAnimation;
    }
}
