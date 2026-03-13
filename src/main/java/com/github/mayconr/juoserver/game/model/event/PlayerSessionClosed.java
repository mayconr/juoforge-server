package com.github.mayconr.juoserver.game.model.event;

import com.github.mayconr.juoserver.infrastructure.eventbus.GameEvent;
import com.github.mayconr.juoserver.network.session.PlayerSession;

public record PlayerSessionClosed(PlayerSession session) implements GameEvent {}
