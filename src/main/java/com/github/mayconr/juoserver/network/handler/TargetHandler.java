package com.github.mayconr.juoserver.network.handler;

import com.github.mayconr.juoserver.game.world.WorldInternal;
import com.github.mayconr.juoserver.network.packet.Target;
import com.github.mayconr.juoserver.network.session.PlayerSession;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@ChannelHandler.Sharable
public class TargetHandler extends PlayerSessionChannelInboundHandler<Target> {

    private final WorldInternal world;

    @Override
    protected void channelRead0(PlayerSession session, ChannelHandlerContext ctx, Target msg) {
        world.resolveTarget(session.getPlayer(), msg);
    }
}
