package com.github.mayconr.juoserver.game.model.event;

import com.github.mayconr.juoserver.game.event.GameEvent;
import com.github.mayconr.juoserver.game.model.UOMobile;

public record MobileLeftLineOfSight(UOMobile observer, UOMobile target) implements GameEvent {
}
