package com.github.mayconr.juoserver.game.packet.handler;

import com.github.mayconr.juoserver.game.core.session.player.PlayerSession;
import com.github.mayconr.juoserver.game.packet.UnicodeSpeachRequest;

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
