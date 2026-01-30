package com.github.mayconr.juoserver.network.handler;

import com.github.mayconr.juoserver.game.session.SessionOutbound;
import com.github.mayconr.juoserver.game.session.player.PlayerSession;
import com.github.mayconr.juoserver.network.packet.ActionRequest;
import io.netty.channel.ChannelHandler;

@ChannelHandler.Sharable
public class ActionRequestedHandler extends PlayerSessionChannelInboundHandler<ActionRequest> {

    @Override
    protected void channelRead0(PlayerSession session, SessionOutbound outbound, ActionRequest msg) {
        session.handleAction(msg);
    }
}
