package com.github.mayconr.juoserver.game.gump;

import com.github.mayconr.juoserver.game.session.SessionOutbound;
import com.github.mayconr.juoserver.network.packet.GumpSelection;

import io.netty.channel.Channel;

public interface GumpSystemCallback {

    void onGumpSelection(SessionOutbound outbound, GumpSelection gumpSelection);
}
