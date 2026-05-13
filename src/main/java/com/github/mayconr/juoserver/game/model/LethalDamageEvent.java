package com.github.mayconr.juoserver.game.model;

import com.github.mayconr.juoserver.infrastructure.eventbus.GameEvent;

public record LethalDamageEvent(UOMobile source, UOMobile target, DamageSourceKind sourceKind) implements GameEvent {
}
