package com.github.mayconr.juoserver.game.model.event;

import com.github.mayconr.juoserver.game.model.UOContainer;
import com.github.mayconr.juoserver.infrastructure.eventbus.GameEvent;
import com.github.mayconr.juoserver.game.model.UOItem;

public record ItemUpdated(UOItem item, UOContainer container) implements GameEvent {}
