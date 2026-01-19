package com.github.mayconr.juoserver.game.gump;

import com.github.mayconr.juoserver.network.packet.GumpSelection;

public interface GumpHandler {
    void handle(GumpContext ctx, GumpSelection selection);
}
