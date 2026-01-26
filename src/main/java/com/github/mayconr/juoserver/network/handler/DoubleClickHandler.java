package com.github.mayconr.juoserver.network.handler;

import com.github.mayconr.juoserver.game.session.SessionOutbound;
import com.github.mayconr.juoserver.game.session.player.PlayerSession;
import com.github.mayconr.juoserver.network.packet.DoubleClick;
import io.netty.channel.ChannelHandler;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@ChannelHandler.Sharable
public class DoubleClickHandler extends PlayerSessionChannelInboundHandler<DoubleClick> {
    @Override
    protected void channelRead0(PlayerSession session, SessionOutbound outbound, DoubleClick msg) {
        session.doubleClick(msg);
    }
}
