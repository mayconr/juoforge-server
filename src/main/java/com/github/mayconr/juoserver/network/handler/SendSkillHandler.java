package com.github.mayconr.juoserver.network.handler;

import com.github.mayconr.juoserver.game.world.WorldInternal;
import com.github.mayconr.juoserver.network.packet.SendSkill;
import com.github.mayconr.juoserver.network.session.PlayerSession;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import lombok.RequiredArgsConstructor;

@ChannelHandler.Sharable
@RequiredArgsConstructor
public class SendSkillHandler extends PlayerSessionChannelInboundHandler<SendSkill> {

    private final WorldInternal  world;

    @Override
    protected void channelRead0(PlayerSession session, ChannelHandlerContext ctx, SendSkill msg) {
        world.sendSkillsLock(session.getPlayer(), msg.getSkills());
    }
}
