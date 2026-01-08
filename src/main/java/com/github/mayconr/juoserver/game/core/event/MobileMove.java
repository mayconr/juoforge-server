package com.github.mayconr.juoserver.game.core.event;

import com.github.mayconr.juoserver.game.core.model.Direction;
import com.github.mayconr.juoserver.game.core.model.UOMobile;

public record MobileMove(UOMobile mobile, Direction direction) implements GameEvent {}
