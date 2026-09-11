package com.github.mayconr.juoserver.game.model.event;

import com.github.mayconr.juoserver.game.combat.flow.execution.CombatType;
import com.github.mayconr.juoserver.game.model.UOMobile;
import com.github.mayconr.juoserver.infrastructure.eventbus.GameEvent;

public record CombatOccurring(
        UOMobile attacker,
        UOMobile target,
        int hitFrame,
        CombatType combatType
) implements GameEvent {
}
