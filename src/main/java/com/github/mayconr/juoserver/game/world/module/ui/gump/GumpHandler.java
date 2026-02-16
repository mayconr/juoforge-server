package com.github.mayconr.juoserver.game.world.module.ui.gump;

import com.github.mayconr.juoserver.network.packet.GumpSelection;

public interface GumpHandler {
    void handle(GumpContext ctx, GumpSelection selection);
}
