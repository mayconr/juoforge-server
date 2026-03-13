package com.github.mayconr.juoserver.network.handler;

import com.github.mayconr.juoserver.infrastructure.server.ClientConnectedHandlerAdapter;
import com.github.mayconr.juoserver.network.session.PlayerSession;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class PlayerSessionChannelInboundHandler<T> extends SessionChannelInboundHandler<T> {

    @Override
    public void channelRead0(ChannelHandlerContext ctx, T msg) throws Exception {
        if (log.isDebugEnabled()) {
            log.debug("Received packet from server: {}", msg);
        }
        channelRead0(ctx.channel().attr(ClientConnectedHandlerAdapter.PLAYER_SESSION_KEY).get(), ctx, msg);
    }

    protected void channelRead0(PlayerSession session, ChannelHandlerContext ctx, T msg) {}


}
