package com.github.mayconr.juoserver.game.model.event;

import com.github.mayconr.juoserver.game.model.DamageSourceKind;
import com.github.mayconr.juoserver.game.model.UOItem;
import com.github.mayconr.juoserver.game.model.UOMobile;
import com.github.mayconr.juoserver.infrastructure.eventbus.GameEvent;

public record MobileDeathEvent(UOMobile source, UOMobile target, DamageSourceKind sourceKind, UOItem corpse) implements GameEvent {
}
