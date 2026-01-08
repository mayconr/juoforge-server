package com.github.mayconr.juoserver.game.core.event;

import com.github.mayconr.juoserver.game.core.model.UOItem;

public record ItemDeleted(UOItem item) implements GameEvent {}
