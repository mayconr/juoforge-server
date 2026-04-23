package com.github.mayconr.juoserver.game.model.event;

import com.github.mayconr.juoserver.game.model.UOItem;
import com.github.mayconr.juoserver.infrastructure.eventbus.GameEvent;

public record OrphanItemCreated(UOItem item) implements GameEvent {
}
