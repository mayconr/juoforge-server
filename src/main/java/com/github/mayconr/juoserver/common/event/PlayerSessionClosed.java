package com.github.mayconr.juoserver.common.event;

import com.github.mayconr.juoserver.game.session.player.PlayerSession;

public record PlayerSessionClosed(PlayerSession session) implements GameEvent {}
