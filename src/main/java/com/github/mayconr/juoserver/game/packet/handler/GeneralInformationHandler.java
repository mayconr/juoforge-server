package com.github.mayconr.juoserver.game.packet.handler;

import com.github.mayconr.juoserver.game.core.session.player.PlayerSession;
import com.github.mayconr.juoserver.game.packet.GeneralInformation;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@ChannelHandler.Sharable
public class GeneralInformationHandler
        extends PlayerSessionChannelInboundHandler<GeneralInformation> {
    @Override
    protected void channelRead0(
            PlayerSession session, ChannelHandlerContext ctx, GeneralInformation msg) {
        log.info(String.valueOf(msg.getSubCommand()));
    }
}
