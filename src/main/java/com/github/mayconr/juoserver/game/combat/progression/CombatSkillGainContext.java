package com.github.mayconr.juoserver.game.combat.progression;

import com.github.mayconr.juoserver.game.combat.CombatHitResult;

import com.github.mayconr.juoserver.game.model.UOMobile;
import com.github.mayconr.juoserver.game.model.WeaponStyle;

/** Describes a resolved swing before any skill gains or damage are applied. */
public record CombatSkillGainContext(
        UOMobile attacker,
        UOMobile defender,
        WeaponStyle attackerStyle,
        WeaponStyle defenderStyle,
        CombatHitResult result
) {}
