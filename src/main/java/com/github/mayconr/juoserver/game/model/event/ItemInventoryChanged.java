package com.github.mayconr.juoserver.game.model.event;

import com.github.mayconr.juoserver.game.model.ItemLocation;
import com.github.mayconr.juoserver.game.model.UOItem;
import com.github.mayconr.juoserver.infrastructure.eventbus.GameEvent;

/** Inventory locations affected by a completed item operation. */
public record ItemInventoryChanged(UOItem item, ItemLocation origin, ItemLocation destination) implements GameEvent {
}
