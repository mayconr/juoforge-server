package com.github.mayconr.juoserver.game.core.combat;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@RequiredArgsConstructor
public class CombatState {
    private final int attackerId;
    private final int targetId;
    private int swing = 0;
    private int nextImpactAt;
    private boolean autoSwing;
}
