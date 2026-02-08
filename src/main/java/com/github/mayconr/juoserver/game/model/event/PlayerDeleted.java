package com.github.mayconr.juoserver.game.model.event;

import com.github.mayconr.juoserver.game.event.GameEvent;
import com.github.mayconr.juoserver.game.model.UOPlayer;

public record PlayerDeleted(UOPlayer deletedPlayer) implements GameEvent {
}
