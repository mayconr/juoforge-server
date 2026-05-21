package com.github.mayconr.juoserver.game.model.event;

import com.github.mayconr.juoserver.game.model.Direction;
import com.github.mayconr.juoserver.game.model.Location;
import com.github.mayconr.juoserver.infrastructure.eventbus.GameEvent;
import com.github.mayconr.juoserver.game.model.MovementResult;
import com.github.mayconr.juoserver.game.model.UOMobile;

public record MobileMoved(UOMobile mobile, Direction direction,
                          Location from,
                          Location to,
                          boolean running,
                          int sequence,
                          boolean teleport) implements GameEvent {}
