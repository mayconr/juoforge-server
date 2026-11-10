package com.github.mayconr.juoserver.game.ui.gump;

import com.github.mayconr.juoserver.game.model.UOPlayer;
import java.util.Set;

public record GumpContext(int gumpId, UOPlayer player, long createdAt, long timeoutMillis,
                          GumpHandler handler, Set<Integer> virtueGraphics) {
    public GumpContext {
        virtueGraphics = Set.copyOf(virtueGraphics);
    }

    public GumpContext(int gumpId, UOPlayer player, long createdAt, long timeoutMillis, GumpHandler handler) {
        this(gumpId, player, createdAt, timeoutMillis, handler, Set.of());
    }

    public boolean isExpired(long now) {
        return now - createdAt >= timeoutMillis;
    }
}
