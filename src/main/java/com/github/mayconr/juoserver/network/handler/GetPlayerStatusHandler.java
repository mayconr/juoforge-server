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
        ctx.writeAndFlush(new StatusBarInfo(session.getPlayer()));
    }
}
