package com.github.mayconr.juoserver.network.handler;

import com.github.mayconr.juoserver.network.packet.OpenChatRequest;
import com.github.mayconr.juoserver.network.session.PlayerSession;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@ChannelHandler.Sharable
public final class OpenChatRequestHandler extends PlayerSessionChannelInboundHandler<OpenChatRequest> {
    @Override
    protected void channelRead0(PlayerSession session, ChannelHandlerContext ctx, OpenChatRequest msg) {
        log.info("Session [{}] received open chat request (0xB5)", ctx.channel().id().asShortText());
        // TODO: Implement channel chat initialization and the server response.
    }
}
