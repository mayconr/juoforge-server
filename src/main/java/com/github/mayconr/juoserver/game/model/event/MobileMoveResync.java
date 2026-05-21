package com.github.mayconr.juoserver.game.model.event;

import com.github.mayconr.juoserver.game.model.UOPlayer;
import com.github.mayconr.juoserver.infrastructure.eventbus.GameEvent;

public record MobileMoveResync(UOPlayer player, int sequence) implements GameEvent {
}
