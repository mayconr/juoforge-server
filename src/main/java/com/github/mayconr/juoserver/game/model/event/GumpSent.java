package com.github.mayconr.juoserver.game.model.event;

import com.github.mayconr.juoserver.infrastructure.eventbus.GameEvent;
import com.github.mayconr.juoserver.game.world.module.ui.gump.GumpBuilder;
import com.github.mayconr.juoserver.game.model.UOPlayer;

public record GumpSent(UOPlayer player, GumpBuilder.BuiltGump builtGump, int gumpId) implements GameEvent {
}
