package com.github.mayconr.juoserver.game.ui.gump;

import com.github.mayconr.juoserver.game.model.UOPlayer;
import com.github.mayconr.juoserver.network.packet.GumpSelection;

public interface GumpSystem {

    /** Removes expired pending gumps during the game loop update. */
    void update(double delta);

    /** Sends a gump and registers a single-use response handler for this player. */
    void send(UOPlayer player, DeclarativeGumpUI gumpUI, GumpHandler handler);

    /** Sends a gump with a finite positive expiration in seconds, overriding the configured default. */
    void send(UOPlayer player, DeclarativeGumpUI gumpUI, GumpHandler handler, double expirationSeconds);

    /** Validates a response before consuming its pending handler; unknown responses are ignored. */
    void onGumpSelection(UOPlayer player, GumpSelection gumpSelection);
}
