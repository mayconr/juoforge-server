package com.github.mayconr.juoserver.game.core.event;

import com.github.mayconr.juoserver.game.core.session.player.PlayerSession;

public record PlayerSessionClosed(PlayerSession session) implements GameEvent {}
