package com.github.mayconr.juoserver.infrastructure.server;

import com.github.mayconr.juoserver.network.session.PlayerSession;
import com.github.mayconr.juoserver.network.session.SessionManager;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.group.ChannelGroup;
import io.netty.util.AttributeKey;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RequiredArgsConstructor
@Slf4j
@ChannelHandler.Sharable
public class ClientConnectedHandlerAdapter extends ChannelInboundHandlerAdapter {

    public static final AttributeKey<PlayerSession> PLAYER_SESSION_KEY = AttributeKey.valueOf("PLAYER_SESSION_KEY");
    private final ChannelGroup channelGroup;
    private final SessionManager sessionManager;

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
        var channel = ctx.channel();

        channelGroup.add(channel);
        var session = sessionManager.createSession(channel);

        channel.attr(PLAYER_SESSION_KEY).set(session);

        final var remoteAddress = channel.remoteAddress();

        session.connect(remoteAddress);

        log.info("New connection from: {}", remoteAddress);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        super.channelInactive(ctx);
        var channel = ctx.channel();

        channelGroup.remove(channel);
        var session = channel.attr(PLAYER_SESSION_KEY).getAndSet(null);

        session.disconnect();

        final var remoteAddress = channel.remoteAddress();
        log.info("Connection closed: {}", remoteAddress);
    }
}
