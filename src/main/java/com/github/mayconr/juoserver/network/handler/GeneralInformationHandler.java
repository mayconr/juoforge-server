package com.github.mayconr.juoserver.network.handler;

import com.github.mayconr.juoserver.network.packet.GeneralInformation;
import com.github.mayconr.juoserver.network.session.PlayerSession;
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
