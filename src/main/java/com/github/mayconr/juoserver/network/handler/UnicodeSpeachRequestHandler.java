package com.github.mayconr.juoserver.network.handler;

import com.github.mayconr.juoserver.game.model.UOPlayer;
import com.github.mayconr.juoserver.game.session.SessionOutbound;
import com.github.mayconr.juoserver.game.session.player.PlayerSession;
import com.github.mayconr.juoserver.game.session.world.WorldInternal;
import com.github.mayconr.juoserver.network.packet.UnicodeSpeachRequest;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@ChannelHandler.Sharable
public class UnicodeSpeachRequestHandler extends PlayerSessionChannelInboundHandler<UnicodeSpeachRequest> {

    private final WorldInternal world;

    @Override
    protected void channelRead0(PlayerSession session, SessionOutbound outbound, UnicodeSpeachRequest msg) {
        world.speech((UOPlayer) session.getPlayer(), msg);
    }
}
