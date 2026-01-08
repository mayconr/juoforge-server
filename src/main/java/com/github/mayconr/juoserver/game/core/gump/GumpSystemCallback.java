package com.github.mayconr.juoserver.game.core.gump;

import com.github.mayconr.juoserver.game.packet.GumpSelection;

import io.netty.channel.Channel;

public interface GumpSystemCallback {

    void onGumpSelection(Channel channel, GumpSelection gumpSelection);
}
