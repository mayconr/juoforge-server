package com.github.mayconr.juoserver.game.world.module.ui.gump;

import com.github.mayconr.juoserver.game.model.UOPlayer;
import com.github.mayconr.juoserver.network.packet.GumpSelection;

public interface GumpSystem {

    void send(UOPlayer player, DeclarativeGumpUI gumpUI, GumpHandler handler);

    void onGumpSelection(UOPlayer player, GumpSelection gumpSelection);
}
