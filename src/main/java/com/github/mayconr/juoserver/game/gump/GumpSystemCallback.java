package com.github.mayconr.juoserver.game.gump;

import com.github.mayconr.juoserver.game.player.SessionOutbound;
import com.github.mayconr.juoserver.network.packet.GumpSelection;

public interface GumpSystemCallback {

    void onGumpSelection(SessionOutbound outbound, GumpSelection gumpSelection);
}
