package com.github.mayconr.juoserver.network.handler;

import com.github.mayconr.juoserver.game.session.SessionOutbound;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

public abstract class SessionChannelInboundHandler<T> extends SimpleChannelInboundHandler<T> {

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, T msg) throws Exception {
        channelRead0(ctx.channel().attr(AttributeKeys.SESSION_OUTBOUND_KEY).get(), msg);
    }

    protected abstract void channelRead0(SessionOutbound outbound, T msg);

}
