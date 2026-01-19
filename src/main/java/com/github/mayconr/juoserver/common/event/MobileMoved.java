package com.github.mayconr.juoserver.common.event;

import com.github.mayconr.juoserver.game.model.MovementResult;
import com.github.mayconr.juoserver.game.model.UOMobile;

public record MobileMoved(UOMobile mobile, MovementResult result) implements GameEvent {}
