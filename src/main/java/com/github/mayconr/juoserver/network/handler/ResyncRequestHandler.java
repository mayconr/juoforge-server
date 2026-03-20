package com.github.mayconr.juoserver.network.handler;

import com.github.mayconr.juoserver.network.packet.MovementResyncAck;
import com.github.mayconr.juoserver.network.session.PlayerSession;
import io.netty.channel.ChannelHandlerContext;

public class ResyncRequestHandler extends PlayerSessionChannelInboundHandler<MovementResyncAck> {

    @Override
    protected void channelRead0(PlayerSession session, ChannelHandlerContext ctx, MovementResyncAck msg) {
        session.resync(msg);
    }
}
