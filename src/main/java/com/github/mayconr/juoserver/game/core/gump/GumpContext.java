package com.github.mayconr.juoserver.game.core.gump;

import com.github.mayconr.juoserver.game.core.model.UOPlayer;

public record GumpContext(int gumpId, UOPlayer player, long createdAt, GumpHandler handler) {}
