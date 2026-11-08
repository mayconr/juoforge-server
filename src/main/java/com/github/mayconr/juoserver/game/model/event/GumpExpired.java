package com.github.mayconr.juoserver.game.model.event;

import com.github.mayconr.juoserver.game.model.UOPlayer;
import com.github.mayconr.juoserver.infrastructure.eventbus.GameEvent;

/** Published after an expired pending gump is removed from server memory. */
public record GumpExpired(UOPlayer player, int gumpId) implements GameEvent {}
