package com.github.mayconr.juoserver.game.core.event;

import com.github.mayconr.juoserver.game.core.model.UOItem;

public record ItemCreated(UOItem item) implements GameEvent {}
