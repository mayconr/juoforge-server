package com.github.mayconr.juoserver.game.model.event;

import com.github.mayconr.juoserver.game.model.DamageComponent;
import com.github.mayconr.juoserver.game.model.DamageSourceKind;
import com.github.mayconr.juoserver.game.model.UOMobile;
import com.github.mayconr.juoserver.infrastructure.eventbus.GameEvent;

import java.util.List;

public record MobileDamagedEvent(UOMobile source, UOMobile target, DamageSourceKind sourceKind,
                                 List<DamageComponent> components, int totalDamage, int oldHitPoints, int newHitPoints) implements GameEvent {
}
