package com.github.mayconr.juoserver.game.model.event;

import com.github.mayconr.juoserver.game.model.DeathCause;
import com.github.mayconr.juoserver.game.model.UOItem;
import com.github.mayconr.juoserver.game.model.UOMobile;
import com.github.mayconr.juoserver.game.model.UOObject;
import com.github.mayconr.juoserver.infrastructure.eventbus.GameEvent;

public record MobileDeathEvent(UOObject source, UOMobile target, DeathCause cause, UOItem corpse) implements GameEvent {
}
