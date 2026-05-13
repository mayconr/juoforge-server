package com.github.mayconr.juoserver.game.model.event;

import com.github.mayconr.juoserver.game.model.UOItem;
import com.github.mayconr.juoserver.game.model.UOMobile;
import com.github.mayconr.juoserver.infrastructure.eventbus.GameEvent;

public record EquippedItemCreated(UOMobile mobile, UOItem item) implements GameEvent {
}
