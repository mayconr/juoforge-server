package com.github.mayconr.juoserver.game.model.event;

import com.github.mayconr.juoserver.game.model.UOPlayer;
import com.github.mayconr.juoserver.infrastructure.eventbus.GameEvent;

/** A double-click on virtue artwork offered in a still-pending gump. Does not close the gump. */
public record VirtueSelected(UOPlayer player, int graphicId) implements GameEvent {}
