package com.github.mayconr.juoserver.game.ui.policy;

import com.github.mayconr.juoserver.game.model.UOPlayer;

public record DoubleClickRequest(UOPlayer player, int serialId) { }
