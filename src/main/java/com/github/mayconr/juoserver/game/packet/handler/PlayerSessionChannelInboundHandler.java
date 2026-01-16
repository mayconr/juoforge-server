package com.github.mayconr.juoserver.game.packet.handler;

import com.github.mayconr.juoserver.game.core.session.SessionOutbound;
import com.github.mayconr.juoserver.game.core.session.player.PlayerSession;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class PlayerSessionChannelInboundHandler<T> extends SessionChannelInboundHandler<T> {

    @Override
    protected void channelRead0(SessionOutbound outbound, T msg) {
        channelRead0(outbound.attr().get(AttributeKeys.PLAYER_SESSION_KEY), outbound.getCtx(), msg);
        // TODO remover acima

        channelRead0(outbound.attr().get(AttributeKeys.PLAYER_SESSION_KEY), outbound, msg);
    }

    /**
     * Use {@link #channelRead0(PlayerSession, SessionOutbound, Object)}
     * @param session
     * @param ctx
     * @param msg
     */
    @Deprecated
    protected void channelRead0(PlayerSession session, ChannelHandlerContext ctx, T msg) {};

    protected void channelRead0(PlayerSession session, SessionOutbound outbound, T msg) {};

}
