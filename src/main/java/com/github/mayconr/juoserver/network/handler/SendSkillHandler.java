package com.github.mayconr.juoserver.network.handler;

import com.github.mayconr.juoserver.game.model.UOPlayer;
import com.github.mayconr.juoserver.game.player.SessionOutbound;
import com.github.mayconr.juoserver.game.player.PlayerSession;
import com.github.mayconr.juoserver.game.world.WorldInternal;
import com.github.mayconr.juoserver.network.packet.SendSkill;
import io.netty.channel.ChannelHandler;
import lombok.RequiredArgsConstructor;

@ChannelHandler.Sharable
@RequiredArgsConstructor
public class SendSkillHandler extends PlayerSessionChannelInboundHandler<SendSkill> {

    private final WorldInternal  world;

    @Override
    protected void channelRead0(PlayerSession session, SessionOutbound outbound, SendSkill msg) {
        world.sendSkillsLock((UOPlayer) session.getPlayer(), msg.getSkills());
    }
}
