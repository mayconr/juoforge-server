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
        switch (msg.getType()) {
            case BASIC_STATUS -> world.ui().sendStatusGump(session.getPlayer(), msg.getSerialId());
            case REQUEST_SKILL -> world.ui().sendSkillGump(session.getPlayer(), msg.getSerialId());
            case GOD_CLIENT -> System.out.println("god client");
        }
    }
}
