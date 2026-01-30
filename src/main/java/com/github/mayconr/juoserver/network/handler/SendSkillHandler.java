package com.github.mayconr.juoserver.network.handler;

import com.github.mayconr.juoserver.game.session.SessionOutbound;
import com.github.mayconr.juoserver.game.session.player.PlayerSession;
import com.github.mayconr.juoserver.network.packet.SendSkill;
import io.netty.channel.ChannelHandler;

@ChannelHandler.Sharable
public class SendSkillHandler extends PlayerSessionChannelInboundHandler<SendSkill> {
    @Override
    protected void channelRead0(PlayerSession session, SessionOutbound outbound, SendSkill msg) {
        session.updateSkillsLock(msg.getSkills());
    }
}
