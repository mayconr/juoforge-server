package com.github.mayconr.juoserver.game.model.event;

import com.github.mayconr.juoserver.infrastructure.eventbus.GameEvent;
import com.github.mayconr.juoserver.game.model.UONpc;

public record NpcDeleted(UONpc deletedNpc) implements GameEvent {
}
