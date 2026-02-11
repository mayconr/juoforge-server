package com.github.mayconr.juoserver.network.handler;

import com.github.mayconr.juoserver.game.gump.GumpSystemCallback;
import com.github.mayconr.juoserver.game.player.SessionOutbound;
import com.github.mayconr.juoserver.game.player.PlayerSession;
import com.github.mayconr.juoserver.network.packet.GumpSelection;
import io.netty.channel.ChannelHandler;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@ChannelHandler.Sharable
public class GumpSelectionHandler extends PlayerSessionChannelInboundHandler<GumpSelection> {

    private final GumpSystemCallback gumpSystemCallback;

    @Override
    protected void channelRead0(PlayerSession session, SessionOutbound outbound, GumpSelection msg) {
        gumpSystemCallback.onGumpSelection(outbound, msg);
    }
}
