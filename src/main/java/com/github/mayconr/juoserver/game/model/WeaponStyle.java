package com.github.mayconr.juoserver.game.model;

import com.github.mayconr.juoserver.game.combat.flow.execution.CombatType;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum WeaponStyle {

    AXE(
            CombatType.MELEE,
            AnimationType.ATTACK_TWO_HANDED_WIDE,
            AnimationType.MOUNTED_ATTACK_MELEE
    ),

    PICKAXE(
            CombatType.MELEE,
            AnimationType.ATTACK_TWO_HANDED_DOWN,
            AnimationType.MOUNTED_ATTACK_MELEE
    ),

    SWORD(
            CombatType.MELEE,
            AnimationType.ATTACK_ONE_HANDED_WIDE,
            AnimationType.MOUNTED_ATTACK_MELEE
    ),

    KATANA(
            CombatType.MELEE,
            AnimationType.ATTACK_TWO_HANDED_WIDE,
            AnimationType.MOUNTED_ATTACK_MELEE
    ),

    SPEAR(
            CombatType.MELEE,
            AnimationType.ATTACK_TWO_HANDED_THRUST,
            AnimationType.MOUNTED_ATTACK_MELEE
    ),

    KRYSS(
            CombatType.MELEE,
            AnimationType.ATTACK_ONE_HANDED_THRUST,
            AnimationType.MOUNTED_ATTACK_MELEE
    ),

    STAFF(
            CombatType.MELEE,
            AnimationType.ATTACK_TWO_HANDED_DOWN,
            AnimationType.MOUNTED_ATTACK_MELEE
    ),

    BOW(
            CombatType.RANGED,
            AnimationType.ATTACK_BOW,
            AnimationType.MOUNTED_ATTACK_BOW
    ),

    CROSSBOW(
            CombatType.RANGED,
            AnimationType.ATTACK_CROSSBOW,
            AnimationType.MOUNTED_ATTACK_CROSSBOW
    ),

    WRESTLING(
            CombatType.WRESTLING,
            AnimationType.ATTACK_ONE_HANDED_DOWN,
            AnimationType.MOUNTED_ATTACK_MELEE
    );

    private final CombatType combatType;
    private final AnimationType onFootAnimation;
    private final AnimationType mountedAnimation;

    public AnimationType animationFor(boolean mounted) {
        return mounted ? mountedAnimation : onFootAnimation;
    }
}
