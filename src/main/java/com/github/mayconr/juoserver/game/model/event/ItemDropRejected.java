package com.github.mayconr.juoserver.game.model.event;

import com.github.mayconr.juoserver.game.model.LiftRejectReason;
import com.github.mayconr.juoserver.game.model.UOItem;
import com.github.mayconr.juoserver.game.model.UOPlayer;
import com.github.mayconr.juoserver.infrastructure.eventbus.GameEvent;

/** A client drop was refused. Restored item is null when the player was not holding an item. */
public record ItemDropRejected(UOPlayer player, LiftRejectReason reason, UOItem restoredItem) implements GameEvent {
}
