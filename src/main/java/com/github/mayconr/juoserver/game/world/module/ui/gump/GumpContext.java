package com.github.mayconr.juoserver.game.world.module.ui.gump;

import com.github.mayconr.juoserver.game.model.UOPlayer;

public record GumpContext(int gumpId, UOPlayer player, long createdAt, GumpHandler handler) {}
