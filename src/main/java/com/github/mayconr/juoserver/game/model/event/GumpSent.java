package com.github.mayconr.juoserver.game.model.event;

import com.github.mayconr.juoserver.game.event.GameEvent;
import com.github.mayconr.juoserver.game.gump.GumpBuilder;
import com.github.mayconr.juoserver.game.model.UOPlayer;

public record GumpSent(UOPlayer player, GumpBuilder.BuiltGump builtGump, int gumpId) implements GameEvent {
}
