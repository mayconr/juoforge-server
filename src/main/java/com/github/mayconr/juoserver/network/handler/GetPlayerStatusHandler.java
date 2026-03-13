package com.github.mayconr.juoserver.network.handler;

import com.github.mayconr.juoserver.game.world.WorldInternal;
import com.github.mayconr.juoserver.network.packet.GetPlayerStatus;
import com.github.mayconr.juoserver.network.session.PlayerSession;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@ChannelHandler.Sharable
public class GetPlayerStatusHandler extends PlayerSessionChannelInboundHandler<GetPlayerStatus> {

    private final WorldInternal world;

    @Override
    protected void channelRead0(PlayerSession session, ChannelHandlerContext ctx, GetPlayerStatus msg) {
        world.playerStatusRequested(session.getPlayer(), msg);
    }
}
