package com.github.mayconr.juoserver.game.model.event;

import com.github.mayconr.juoserver.infrastructure.eventbus.GameEvent;
import com.github.mayconr.juoserver.network.session.PlayerSession;
import com.github.mayconr.juoserver.network.session.SessionState;

public record PlayerSessionStatusChanged(PlayerSession session, SessionState oldState, SessionState newState) implements GameEvent {}
