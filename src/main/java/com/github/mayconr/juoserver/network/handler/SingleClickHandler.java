package com.github.mayconr.juoserver.network.handler;

import com.github.mayconr.juoserver.game.session.SessionOutbound;
import com.github.mayconr.juoserver.game.session.player.PlayerSession;
import com.github.mayconr.juoserver.network.packet.SingleClickRequest;
import io.netty.channel.ChannelHandler;

@ChannelHandler.Sharable
public class SingleClickHandler extends PlayerSessionChannelInboundHandler<SingleClickRequest> {

    @Override
    protected void channelRead0(PlayerSession session, SessionOutbound outbound, SingleClickRequest msg) {
        session.singleClick(msg);
    }
}
