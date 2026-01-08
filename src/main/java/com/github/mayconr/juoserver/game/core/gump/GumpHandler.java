package com.github.mayconr.juoserver.game.core.gump;

import com.github.mayconr.juoserver.game.packet.GumpSelection;

public interface GumpHandler {
    void handle(GumpContext ctx, GumpSelection selection);
}
