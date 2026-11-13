package com.github.mayconr.juoserver.game.model.event;

import com.github.mayconr.juoserver.game.model.UOPlayer;
import com.github.mayconr.juoserver.infrastructure.eventbus.GameEvent;

/** The player's client drop completed successfully, including a drop that joined a stack. */
public record ItemDropAccepted(UOPlayer player) implements GameEvent {
}
