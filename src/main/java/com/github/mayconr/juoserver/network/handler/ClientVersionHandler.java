package com.github.mayconr.juoserver.network.handler;

import com.github.mayconr.juoserver.network.packet.ClientVersion;
import com.github.mayconr.juoserver.network.session.PlayerSession;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@ChannelHandler.Sharable
@RequiredArgsConstructor
public class ClientVersionHandler extends PlayerSessionChannelInboundHandler<ClientVersion> {

    @Override
    protected void channelRead0(PlayerSession session, ChannelHandlerContext ctx, ClientVersion msg) {
        session.setClientVersion(msg.getClientVersion());
    }
}
