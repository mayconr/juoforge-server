package com.github.mayconr.juoserver.common.event;

import com.github.mayconr.juoserver.game.session.player.PlayerSession;

public record PlayerSessionCreated(PlayerSession session) implements GameEvent {}
