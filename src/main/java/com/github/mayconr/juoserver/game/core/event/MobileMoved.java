package com.github.mayconr.juoserver.game.core.event;

import com.github.mayconr.juoserver.game.core.model.MovementResult;
import com.github.mayconr.juoserver.game.core.model.UOMobile;

public record MobileMoved(UOMobile mobile, MovementResult result) implements GameEvent {}
