package com.github.mayconr.juoserver.network.handler;

import com.github.mayconr.juoserver.game.session.SessionOutbound;
import com.github.mayconr.juoserver.game.session.player.PlayerSession;
import com.github.mayconr.juoserver.network.packet.GetPlayerStatus;
import com.github.mayconr.juoserver.network.packet.StatusBarInfo;
import io.netty.channel.ChannelHandler;

@ChannelHandler.Sharable
public class GetPlayerStatusHandler extends PlayerSessionChannelInboundHandler<GetPlayerStatus> {

    @Override
    protected void channelRead0(PlayerSession session, SessionOutbound outbound, GetPlayerStatus msg) {
        switch (msg.getType()) {
            case BASIC_STATUS -> outbound.writeAndFlush(new StatusBarInfo(session.getPlayer()));
            case REQUEST_SKILL -> session.sendSkillGump(msg.getSerialId());
            case GOD_CLIENT -> System.out.println("god client");
        }
    }
}
