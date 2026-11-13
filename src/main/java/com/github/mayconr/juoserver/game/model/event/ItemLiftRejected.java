package com.github.mayconr.juoserver.game.model.event;

import com.github.mayconr.juoserver.game.model.LiftRejectReason;
import com.github.mayconr.juoserver.game.model.UOPlayer;
import com.github.mayconr.juoserver.infrastructure.eventbus.GameEvent;

/** The player's request to lift an item was refused and the client must put the item back. */
public record ItemLiftRejected(UOPlayer player, LiftRejectReason reason) implements GameEvent {
}
