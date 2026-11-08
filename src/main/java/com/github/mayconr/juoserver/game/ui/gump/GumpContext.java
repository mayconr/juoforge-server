package com.github.mayconr.juoserver.game.ui.gump;

import com.github.mayconr.juoserver.game.model.UOPlayer;

public record GumpContext(int gumpId, UOPlayer player, long createdAt, long timeoutMillis, GumpHandler handler) {
    public boolean isExpired(long now) {
        return now - createdAt >= timeoutMillis;
    }
}
