package com.github.mayconr.juoserver.game.model.event;

import com.github.mayconr.juoserver.infrastructure.eventbus.GameEvent;
import com.github.mayconr.juoserver.game.model.CharacterStatus;
import com.github.mayconr.juoserver.game.model.UOMobile;

public record MobileStatusChanged(UOMobile mobile, CharacterStatus newStatus, CharacterStatus oldStatus) implements GameEvent {
}
