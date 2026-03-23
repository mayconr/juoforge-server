package com.github.mayconr.juoserver.game.model.event;

import com.github.mayconr.juoserver.infrastructure.eventbus.GameEvent;
import com.github.mayconr.juoserver.game.model.UOItem;

public record GroundedItemCreated(UOItem item, boolean render) implements GameEvent {}
