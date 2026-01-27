package com.github.mayconr.juoserver.network.handler;

import com.github.mayconr.juoserver.game.session.player.PlayerSession;
import com.github.mayconr.juoserver.network.packet.GetPlayerStatus;
import com.github.mayconr.juoserver.network.packet.StatusBarInfo;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;

@ChannelHandler.Sharable
public class GetPlayerStatusHandler extends PlayerSessionChannelInboundHandler<GetPlayerStatus> {
    @Override
    protected void channelRead0(
            PlayerSession session, ChannelHandlerContext ctx, GetPlayerStatus msg) {
        switch (msg.getType()) {
            case BASIC_STATUS -> ctx.writeAndFlush(new StatusBarInfo(session.getPlayer()));
            case REQUEST_SKILL -> System.out.println("pediu skill");
            case GOD_CLIENT -> System.out.println("god client");
        }

    }
}
