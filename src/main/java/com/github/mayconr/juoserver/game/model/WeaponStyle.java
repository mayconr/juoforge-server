package com.github.mayconr.juoserver.game.model;

import com.github.mayconr.juoserver.game.combat.flow.execution.CombatType;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum WeaponStyle {
    MELEE(CombatType.MELEE),
    RANGED(CombatType.RANGED);

    private final CombatType combatType;
}
