package com.github.mayconr.juoserver.game.model;

import java.util.EnumSet;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AnimationGroupTest {
    @Test
    void coversEveryKnownAnimation() {
        var covered = EnumSet.noneOf(AnimationType.class);
        for (var group : AnimationGroup.values()) {
            covered.add(group.animationFor(false));
            covered.add(group.animationFor(true));
        }
        assertEquals(EnumSet.allOf(AnimationType.class), covered);
    }

    @ParameterizedTest
    @EnumSource(value = AnimationGroup.class, names = {
            "ATTACK_ONE_HANDED_DOWN", "ATTACK_ONE_HANDED_THRUST", "ATTACK_ONE_HANDED_WIDE",
            "ATTACK_TWO_HANDED_DOWN", "ATTACK_TWO_HANDED_WIDE", "ATTACK_TWO_HANDED_THRUST"
    })
    void meleeAttacksUseMountedMelee(AnimationGroup group) {
        assertEquals(AnimationType.MOUNTED_ATTACK_MELEE, group.animationFor(true));
        assertEquals(AnimationType.valueOf(group.name()), group.animationFor(false));
    }

    @Test
    void rangedAttacksKeepTheirMountedWeaponType() {
        assertEquals(AnimationType.MOUNTED_ATTACK_BOW, AnimationGroup.ATTACK_BOW.animationFor(true));
        assertEquals(AnimationType.MOUNTED_ATTACK_CROSSBOW, AnimationGroup.ATTACK_CROSSBOW.animationFor(true));
    }

    @Test
    void singleAnimationGroupUsesSameAnimationWhenMounted() {
        assertEquals(AnimationType.SALUTE, AnimationGroup.SALUTE.animationFor(false));
        assertEquals(AnimationType.SALUTE, AnimationGroup.SALUTE.animationFor(true));
    }
}
