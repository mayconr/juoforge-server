package com.github.mayconr.juoserver.network.handler;

import com.github.mayconr.juoserver.game.session.player.PlayerSession;
import com.github.mayconr.juoserver.network.packet.UnicodeSpeachRequest;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;

@ChannelHandler.Sharable
public class UnicodeSpeachRequestHandler
        extends PlayerSessionChannelInboundHandler<UnicodeSpeachRequest> {
    @Override
    protected void channelRead0(
            PlayerSession session, ChannelHandlerContext ctx, UnicodeSpeachRequest msg) {
        session.speech(msg);
    }
}
