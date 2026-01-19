package com.github.mayconr.juoserver.common.event;

import com.github.mayconr.juoserver.game.session.player.PlayerSession;

public record PlayerSessionStarted(PlayerSession session) implements GameEvent {}
