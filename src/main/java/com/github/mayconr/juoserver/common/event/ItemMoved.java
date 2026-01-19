package com.github.mayconr.juoserver.common.event;

import com.github.mayconr.juoserver.game.model.UOItem;

public record ItemMoved(UOItem item) implements GameEvent {}
