package com.github.mayconr.juoserver.game.model.event;

import com.github.mayconr.juoserver.game.event.GameEvent;
import com.github.mayconr.juoserver.game.model.MovementResult;
import com.github.mayconr.juoserver.game.model.UOMobile;

public record MobileMoved(UOMobile mobile, MovementResult result, int sequence, boolean teleport) implements GameEvent {}
